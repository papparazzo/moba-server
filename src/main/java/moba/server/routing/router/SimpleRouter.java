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
import moba.server.routing.nodes.BlockNode;
import moba.server.routing.nodes.NodeInterface;
import moba.server.routing.typedefs.BlockNodeMap;
import moba.server.routing.typedefs.SwitchStateData;

import java.util.Vector;

final public class SimpleRouter {

    private final BlockNodeMap blocks;

    private Vector<SwitchStateData> routeMap;

    public SimpleRouter(BlockNodeMap blocks) {
        this.blocks = blocks;
    }

    private RoutingListItem fetchNextNode(NodeInterface origin, NodeInterface next, long fromBlock, long toBlock) {
        if(next == null) {
            return null;
        }

        if(next.getId() == fromBlock) {
            return null;
        }

        if(next.getId() == toBlock) {
            return new RoutingListItem(null, new SwitchStateData(next.getId(), SwitchStand.STRAIGHT));
        }

        var ctrS = fetchNextNode(next, next.getJunctionNode(SwitchStand.STRAIGHT, origin), fromBlock, toBlock);
        var ctrB = fetchNextNode(next, next.getJunctionNode(SwitchStand.BEND, origin), fromBlock, toBlock);

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

    public RoutingListItem getRoute(long fromBlock, long toBlock) {

        BlockNode block = blocks.get(fromBlock);

        if(block == null) {
            throw new IllegalArgumentException("start-block <" + fromBlock + "> not found");
        }

        // TODO Hier noch die Fahrtrichtung berücksichtigen...
        RoutingListItem itemL = fetchNextNode(block, block.getIn(), fromBlock, toBlock);
        RoutingListItem itemR = fetchNextNode(block, block.getOut(), fromBlock, toBlock);

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
}
