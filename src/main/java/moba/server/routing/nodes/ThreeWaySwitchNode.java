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

public class ThreeWaySwitchNode extends Node {

    protected Node in;
    protected Node outStraight;
    protected Node outBendLeft;
    protected Node outBendRight;

    public ThreeWaySwitchNode(int id, SwitchStand switchStand) {
        super(id, switchStand);
    }

    public ThreeWaySwitchNode(int id) {
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
                outBendLeft = node;
                return;

            case TOP_RIGHT:
                outBendRight = node;
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
        if(node != in && node != outStraight && node != outBendLeft && node != outBendRight) {
            throw new NodeException("invalid node given!");
        }

        if(node == in && currentState == SwitchStand.BEND_2) {
            return outBendLeft;
        }

        if(node == in && currentState == SwitchStand.BEND_1) {
            return outBendRight;
        }

        if(node == in) {
            return outStraight;
        }

        if(node == outBendLeft && currentState == SwitchStand.BEND_2) {
            return in;
        }

        if(node == outBendRight && currentState == SwitchStand.BEND_1) {
            return in;
        }

        if(node == outStraight && (currentState == SwitchStand.STRAIGHT_1 || currentState == SwitchStand.STRAIGHT_2)) {
            return in;
        }

        return null;
    }

    @Override
    public Node getJunctionNode(Direction dir)
    throws NodeException {
        return switch(dir) {
            case TOP       -> outStraight;
            case TOP_LEFT  -> outBendLeft;
            case TOP_RIGHT -> outBendRight;
            case BOTTOM    -> in;
            default        -> throw new NodeException("invalid direction given!");
        };
    }
}
