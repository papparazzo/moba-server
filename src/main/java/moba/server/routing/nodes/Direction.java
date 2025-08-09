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

public final class Direction {
    public static final int UNSET        =   0;
    public static final int TOP          =   1;
    public static final int TOP_RIGHT    =   2;
    public static final int RIGHT        =   4;
    public static final int BOTTOM_RIGHT =   8;

    public static final int BOTTOM       =  16;
    public static final int BOTTOM_LEFT  =  32;
    public static final int LEFT         =  64;
    public static final int TOP_LEFT     = 128;

    public static int getComplementaryDirection(int direction) {
        return switch(direction) {
            case TOP -> BOTTOM;
            case TOP_RIGHT -> BOTTOM_LEFT;
            case RIGHT -> LEFT;
            case BOTTOM_RIGHT -> TOP_LEFT;
            case BOTTOM -> TOP;
            case BOTTOM_LEFT -> TOP_RIGHT;
            case LEFT -> RIGHT;
            case TOP_LEFT -> BOTTOM_RIGHT;
            default -> UNSET;
        };
    }

    public static int shift(int direction, int steps) {
        if(direction == UNSET) {
            return UNSET;
        }

        for(int i = 0; i < steps; i++) {
            direction = ((direction << 1) | (direction >> 7)) & 255;
        }
        return direction;
    }
}

