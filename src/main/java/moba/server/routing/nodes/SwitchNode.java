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

package moba.server.routing.nodes;

import moba.server.datatypes.enumerations.SwitchStand;
import moba.server.routing.Direction;

final public class SwitchNode extends AbstractNode {

    private NodeInterface in;
    private NodeInterface outStraight;
    private NodeInterface outBend;

    public SwitchNode(long id, SwitchStand switchStand) {
        super(id, switchStand);
    }

    @Override
    public void setJunctionNode(int dir, NodeInterface node)
    throws NodeException {
        switch(dir) {
            case Direction.TOP:
                outStraight = node;
                return;

            case Direction.TOP_LEFT:
            case Direction.TOP_RIGHT:
                outBend = node;
                return;

            case Direction.BOTTOM:
                in = node;
                return;

            default:
                throw new NodeException("invalid direction given!");
        }
    }

    @Override
    public NodeInterface getJunctionNode(SwitchStand switchStand, NodeInterface node)
    throws NodeException {
        if(node != in && node != outStraight && node != outBend) {
            throw new NodeException("invalid node given!");
        }

        if(node == outStraight && switchStand == SwitchStand.STRAIGHT) {
            return in;
        }

        if(node == outBend && switchStand == SwitchStand.BEND) {
            return in;
        }

        if(node == in && switchStand == SwitchStand.BEND) {
            return outBend;
        }

        if(node == in && switchStand == SwitchStand.STRAIGHT) {
            return outStraight;
        }

        return null;
    }

    public void turn(SwitchStand stand) {
        currentState = stand;
    }
}
