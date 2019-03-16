/*
 *  Project:    moba-server
 *
 *  Copyright (C) 2019 Stefan Paproth <pappi-@gmx.de>
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

public class Terminator implements NodeI {

    protected NodeI in;
    protected Train train;

    public Terminator(NodeI in, Train train) {
        this.in = in;
        this.train = train;
    }

    public Terminator(NodeI in) {
        this(in, null);
    }

    public Terminator() {
        this(null, null);
    }

    public void setInNode(NodeI node) {
        in = node;
    }

    @Override
    public NodeI getJunctionNode(NodeI node) throws NodeException {
        if(node == in) {
            return null;
        }
        throw new NodeException("invalid node given!");
    }
}


