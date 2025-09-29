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
import moba.server.datatypes.enumerations.TrainType;
import moba.server.datatypes.objects.Train;
import moba.server.routing.Direction;

import java.util.HashSet;
import java.util.Set;

final public class BlockNode extends AbstractNode {
    private final Set<TrainType> trainTypes;
    private final boolean hasCatenary;

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
        this(id, new HashSet<>(), false);
    }

    public BlockNode(long id, Set<TrainType> trainTypes, boolean hasCatenary) {
        super(id, SwitchStand.STRAIGHT);
        this.trainTypes = trainTypes;
        this.hasCatenary = hasCatenary;
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

    public boolean trainAllowed(Train train) {
        if(train.hasPantograph() && !hasCatenary) {
            return false;
        }

        return trainTypes.contains(train.trainType());
    }
};
