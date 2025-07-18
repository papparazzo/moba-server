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

public class CrossOverSwitchNode extends Node {

    protected Node outTop;
    protected Node outRight;
    protected Node inBottom;
    protected Node inLeft;

    public CrossOverSwitchNode(int id) {
        super(id);
    }

    @Override
    void setJunctionNode(Direction dir, Node node)
    throws NodeException {
        switch(dir) {
            case TOP:
                outTop = node;
                return;

            case TOP_RIGHT:
                outRight = node;
                return;

            case BOTTOM:
                inBottom = node;
                return;

            case BOTTOM_LEFT:
                inLeft = node;
                return;
        }
        throw new NodeException("invalid direction given!");
    }

    @Override
    Node getJunctionNode(Node node)
    throws NodeException {
        if(node != outTop && node != outRight && node != inBottom && node != inLeft) {
            throw new NodeException("invalid node given!");
        }

        Node activeIn = getInNode();
        Node activeOut = getOutNode();

        if(node == activeIn) {
            return activeOut;
        }

        if(node == activeOut) {
            return activeIn;
        }

        return null;
    }

    @Override
    Node getJunctionNode(Direction dir)
    throws NodeException {
        return switch(dir) {
            case TOP -> outTop;
            case TOP_RIGHT -> outRight;
            case BOTTOM -> inBottom;
            case BOTTOM_LEFT -> inLeft;
            default -> throw new NodeException("invalid direction given!");
        };
    }

    Node getInNode() {
        return switch(currentState) {
            case BEND_1, BEND_2 -> outTop;
            case STRAIGHT_1, STRAIGHT_2 -> outRight;
        };
    }

    Node getOutNode() {
        return switch(currentState) {
            case BEND_1, STRAIGHT_1 -> inBottom;
            case BEND_2, STRAIGHT_2 -> inLeft;
        };
    }
}
