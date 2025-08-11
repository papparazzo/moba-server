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

package moba.server.routing.typedefs;

import moba.server.datatypes.objects.Position;

final public class LocationVector {

    private final Position position;
    private int direction;

    public LocationVector(LocationVector vector) {
        this(new Position(vector.position), vector.direction);
    }

    public LocationVector(Position position, int direction) {
        this.position = position;
        this.direction = direction;
    }

    public void step() {
        position.setNewPosition(direction);
    }

    public void setDirection(int dir) {
        this.direction = dir;
    }

    public Position getPosition() {
        return position;
    }

    public int getDirection() {
        return direction;
    }
}
