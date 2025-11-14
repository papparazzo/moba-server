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
import moba.server.routing.router.RoutingList;
import moba.server.routing.router.SimpleRouter;
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
     *  TODO: Hier muss auch der Bahnübergang mit berücksichtigt werden (Function-GlobalPortAddress in Environment)
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
        RoutingList current = routing.getRoute(trainDestination);

        // Zug befindet sich bereits im Ziel!
        if(current == null) {
            return true;
        }

        try {
            while (current.successor() != null) {
                current = current.successor();

                // Es handelt sich um Weichenliste!
                if(current.routingItem().switchStand() != null) {
                    if(handleSwitchingList(current, trainDestination.train().trainId())) {
                        return false;
                    }
                }
                // TODO: Achtung! Signal auf freie Fahrt schalten
                boolean okay = interlockBlock.setBlock(trainDestination.train().trainId(), current.routingItem().id());

                if(!okay) {
                    // TODO: Vorletzte id in TrainJourney setzen!
                    return false;
                }
            }
        } catch (Throwable e) {
            // Predicat "trainQueue.removeIf(this::handleTrain);" darf keine checked Exceptions werfen!
            throw new RuntimeException(e);
        }


        //return generator.getTrainActionList(trainDestination, itemList);

        /*
        trainData.destinationBlockId();
        trainData.trainId();
        */

        // Zug ist am Ziel: return true, sonst false!
        return true;
    }

    private boolean handleSwitchingList(RoutingList current, int trainId)
    throws SQLException, ClientErrorException {
        // FIXME: Die routeId muss hier noch gesetzt werden!
        int routeId = 4;

        Vector<SwitchStateData> switchingList = new Vector<>();

        while (current.successor() != null) {
            RoutingList tmp = current.successor();
            SwitchStateData data = current.routingItem();

            if(data.switchStand() == null) {
                break;
            }

            switchingList.add(data);
            current = tmp;
        }

        // schauen, ob Weichen bereits geschaltet wurden …
        if(interlockRoute.setRoute(trainId, switchingList) == InterlockRoute.RouteStatus.NOT_BLOCKED) {
            return false;
        }




        SwitchStateMap list = repo.getSwitchStateListForRoute(routeId);


        // FIXME: Wenn alle Weichen bereits geschaltet wurden brauchen wir auch nicht zu warten!
        ActionListCollection collection = generator.getSwitchActionList(routeId, toSwitchList);

        if(list.isEmpty()) {
            interlockRoute.routeSet(trainId);
        }

        return true;
    }


}
