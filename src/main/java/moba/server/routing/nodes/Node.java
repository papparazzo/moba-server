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

    protected SwitchStand currentState;

    public Node(SwitchStand switchStand) {
        this.currentState = switchStand;
    }

    public Node() {
        this(SwitchStand.STRAIGHT_1);
    }

    abstract public Node getJunctionNode(Node node) throws NodeException;
    abstract public void setJunctionNode(int dir, Node node) throws NodeException;

    void turn(SwitchStand stand) {
        currentState = stand;
    }
}
