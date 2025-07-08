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

public enum Direction {
    UNSET       (0),
    TOP         (1),
    TOP_RIGHT   (2),
    RIGHT       (4),
    BOTTOM_RIGHT(8),

    BOTTOM      (16),
    BOTTOM_LEFT (32),
    LEFT        (64),
    TOP_LEFT    (128);

    private final int weight;

    Direction(int weight) {
        this.weight = weight;
    }

    public int getWeight() {
        return weight;
    }

    public static Direction fromId(int id) {
        for(Direction type : values()) {
            if(type.weight == id) {
                return type;
            }
        }
        throw new IllegalArgumentException("unknown direction id [" + id + "].");
    }
}