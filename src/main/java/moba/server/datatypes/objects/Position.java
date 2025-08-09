/*
 *  Project:    moba-server
 *
 *  Copyright (C) 2022 Stefan Paproth <pappi-@gmx.de>
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

package moba.server.datatypes.objects;

import moba.server.routing.nodes.Direction;

import java.util.Objects;

final public class Position {

    private long x;
    private long y;

    public Position(long x, long y) {
        if(x < 0 || y < 0) {
            throw new IllegalArgumentException("Position must be >= 0");
        }

        this.x = x;
        this.y = y;
    }

    public Position(Position pos) {
        this(pos.x, pos.y);
    }

    public Position() {
        this(0, 0);
    }

    public long getX() {
        return x;
    }

    public long getY() {
        return y;
    }

    @Override
    public String toString() {
        return "Position{x=" + x + ", y=" + y + '}';
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) {
            return true;
        }
        if(o == null || getClass() != o.getClass()) {
            return false;
        }
        Position position = (Position) o;
        return x == position.x && y == position.y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    public Position getDistance(Position pos) {
        return new Position(pos.x - x, pos.y - y);
    }

    public Position grow(Position pos) {
        x = Math.max(pos.x, x);
        y = Math.max(pos.y, y);
        return new Position(x, y);
    }

    /**
     * setzt den Cursor (Position) in die Richtung, welche mit Direction
     * angegeben ist. Beispiel: Direction RIGHT → x einen weiter nach rechts
     */
    public void setNewPosition(int dir) {
        switch(dir) {
            case Direction.UNSET:
                return;

            case Direction.TOP_RIGHT:
                x++;  // fall-through

            case Direction.TOP:
                y--;
                break;

            case Direction.BOTTOM_RIGHT:
                y++; // fall-through

            case Direction.RIGHT:
                x++;
                return;

            case Direction.BOTTOM:
                y++;
                return;

            case Direction.BOTTOM_LEFT:
                y++;  // fall-through

            case Direction.LEFT:
                x--;
                break;

            case Direction.TOP_LEFT:
                y--;
                x--;
                break;
        }
    }
}

