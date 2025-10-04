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

import moba.server.datatypes.objects.*;
import moba.server.exceptions.ClientErrorException;
import moba.server.routing.router.RoutingList;
import moba.server.routing.router.SimpleRouter;

import java.sql.SQLException;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Vector;

// FIXME: ThreadSafety!!!
final public class TrainRunner {

    private final SimpleRouter routing;

    private final ActionListGenerator generator;

    private final LinkedList<TrainJourney> trainQueue = new LinkedList<>();

    private final Interlock interlock;

    // TODO: Hier muss auch der Bahnübergang mit berücksichtigt werden (Function-Address in Environment)
    // TODO: Kreuzungsweichen!!

    public TrainRunner(SimpleRouter routing, Interlock interlock, ActionListGenerator generator) {
        this.generator = generator;
        this.interlock = interlock;
        this.routing = routing;
    }

    /**
     * TODO Aufruf nach Fahrplan
     */
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

    /**
     * TODO Aufruf nach jeder Action
     */
    public void pushTrain() {

        trainQueue.removeIf(this::handleTrain);

        // FIXME: handleTrain: 3 Rückgabewerte
       // throw new IllegalStateException("Deadlock detected!");
    }

    public void releaseRoute(int routeId)
    throws SQLException, ClientErrorException {
        interlock.releaseRoute(routeId);
        pushTrain();
    }

    public void releaseBlock(int trainId, int blockId)
    throws SQLException, ClientErrorException {
        interlock.releaseBlock(trainId, blockId);
        pushTrain();
    }

    public void setSwitched(int routeId) {
        interlock.routeSet(routeId);
        pushTrain();
    }

    /**
     * Check if a train already exists in the linked list. If so, insert right after the last occurrence
     */
    private boolean insertAfterIfAlreadyExist(TrainJourney trainDestination) {
        ListIterator<TrainJourney> iterator = trainQueue.listIterator();

        // Check if the train is already in the list...
        while (iterator.hasPrevious()) {
            TrainJourney element = iterator.previous();

            if(element.train().address() != trainDestination.train().address()) {
                continue;
            }

            int index = iterator.previousIndex();

            if(index == -1) {
                break;
            }

            // ... train was found: Put it right behind the last occurrence
            // TODO: Check this: is this really '+1' ?
            trainQueue.add(index + 1, trainDestination);
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

        Vector<Long> switchingList = new Vector<>();

        while (current.successor() != null) {
            current = current.successor();

            // Es handelt sich um einen Block!
            if(current.routingItem().switchStand() == null) {
                try {
                    // FIXME: Hier brauchen wir die TrainId!
                    boolean okay = interlock.setBlock(trainDestination.train().address(), current.routingItem().id());

                    if(!okay) {
                        // TODO: Vorletzte id in TRainJourney setzen!
                        return false;
                    }
                } catch (Throwable e) {
                    // Predicat "trainQueue.removeIf(this::handleTrain);" darf keine checked Exceptions werfen!
                    throw new RuntimeException(e);
                }
                continue;
            }
        }

        //return generator.getTrainActionList(trainDestination, itemList);

        /*
        trainData.destinationBlockId();
        trainData.trainId();
        */

        // Zug ist am Ziel: return true, sonst false!
        return true;
    }

    private boolean handleBlockList(RoutingList current)
    throws SQLException {
        int trainId = 0;



        return true;
    }

    private boolean handleSwitchingList(RoutingList current)
    throws SQLException {
        // FIXME: Wo bekommen wir die Zug-Id her?
        int trainId = 0;
        int routeId = 4;

        Vector<Long> switchingList = new Vector<>();

        while (current.successor() != null) {
            RoutingList tmp = current.successor();

            if(current.routingItem().switchStand() != null) {
                break;
            }
            switchingList.add(current.routingItem().id());
            current = tmp;
        }

        // schauen ob Weichen bereits geschaltet...

        if(interlock.setRoute(trainId, switchingList)) {
            return false;
        }
        ActionListCollection collection = generator.getSwitchActionList(routeId, switchingList);
        return true;
    }
}
