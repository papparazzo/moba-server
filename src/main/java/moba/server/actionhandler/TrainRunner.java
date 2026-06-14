/*
 *  Project:    moba-server
 *
 *  Copyright (C) 2025 Stefan Paproth <pappi-@gmx.de>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program. If not, see <https://www.gnu.org/licenses/agpl.txt>.
 *
 */

package moba.server.actionhandler;

import moba.server.datatypes.collections.SwitchStateMap;
import moba.server.datatypes.objects.*;
import moba.server.exceptions.ClientErrorException;
import moba.server.repositories.SwitchStateRepository;
import moba.server.routing.router.SimpleRouter;
import moba.server.routing.router.routinglistitems.Block;
import moba.server.routing.router.routinglistitems.Route;
import moba.server.routing.router.routinglistitems.RoutingElementInterface;
import moba.server.routing.typedefs.SwitchStateData;
import moba.server.utilities.layout.ActiveTrackLayout;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Collections;
import java.util.List;

final public class TrainRunner {

    private final SimpleRouter routing;

    private final ActionListGenerator generator;

    private final List<TrainJourney> trainQueue = Collections.synchronizedList(new LinkedList<>());

    private final InterlockBlock interlockBlock;

    private final InterlockRoute interlockRoute;

    private final SwitchStateRepository repo;

    private final ActiveTrackLayout activeLayout;

    /*
     *  TODO: Hier muss auch der Bahnübergang mit berücksichtigt werden (Function-GlobalPortAddressData in Environment)
     *        Anmerkung: Bahnübergang ist keine Funktion sondern wie eine Weiche zu behandeln
     *  TODO: Kreuzungsweichen!!
     *  TODO: Für Weichen und Bahnübergang benötigen wir benötigen wir noch ein Feedback, das alle Weichen und
     *        Bahnübergänge geschaltet wurden. Bahnübergang benötigt Zeit!
     */
    public TrainRunner(
        SimpleRouter routing,
        InterlockBlock interlock,
        InterlockRoute interlockRoute,
        SwitchStateRepository repo,
        ActionListGenerator generator,
        ActiveTrackLayout activeLayout
    ) {
        this.generator = generator;
        this.interlockBlock = interlock;
        this.interlockRoute = interlockRoute;
        this.repo = repo;
        this.routing = routing;
        this.activeLayout = activeLayout;
    }

    // Aufruf nach Fahrplan
    public void pushTrain(TrainJourney train) {
        // Train already exists in the list, this train has to be handled first
        if(insertAfterIfAlreadyExist(train)) {
            return;
        }

        if(handleTrain(train)) {
            return;
        }
        trainQueue.add(train);
    }

    // Aufruf nach jeder Action
    public void pushTrain() {

        trainQueue.removeIf(this::handleTrain);

        // FIXME: handleTrain: 3 Rückgabewerte
        // throw new IllegalStateException("Deadlock detected!");
    }

    public void releaseRoute(int routeId)
    throws SQLException, ClientErrorException {
        interlockRoute.releaseRoute(routeId);
        pushTrain();
    }

    public void releaseBlock(int blockId)
    throws SQLException, ClientErrorException {
        interlockBlock.releaseBlock(blockId);
        pushTrain();
    }

    public void setSwitched(int routeId) {
        interlockRoute.routeSet(routeId);
        pushTrain();
    }

    public boolean trainsToHandle() {
        return !trainQueue.isEmpty();
    }

    /**
     * Check if a train already exists in the linked list. If so, insert right after the last occurrence
     */
    private boolean insertAfterIfAlreadyExist(TrainJourney trainJourney) {
        ListIterator<TrainJourney> iterator = trainQueue.listIterator();

        // Check if the train is already in the list...
        while(iterator.hasPrevious()) {
            TrainJourney element = iterator.previous();

            if(element.train().address() != trainJourney.train().address()) {
                continue;
            }

            int index = iterator.previousIndex();

            if(index == -1) {
                break;
            }

            // ... train was found: Put it right behind the last occurrence
            // TODO: Check this: is this really '+1' ?
            trainQueue.add(index + 1, trainJourney);
            return true;
        }
        return false;
    }

    private boolean handleTrain(TrainJourney trainJourney) {
        ArrayList<Long> blocks = new ArrayList<>();
        try {
            ArrayList<RoutingElementInterface> routingElements = routing.getRoute(trainJourney);

            // Zug befindet sich bereits im Ziel!
            if(routingElements.isEmpty()) {
                return true;
            }

            for(RoutingElementInterface element : routingElements) {
                if(element instanceof Route) {
                    if(!handleSwitchingList(((Route)element).switchingList(), trainJourney.train().trainId())) {
                        generator.sendBlockActionList(trainJourney.train(), blocks);
                        return false;
                    }
                } else {
                    if(!interlockBlock.setBlock(trainJourney.train().trainId(), ((Block)element).id())) {
                        generator.sendBlockActionList(trainJourney.train(), blocks);
                        return false;
                    }
                    blocks.add(((Block)element).id());
                }
            }
        } catch(Throwable e) {
            // Predicat "trainQueue.removeIf(this::handleTrain);" darf keine checked Exceptions werfen!
            throw new RuntimeException(e);
        }

        // Zug kann bis zum Ziel fahren
        generator.sendBlockActionList(trainJourney.train(), blocks);
        return true;
    }

    private boolean handleSwitchingList(ArrayList<SwitchStateData> switchingList, long trainId)
    throws SQLException, ClientErrorException {
        long routeId = activeLayout.getActiveLayout();

        switch(interlockRoute.setRoute(trainId, switchingList)) {
            case NOT_BLOCKED:
            case BLOCKED_AND_NOT_SWITCHED_WAITING:
                return false;

            case BLOCKED_AND_SWITCHED:
                return true;

            case BLOCKED_AND_NOT_SWITCHED:
                break;

            default:
                throw new IllegalStateException("Unknown SwitchStateMap status!");
        }

        SwitchStateMap list = repo.getSwitchStateListForRoute(routeId);

        switchingList.removeIf(t->list.get(t.id()).stand() == t.switchStand());

        if(switchingList.isEmpty()) {
            interlockRoute.removeRoute(trainId);
            return true;
        }

        generator.sendSwitchActionList(trainId, switchingList);
        return false;
    }
}
