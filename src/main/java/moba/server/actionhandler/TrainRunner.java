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

import java.sql.SQLException;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Vector;

// FIXME: ThreadSafety!!!
final public class TrainRunner {

    private final SimpleRouter routing;

    private final ActionListGenerator generator;

    private final LinkedList<TrainJourney> trainQueue = new LinkedList<>();

    private final InterlockBlock interlockBlock;

    private final InterlockRoute interlockRoute;

    private final SwitchStateRepository repo;

    /*
     *  TODO: Hier muss auch der Bahnübergang mit berücksichtigt werden (Function-GlobalPortAddressData in Environment)
     *        Anmerkung: Bahnübergang ist keine Funktion sondern wie eine Weiche zu behandeln
     *  TODO: Kreuzungsweichen!!
     *  TODO: Für Weichen und Bahnübergang benötigen wir benötigen wir noch ein Feedback, das alle Weichen und
     *        Bahnübergänge geschaltet wurden. Bahnübergang benötigt Zeit!
     */
    public TrainRunner(SimpleRouter routing, InterlockBlock interlock, InterlockRoute interlockRoute, SwitchStateRepository repo, ActionListGenerator generator) {
        this.generator = generator;
        this.interlockBlock = interlock;
        this.interlockRoute = interlockRoute;
        this.repo = repo;
        this.routing = routing;
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

    public void releaseBlock(int trainId, int blockId)
    throws SQLException, ClientErrorException {
        interlockBlock.releaseBlock(trainId, blockId);
        pushTrain();
    }

    public void setSwitched(int routeId) {
        interlockRoute.routeSet(routeId);
        pushTrain();
    }

    /**
     * Check if a train already exists in the linked list. If so, insert right after the last occurrence
     */
    private boolean insertAfterIfAlreadyExist(TrainJourney trainJourney) {
        ListIterator<TrainJourney> iterator = trainQueue.listIterator();

        // Check if the train is already in the list...
        while (iterator.hasPrevious()) {
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

    private boolean handleTrain(TrainJourney trainDestination) {
        try {
            Vector<RoutingElementInterface> routingElements = routing.getRoute(trainDestination);

            Vector<Long> blocks = new Vector<>();
          //  generator.sendBlockActionList(trainDestination.train(), blocks);


            // Zug befindet sich bereits im Ziel!
            if(routingElements.isEmpty()) {
                return true;
            }

            for (RoutingElementInterface element : routingElements) {
                if(element instanceof Route) {
                    if(!handleSwitchingList(((Route)element).switchingList(), trainDestination.train().trainId())) {
                        return false;
                    }
                } else {
                    if(!interlockBlock.setBlock(trainDestination.train().trainId(), ((Block)element).id())) {
                        return false;
                    }
                    blocks.add(((Block)element).id());
                }
            }
        } catch (Throwable e) {
            // Predicat "trainQueue.removeIf(this::handleTrain);" darf keine checked Exceptions werfen!
            throw new RuntimeException(e);
        }

        // Zug ist am Ziel
        return true;
    }

    private boolean handleSwitchingList(Vector<SwitchStateData> switchingList, int trainId)
    throws SQLException, ClientErrorException {
        // FIXME: Die routeId muss hier noch gesetzt werden!
        int routeId = 4;

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

        generator.sendSwitchActionList(routeId, switchingList);
        return false;
    }
}
