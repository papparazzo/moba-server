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

package moba.server.routing;

import java.util.Vector;

public class Router {

    // Liefert alle gesperrten Block-Ids zurück
    public Vector<Integer> getRoute(int fromBlock, int toBlock) {
        Vector<Integer> v = new Vector<>();

        switch(toBlock) {
            case 411:
                v.add(424);
                v.add(467);
                v.add(411);
                break;
            case 296:
                v.add(411);
                v.add(318);
                v.add(218);
                v.add(296);
                break;
            case 424:
                v.add(296);
                v.add(424);
                break;
        }
        return v;
    }
}
