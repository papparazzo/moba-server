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

public class BlockNode extends Node {

    protected Node in = null;
    protected Node out = null;

    public BlockNode(int id) {
        super(id);
    }

    public BlockNode(int id, TrainData train) {
        super(id);
        this.train = train;
    }

    public void setTrain(TrainData train) {
        this.train = train;
    }

    public TrainData getTrain() {
        return train;
    }

    @Override
    public void setJunctionNode(Direction dir, Node node)
    throws NodeException {
        switch(dir) {
            case TOP:
            case TOP_RIGHT:
            case RIGHT:
            case BOTTOM_RIGHT:
                out = node;
                return;

            case BOTTOM:
            case BOTTOM_LEFT:
            case LEFT:
            case TOP_LEFT:
                in = node;
                return;
        }
        throw new NodeException("invalid direction given!");
    }

    @Override
    Node getJunctionNode(Node node)
    throws NodeException {
        if(node == in) {
            return out;
        }
        if(node == out) {
            return in;
        }
        throw new NodeException("invalid node given!");
    }

    @Override
    Node getJunctionNode(Direction dir)
    throws NodeException {
        return switch(dir) {
            case TOP, TOP_RIGHT, RIGHT, BOTTOM_RIGHT -> out;
            case BOTTOM, BOTTOM_LEFT, LEFT, TOP_LEFT -> in;
            default -> throw new NodeException("invalid direction given!");
        };
    }
};
