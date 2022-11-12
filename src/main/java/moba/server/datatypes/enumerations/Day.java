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
 *  along with this program. If not, see <http://www.gnu.org/licenses/agpl.txt>.
 *
 */

package moba.server.datatypes.enumerations;

import java.io.IOException;
import moba.server.json.JSONException;
import moba.server.json.JSONToStringI;

public enum Day implements JSONToStringI {
    SUNDAY,
    MONDAY,
    TUESDAY,
    WEDNESDAY,
    THURSDAY,
    FRIDAY,
    SATURDAY;

    protected final int value;

    private Day() {
        value = ordinal();
    }

    public Day next() {
        if (ordinal() == values().length - 1) {
            return values()[0];
        }
        return values()[ordinal() + 1];
    }

    @Override
    public String toJsonString(boolean formated, int indent)
    throws JSONException, IOException {
        StringBuilder b = new StringBuilder();
        b.append('"');
        b.append(Day.values()[value].toString());
        b.append('"');
        return b.toString();
    }
}
