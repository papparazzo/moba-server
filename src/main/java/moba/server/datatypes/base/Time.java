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

package moba.server.datatypes.base;

import java.io.IOException;

import moba.server.json.JSONException;
import moba.server.json.JSONToStringI;

public class Time implements JSONToStringI {
    protected int value;

    public Time() {
    }

    public Time(int val)
    throws IllegalArgumentException {
        setValue(val);
    }

   public Time(int hour, int minute)
    throws IllegalArgumentException {
        if(hour < 0 || hour > 24 || minute < 0 || minute > 59) {
            throw new IllegalArgumentException();
        }
        value = hour * 60 * 60;
        value += minute * 60;
    }

    public Time(String val)
    throws IllegalArgumentException {
        setValue(val);
    }

    public final void setValue(int val)
    throws IllegalArgumentException {
        if(val < 0 || val > ((60 * 60 * 24) - 1)) {
            throw new IllegalArgumentException();
        }
        value = val;
    }

    public final void setValue(String val)
    throws IllegalArgumentException {
        String[] tokens = val.split(":");

        value = Integer.parseInt(tokens[0]) * 60 * 60;
        value += Integer.parseInt(tokens[1]) * 60;
    }

    public int getValue() {
        return value;
    }

    @Override
    public String toString() {
        long t = value / 60;
        long m = t % 60;
        t /= 60;
        long h = t % 24;

        StringBuilder sb = new StringBuilder();
        if(h < 10) {
            sb.append("0");
        }
        sb.append(h);
        sb.append(":");
        if(m < 10) {
            sb.append("0");
        }
        sb.append(m);
        return sb.toString();
    }

    @Override
    public String toJsonString(boolean formated, int indent)
    throws JSONException, IOException {
        StringBuilder b = new StringBuilder();
        b.append('"');
        b.append(toString());
        b.append('"');
        return b.toString();
    }
}
