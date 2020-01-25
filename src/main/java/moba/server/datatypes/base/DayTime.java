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

public class DayTime implements JSONToStringI {
    protected int value;

    public DayTime() {
        value = 0;
    }

    public DayTime(String day, int hour, int minute)
    throws IllegalArgumentException {
        if(hour < 0 || hour > 24 || minute < 0 || minute > 59) {
            throw new IllegalArgumentException();
        }
        value = getValOfDay(day);
        value += hour * 60 * 60;
        value += minute * 60;
    }

    public DayTime(int val)
    throws IllegalArgumentException {
        setValue(val);
    }

    public DayTime(String val)
    throws IllegalArgumentException {
        setValue(val);
    }

    public final void setValue(int val)
    throws IllegalArgumentException {
        if(val < 0 || val > ((7 * 60 * 60 * 24) - 1)) {
            throw new IllegalArgumentException();
        }
        value = val;
    }

    public final void setValue(String val)
    throws IllegalArgumentException {
        String tokens[] = val.split(" ");

        if(tokens.length != 2) {
            throw new IllegalArgumentException();
        }

        value = getValOfDay(tokens[0]);
        tokens = tokens[1].split(":");

        value += Integer.parseInt(tokens[0]) * 60 * 60;
        value += Integer.parseInt(tokens[1]) * 60;
    }

    protected final int getValOfDay(String val)
    throws IllegalArgumentException {
        switch(val) {
            case "So":
                return 0;

            case "Mo":
                return 60 * 60 * 24;

            case "Di":
                return 2 * 60 * 60 * 24;

            case "Mi":
                return 3 * 60 * 60 * 24;

            case "Do":
                return 4 * 60 * 60 * 24;

            case "Fr":
                return 5 * 60 * 60 * 24;

            case "Sa":
                return 6 * 60 * 60 * 24;

            default:
                throw new IllegalArgumentException();
        }
    }

    public int getValue() {
        return value;
    }

    public boolean isFullHour() {
        long f = value / 60;
        return (f % 60 == 0);
    }

    @Override
    public String toString() {
        long f = value / 60;
        long m = f % 60;
        f /= 60;
        long h = f % 24;
        f /= 24;
        StringBuilder sb = new StringBuilder();
        switch((int)f) {
            case 0:
                sb.append("So ");
                break;

            case 1:
                sb.append("Mo ");
                break;

            case 2:
                sb.append("Di ");
                break;

            case 3:
                sb.append("Mi ");
                break;

            case 4:
                sb.append("Do ");
                break;

            case 5:
                sb.append("Fr ");
                break;

            case 6:
                sb.append("Sa ");
                break;
        }
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
