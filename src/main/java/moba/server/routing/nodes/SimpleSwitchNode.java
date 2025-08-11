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

final public class SimpleSwitchNode extends Node {

    private Node in;
    private Node outStraight;
    private Node outBend;

    public SimpleSwitchNode(SwitchStand switchStand) {
        super(switchStand);
    }

    @Override
    public void setJunctionNode(int dir, Node node)
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
    public Node getJunctionNode(Node node)
    throws NodeException {
        if(node != in && node != outStraight && node != outBend) {
            throw new NodeException("invalid node given!");
        }
        if(node == outStraight && (currentState == SwitchStand.STRAIGHT_1 || currentState == SwitchStand.STRAIGHT_2)) {
            return in;
        }

        if(node == outBend && (currentState == SwitchStand.BEND_1 || currentState == SwitchStand.BEND_2)) {
            return in;
        }

        if(node == in && (currentState == SwitchStand.BEND_1 || currentState == SwitchStand.BEND_2)) {
            return outBend;
        }

        if(node == in && (currentState == SwitchStand.STRAIGHT_1 || currentState == SwitchStand.STRAIGHT_2)) {
            return outStraight;
        }
        return null;
    }
}
