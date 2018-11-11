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

package datatypes.enumerations;

import java.io.IOException;

import json.JSONException;
import json.JSONToStringI;

public enum Switch implements JSONToStringI {
    ON,
    AUTO,
    UNSET,
    TRIGGER,
    OFF;

    protected final int value;

    public static Switch getValue(String s, Switch def) {
        Switch t = Switch.valueOf(s);
        if(t == Switch.UNSET) {
            return def;
        }
        return t;
    }

    private Switch() {
        value = ordinal();
    }

    @Override
    public String toJsonString(boolean formated, int indent)
    throws JSONException, IOException {
        StringBuilder b = new StringBuilder();
        b.append('"');
        b.append(Switch.values()[value].toString());
        b.append('"');
        return b.toString();
    }
}
