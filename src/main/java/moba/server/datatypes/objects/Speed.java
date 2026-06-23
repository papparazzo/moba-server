/*
 *  Project:    moba-server
 *
 *  Copyright (C) 2026 Stefan Paproth <pappi-@gmx.de>
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

public record Speed(
    int maximum,
    int reduced,
    int shunting
) {
    public Speed {
        if(maximum < 0 || maximum < reduced) {
            throw new IllegalArgumentException("illegal speed values: <" + maximum + " " + reduced + ">");
        }

        if(reduced < 0 || reduced < shunting) {
            throw new IllegalArgumentException("illegal speed values: <" + maximum + " " + reduced + ">");
        }

        if(shunting < 0) {
            throw new IllegalArgumentException("illegal speed values: <" + maximum + " " + reduced + ">");
        }
    }

    public Speed(int maximum) {
        this(maximum, maximum, maximum);
    }
}
