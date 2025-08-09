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
    public void setJunctionNode(int dir, Node node)
    throws NodeException {
        switch(dir) {
            case Direction.TOP:
            case Direction.TOP_RIGHT:
            case Direction.RIGHT:
            case Direction.BOTTOM_RIGHT:
                out = node;
                return;

            case Direction.BOTTOM:
            case Direction.BOTTOM_LEFT:
            case Direction.LEFT:
            case Direction.TOP_LEFT:
                in = node;
                return;
        }
        throw new NodeException("invalid direction given!");
    }

    @Override
    public Node getJunctionNode(Node node)
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
    public Node getJunctionNode(int dir)
    throws NodeException {
        return switch(dir) {
            case Direction.TOP, Direction.TOP_RIGHT, Direction.RIGHT, Direction.BOTTOM_RIGHT -> out;
            case Direction.BOTTOM, Direction.BOTTOM_LEFT, Direction.LEFT, Direction.TOP_LEFT -> in;
            default -> throw new NodeException("invalid direction given!");
        };
    }
};
