/*
 *  Project:    moba-server
 *
 *  Copyright (C) 2021 Stefan Paproth <pappi-@gmx.de>
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

// TODO: das muss hier raus aus dem Verzeichnis!
package moba.server.datatypes.enumerations;

import moba.server.routing.Direction;

public enum SymbolType {
    END              (Direction.TOP),
    STRAIGHT         (Direction.TOP | Direction.BOTTOM),
    RIGHT_SWITCH     (Direction.TOP | Direction.BOTTOM | Direction.TOP_RIGHT),
    CROSS_OVER_SWITCH(Direction.TOP | Direction.BOTTOM | Direction.TOP_RIGHT | Direction.BOTTOM_LEFT),
    LEFT_SWITCH      (Direction.TOP | Direction.BOTTOM | Direction.TOP_LEFT),
    THREE_WAY_SWITCH (Direction.TOP | Direction.BOTTOM | Direction.TOP_LEFT | Direction.TOP_RIGHT),
    CROSS_OVER       (Direction.TOP | Direction.BOTTOM | Direction.RIGHT | Direction.LEFT),
    BEND             (Direction.TOP | Direction.BOTTOM_LEFT);

    private final int value;

    SymbolType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static SymbolType fromId(int id) {
        for(SymbolType type : values()) {
            if(type.value == id) {
                return type;
            }
        }
        throw new IllegalArgumentException("unknown direction id [" + id + "].");
    }
}
