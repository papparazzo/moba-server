/*
 *  Project:    moba-server
 *
 *  Copyright (C) 2018 Stefan Paproth <pappi-@gmx.de>
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
 *  along with this program. If not, see <http://www.gnu.org/licenses/agpl.txt>.
 *
 */

package automode.node;

import automode.Train;
import automode.NodeException;

public class Block implements NodeI {

    protected NodeI in;
    protected NodeI out;

    protected Train train;

    public Block(NodeI in, NodeI out, Train train) {
        this.in = in;
        this.out = out;
        this.train = train;
    }

    public Block(NodeI in, NodeI out) {
        this(in, out, null);
    }

    public Block(NodeI in) {
        this(in, null, null);
    }

    public void setInNode(NodeI node) {
        in = node;
    }

    public void setOutNode(NodeI node) {
        out = node;
    }

    @Override
    public NodeI getJunctionNode(NodeI node) throws NodeException {
        if(node == in) {
            return out;
        }
        if(node == out) {
            return in;
        }
        throw new NodeException("invalid node given!");
    }
}


