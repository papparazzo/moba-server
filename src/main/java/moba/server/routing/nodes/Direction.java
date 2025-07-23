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

    private final int direction;

    Direction(int direction) {
        this.direction = direction;
    }

    public int getDirection() {
        return direction;
    }

    public static Direction fromId(int direction) {
        for(Direction type : values()) {
            if(type.direction == direction) {
                return type;
            }
        }
        throw new IllegalArgumentException("unknown direction [" + direction + "].");
    }

    public Direction next() {
        return this == UNSET ? TOP : values()[this.ordinal() + 1];
    }

    public Direction previous() {
        return this == UNSET ? BOTTOM : values()[this.ordinal() - 1];
    }

    public Direction next(int steps) {
        Direction dir = this;
        for(int i = 0; i < steps; i++) {
            dir = dir.next();
        }
        return dir;
    }

    public Direction previous(int steps) {
        Direction dir = this;
        for(int i = 0; i < steps; i++) {
            dir = dir.previous();
        }
        return dir;
    }

    /**
    * Die Distanz zwischen zwei Verbindungspunkten muss mindestens 3 Bits betragen, damit
    * zwei 2 Verbindungspunkte (auch als Teil einer Weiche) ein gültiges Gleis bilden.
    * Zu einem Verbindungspunkt "dirIn" kommen nur 3 mögliche Verbindungspunkte "dirOut" infrage:
    * 1. Der komplementäre Verbindungspunkt (also ein gerades Gleis)
    * 2. Der komplementäre Verbindungspunkt + 1 Bit (also rechts gebogenes Gleis)
    * 3. Der komplementäre Verbindungspunkt - 1 Bit (also links gebogenes Gleis)
    *
    * @return DistanceType
    */
    public DistanceType getDistanceType(Direction dirOut) {
        var dirInCom = getComplementaryDirection();

        if(dirOut == dirInCom) {
            return DistanceType.STRAIGHT;
        }

        if(dirOut == dirInCom.next()) {
            return DistanceType.RIGHT_BEND;
        }

        if(dirOut == dirInCom.previous()) {
            return DistanceType.LEFT_BEND;
        }

        return DistanceType.INVALID;
    }

    public Direction getComplementaryDirection() {
        return switch(this) {
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
}