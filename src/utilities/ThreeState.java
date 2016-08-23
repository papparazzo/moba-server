/*
 *  AppServer
 *
 *  Copyright (C) 2015 stefan
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
package utilities;

import java.io.*;
import json.*;

public enum ThreeState implements JSONToStringI {
    ON,
    UNSET,
    OFF;

    protected final int value;

    public static boolean getValue(ThreeState t, boolean def) {
        switch(t) {
            case UNSET:
                return def;

            case ON:
                return true;

            case OFF:
                return false;

            default:
                throw new UnsupportedOperationException("Not supported yet.");
        }
    }

    public static boolean getValue(Switch s, boolean def) {
        switch(s) {
            case UNSET:
                return def;

            case ON:
                return true;

            case OFF:
                return false;

            default:
                throw new UnsupportedOperationException("Not supported yet.");
        }
    }

    public static ThreeState getValue(ThreeState t, ThreeState def) {
        if(t == ThreeState.UNSET) {
            return def;
        }
        return t;
    }

    public static ThreeState getValue(Switch s, ThreeState def) {
        switch(s) {
            case OFF:
                return ThreeState.OFF;

            case ON:
                return ThreeState.ON;

            case UNSET:
                return def;

            default:
                throw new UnsupportedOperationException("Not supported yet.");
        }
    }

    public static ThreeState getValue(boolean val) {
        if(val) {
            return ThreeState.ON;
        }
        return ThreeState.OFF;
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
