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

package moba.server.routing;

import moba.server.datatypes.enumerations.SwitchStand;
import moba.server.routing.nodes.BlockNode;
import moba.server.routing.nodes.NodeInterface;
import moba.server.routing.typedefs.BlockNodeMap;
import moba.server.routing.typedefs.SwitchStateData;

import java.util.Vector;

final public class Router {

    private final BlockNodeMap blocks;

    private Vector<SwitchStateData> routeMap;

    public Router(BlockNodeMap blocks) {
        this.blocks = blocks;
    }

    private boolean fetchNextNode(NodeInterface origin, NodeInterface next, long fromBlock, long toBlock)
    {
        if(next == null) {
            return false;
        }

        if(next.getId() == fromBlock) {
            return false;
        }

        if(next.getId() == toBlock) {
            routeMap.add(new SwitchStateData(next.getId(), SwitchStand.STRAIGHT));
            return true;
        }

        if(fetchNextNode(next, next.getJunctionNode(SwitchStand.STRAIGHT, origin), fromBlock, toBlock)) {
            routeMap.add(new SwitchStateData(next.getId(), SwitchStand.STRAIGHT));
            return true;
        }

        if(fetchNextNode(next, next.getJunctionNode(SwitchStand.BEND, origin), fromBlock, toBlock)) {
            routeMap.add(new SwitchStateData(next.getId(), SwitchStand.BEND));
            return true;
        }

        return false;
    }

    Vector<SwitchStateData> getRoute(long fromBlock, long toBlock) {
        routeMap = new Vector<>();

        BlockNode block = blocks.get(fromBlock);

        if(block == null) {
            throw new IllegalArgumentException("start-block <" + fromBlock + "> not found");
        }

        if(fetchNextNode(block, block.getIn(), fromBlock, toBlock)) {
            return routeMap;
        }

        if(fetchNextNode(block, block.getOut(), fromBlock, toBlock)) {
            return routeMap;
        }

        throw new IllegalArgumentException("no route found from <" + fromBlock + "> to <" + toBlock + ">");
    }
}
