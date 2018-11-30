/*
 *  Project:    moba-server
 *
 *  Copyright (C) 2016 Stefan Paproth <pappi-@gmx.de>
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

package datatypes.base;

public class Position {

    protected int x = 0;
    protected int y = 0;

    public Position(int x, int y) {
        this.x = x;
        this.y = y;
    }

    int getX() {
        return x;
    }

    int getY() {
        return y;
    }

    public void setNewPosition(Direction d) {
        switch(d.direction) {
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
