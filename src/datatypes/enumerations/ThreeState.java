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

public enum ThreeState implements JSONToStringI {
    ON,
    OFF,
    AUTO,
    UNSET;

    protected final int value;

    public static ThreeState getValue(ThreeState t, ThreeState def) {
        if(t == ThreeState.UNSET) {
            return def;
        }
        return t;
    }

    public static ThreeState getValue(boolean val) {
        if(val) {
            return ThreeState.ON;
        }
        return ThreeState.OFF;
    }

    public static ThreeState getValue(String s, ThreeState def) {
        ThreeState t = ThreeState.valueOf(s);
        if(t == ThreeState.UNSET) {
            return def;
        }
        return t;
    }

    private ThreeState() {
        this.value = ordinal();
    }

    @Override
    public String toJsonString(boolean formated, int indent)
    throws JSONException, IOException {
        StringBuilder b = new StringBuilder();
        b.append('"');
        b.append(ThreeState.values()[this.value].toString());
        b.append('"');
        return b.toString();
    }
}
