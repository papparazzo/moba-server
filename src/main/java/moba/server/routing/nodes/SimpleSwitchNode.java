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

public class SimpleSwitchNode extends Node {

    protected Node in;
    protected Node outStraight;
    protected Node outBend;

    public SimpleSwitchNode(int id) {
        super(id);
    }

    @Override
    public void setJunctionNode(Direction dir, Node node)
    throws NodeException {
        switch(dir) {
            case TOP:
                outStraight = node;
                return;

            case TOP_LEFT:
            case TOP_RIGHT:
                outBend = node;
                return;

            case BOTTOM:
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

    @Override
    public Node getJunctionNode(Direction dir)
    throws NodeException {
        return switch(dir) {
            case TOP                 -> outStraight;
            case TOP_LEFT, TOP_RIGHT -> outBend;
            case BOTTOM              -> in;
            default                  -> throw new NodeException("invalid direction given!");
        };
    }
}
