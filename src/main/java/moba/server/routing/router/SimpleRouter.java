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

package moba.server.routing.router;

import moba.server.datatypes.enumerations.SwitchStand;
import moba.server.datatypes.objects.TrainJourney;
import moba.server.routing.nodes.BlockNode;
import moba.server.routing.nodes.NodeInterface;
import moba.server.routing.typedefs.BlockNodeMap;
import moba.server.routing.typedefs.SwitchStateData;

/**
 * Liefert eine verkettete Liste vom Startblock bis zum Zielblock mit Weichen und Blöcken zurück
 *     - Weichen müssen hier ggf. noch richtig gestellt und gesperrt werden
 *     - Blöcke müssen geprüft werden, ob diese frei sind!
 * Anmerkung: Bei Blöcken ist der SwitchStand auf null gesetzt
 * TODO: - Unterscheidung zwischen Bahnhofs / Ziel- und Streckenblöcke -> hier kann der Zug halten, Block mitten
 *         auf der Strecke kann kein Ziel sein
 *       - Im Schattenbahnhof soll es eine Gruppe von Zielblöcken geben. D.h. der Zug fährt dort irgendwo auf ein
 *         Gleis ein.
 */
final public class SimpleRouter {

    private final BlockNodeMap blocks;

    public SimpleRouter(BlockNodeMap blocks) {
        this.blocks = blocks;
    }

    public RoutingListItem getRoute(TrainJourney journey) {
        long fromBlock = journey.departureBlockId();
        long toBlock = journey.destinationBlockId();

        BlockNode block = blocks.get(fromBlock);

        if(block == null) {
            throw new IllegalArgumentException("start-block <" + fromBlock + "> not found");
        }

        if(toBlock == fromBlock) {
            // Zug befindet sich bereits in diesem Block.
            return null;
        }

        RoutingListItem itemL = fetchNextNode(block, block.getIn(), journey);

        // TODO: Hier noch die Fahrtrichtung berücksichtigen...
        //if(journey.train().noDirectionalControl()) {

        //}

        RoutingListItem itemR = fetchNextNode(block, block.getOut(), journey);

        if(itemL == null && itemR == null) {
            throw new IllegalArgumentException("no route found from <" + fromBlock + "> to <" + toBlock + ">");
        }

        if(itemL == null) {
            return itemR;
        }

        if(itemR == null) {
            return itemL;
        }

        if(itemL.getCount() > itemR.getCount()) {
            return itemR;
        }
        return itemL;
    }

    private RoutingListItem fetchNextNode(NodeInterface origin, NodeInterface next, TrainJourney destination) {
        if(next == null) {
            return null;
        }

        if(next.getId() == destination.departureBlockId()) {
            return null;
        }

        if(!next.trainAllowed(destination.train())) {
            return null;
        }

        if(next.getId() == destination.destinationBlockId()) {
            return new RoutingListItem(null, new SwitchStateData(next.getId(), null));
        }

        var ctrS = fetchNextNode(next, next.getJunctionNode(SwitchStand.STRAIGHT, origin), destination);

        if(next instanceof BlockNode) {
            return ctrS == null ? null : new RoutingListItem(ctrS, new SwitchStateData(next.getId(), null));
        }

        var ctrB = fetchNextNode(next, next.getJunctionNode(SwitchStand.BEND, origin), destination);

        if(ctrS == null && ctrB == null) {
            return null;
        }

        if(ctrS == null) {
            return new RoutingListItem(ctrB, new SwitchStateData(next.getId(), SwitchStand.BEND));
        }

        if(ctrB == null) {
            return new RoutingListItem(ctrS, new SwitchStateData(next.getId(), SwitchStand.STRAIGHT));
        }

        if(ctrS.getCount() > ctrB.getCount()) {
            return new RoutingListItem(ctrB, new SwitchStateData(next.getId(), SwitchStand.BEND));
        }
        return new RoutingListItem(ctrS, new SwitchStateData(next.getId(), SwitchStand.STRAIGHT));
    }
}
