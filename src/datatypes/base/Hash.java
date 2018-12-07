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

package datatypes.base;

import java.io.IOException;

import json.JSONException;
import json.JSONToStringI;

public class Hash implements JSONToStringI {
    protected String value;

    public Hash() {

    }

    public Hash(String val)
    throws IllegalArgumentException {
        setValue(val);
    }

    public final void setValue(String val)
    throws IllegalArgumentException {
        if(!val.matches("[A-F0-9]{64}")) {
            throw new IllegalArgumentException();
        }
        value = val;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public String toJsonString(boolean formated, int indent)
    throws JSONException, IOException {
        StringBuilder b = new StringBuilder();
        b.append('"');
        b.append(value);
        b.append('"');
        return b.toString();
    }
}
