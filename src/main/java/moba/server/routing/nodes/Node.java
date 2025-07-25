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

public abstract class Node {

    protected int id;
    protected SwitchStand currentState;

    public Node(int id, SwitchStand switchStand) {
        this.id = id;
        this.currentState = switchStand;
    }

    public Node(int id) {
        this(id, SwitchStand.STRAIGHT_1);
    }

    abstract Node getJunctionNode(Node node) throws NodeException;
    abstract Node getJunctionNode(Direction dir) throws NodeException;
    abstract void setJunctionNode(Direction dir, Node node) throws NodeException;

    void turn(SwitchStand stand) {
        currentState = stand;
    }

    int getId() {
        return id;
    }
}
