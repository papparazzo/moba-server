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

package datatypes.base;

public class Direction {
    public static final int UNSET        = 0;

    public static final int TOP          = 1;
    public static final int TOP_RIGHT    = 2;
    public static final int RIGHT        = 4;
    public static final int BOTTOM_RIGHT = 8;

    public static final int BOTTOM       = 16;
    public static final int BOTTOM_LEFT  = 32;
    public static final int LEFT         = 64;
    public static final int TOP_LEFT     = 128;

    protected int direction = Direction.UNSET;

    public Direction() {

    }

    public Direction(int dir) {
        setDirection(dir);
    }

    public int getDirection() {
        return direction;
    }

    public final void setDirection(int dir) {
        switch(dir) {
            case TOP:
            case TOP_RIGHT:
            case RIGHT:
            case BOTTOM:
            case BOTTOM_LEFT:
            case LEFT:
            case TOP_LEFT:
                direction = dir;
                return;
        }
        throw new IllegalArgumentException();
    }

    public Direction getNextLeftDirection() {
        if(direction == TOP) {
            return new Direction(TOP_LEFT);
        }
        return new Direction(direction / 2);
    }

    public Direction getNextRightDirection() {
        if(direction == TOP_LEFT) {
            return new Direction(TOP);
        }
        return new Direction(direction * 2);
    }

    public Direction getComplementaryDirection() {
        if(direction == UNSET) {
            return new Direction(UNSET);
        }
        if(direction < BOTTOM) {
            return new Direction(direction * 16);
        }
        return new Direction(direction / 16);
    }

    public enum DistanceType {
        INVALID,
        STRAIGHT,
        BEND
    };

    /**
    * Die Distanz zwischen zwei Verbindungspunkte muss mindestens 3 Bit betragen, damit
    * zwei 2 Verbindungspunkte (auch als Teil einer Weiche) ein gültiges Gleis bilden.
    * Zu einem Verbindungspunkt dir1 kommen nur 3 mögliche Verbindungspunkte dir2 in Frage:
    * 1. Der komplemntäre Verbindungspunkt (also ein gerades Gleis)
    * 2. Der komplemntäre Verbindungspunkt + 1 Bit (also gebogenes Gleis)
    * 3. Der komplemntäre Verbindungspunkt - 1 Bit (also gebogenes Gleis)
    *
    * @param dir
    * @return DistanceType
    */
    public DistanceType getDistanceType(Direction dir) {
        if(dir.direction == direction) {
            return DistanceType.INVALID;
        }

        Direction dirc = dir.getComplementaryDirection();

        if(direction == dirc.direction) {
            return DistanceType.STRAIGHT;
        }

        if(direction == (dirc.direction * 2)) {
            return DistanceType.BEND;
        }

        // Sonderfall: TOP == 1 -> 1 / 2 = 0 -> müsste hier jedoch 128 sein!!
        if(dirc.direction == TOP && direction == TOP_LEFT) {
            return DistanceType.BEND;
        }

        if(direction == (dirc.direction / 2)) {
            return DistanceType.BEND;
        }

        return DistanceType.INVALID;
    }
}
