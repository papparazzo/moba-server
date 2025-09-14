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
import moba.server.routing.router.RoutingListItem;
import moba.server.routing.router.SimpleRouter;

import java.util.LinkedList;
import java.util.Queue;

final public class TrainRunner {

    private final SimpleRouter routing;

    private final ActionListGenerator generator;

    private final Queue<TrainDestination> queue = new LinkedList<>();

    public TrainRunner(SimpleRouter routing, ActionListGenerator generator) {
        this.generator = generator;
        this.routing = routing;
    }

    public void pushTrain(TrainDestination train) {
        queue.offer(train);
        pushTrain();
    }

    /**
     * TODO Aufruf nach jeder Action
     */
    public void pushTrain() {

        TrainDestination trainData = queue.poll();

        if(trainData == null) {
            return;
        }

        if(handleTrain(trainData)) {
            return;
        }

        // FIXME: Was machen wir hier wenn ein Zug mit derselben ID bereits drinnen ist?
        //        Lösung: Verkette Liste! So kommt die Reihenfolge eines Zuges nicht durcheinander!
        queue.offer(trainData);
    }

    private boolean handleTrain(TrainDestination trainDestination) {


        RoutingListItem routingListItem = routing.getRoute(trainDestination);

        //return generator.getTrainActionList(trainDestination, itemList);

        /*
        trainData.destinationBlockId();
        trainData.trainId();
        */

        // Zug ist am Ziel: return true, sonst false!
        return false;
    }

/*
    private ActionListCollection getActionList(Train train, int toBlock) {
    }

    public boolean block(RoutingListItem itemList)
    throws SQLException {

        RoutingListItem current = itemList;
        while (current.successor() != null) {
            current = current.successor();

            if(block()) {
                return true;
            }
            else {
                return false;
            }

        }

        blockContacts.get();
        switchStates.get();

    }
*/

}
