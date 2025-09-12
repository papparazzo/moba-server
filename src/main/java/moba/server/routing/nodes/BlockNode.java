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
import moba.server.datatypes.objects.Train;
import moba.server.routing.Direction;

final public class BlockNode extends AbstractNode {

    // TODO: Limitations!
    //       Güterzug
    //       Nahverkehr
    //       Fernverkehr
    //       Oberleitung
    //       Rangierzug

    private NodeInterface in = null;
    private NodeInterface out = null;

    private Train train = null;

    public BlockNode(long id) {
        super(id, SwitchStand.STRAIGHT);
    }

    public void setTrain(Train train) {
        this.train = train;
    }

    public Train getTrain() {
        return train;
    }

    @Override
    public void setJunctionNode(int dir, NodeInterface node)
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

    public NodeInterface getJunctionNode(SwitchStand switchStand, NodeInterface node)
    throws NodeException {
        if(switchStand != SwitchStand.STRAIGHT) {
            return null;
        }

        if(node == in) {
            return out;
        }
        if(node == out) {
            return in;
        }
        throw new NodeException("invalid node given!");
    }

    public NodeInterface getIn() {
        return in;
    }

    public NodeInterface getOut() {
        return out;
    }

    public void turn(SwitchStand stand) {
        throw new NodeException("cannot turn block node!");
    }

/*
    boolean isOut(NodeInterface b) {
        return b == out;
    }

     boolean isBlocked() {
        return train != null;
    }

    BlockNode pushTrain() {
        if(!isBlocked()) {
            throw new NodeException("block not blocked!");
        }

        BlockNode nextBlock;

        if(train.drivingDirection() == DrivingDirection.FORWARD) {
            nextBlock = getNextBlock(out);
        } else {
            nextBlock = getNextBlock(in);
        }

        if(nextBlock != null) {
            nextBlock.setTrain(train);
            train = null;
        }
        return nextBlock;
    }


    BlockNode getNextBlock(NodeInterface nextNode) {
        if(nextNode == null) {
            return null;
        }

        NodeInterface curNode = this;
        NodeInterface afterNextNode;

        while(true) {
            afterNextNode = nextNode.getJunctionNode(curNode);
            if(afterNextNode == null) {
                return null;
            }

            var nextBlock = (BlockNode)(nextNode);
            if(nextBlock != null) {
                if(nextBlock.isBlocked()) {
                    return null;
                }

                if(train != null) {
                    return nextBlock;
                }
                if(nextBlock.isOut(curNode)) {
                    train = train.withDrivingDirection(DrivingDirection.BACKWARD);
                } else {
                    train = train.withDrivingDirection(DrivingDirection.FORWARD);
                }
                return nextBlock;
            }
            curNode = nextNode;
            nextNode = afterNextNode;
        }
    }*/
};
