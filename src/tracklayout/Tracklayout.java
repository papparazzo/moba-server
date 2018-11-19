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

package tracklayout;

import datatypes.base.Point;

public class Tracklayout {

    protected Symbol[][] layout;

    protected int height;
    protected int width;

    public Tracklayout(int height, int width) {
        this.height = height;
        this.width = width;

        layout = new Symbol[width][height];
    }

    public void addSymbol(int x, int y, Symbol symbol) {
        if(x >= width || y >= height) {
            throw new ArrayIndexOutOfBoundsException();
        }
        layout[x][y] = new Symbol();
    }

    public Point getStartPoint() {
        for(int y = 0; y < height; ++y) {
            for(int x = 0; x < width; ++x) {
                if(layout[x][y] != null) {
                    return new Point(x, y);
                }
            }
        }
        throw new ArrayIndexOutOfBoundsException("No start found");
    }

    public boolean validate() {
        return false;
    }

}
