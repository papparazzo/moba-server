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
 *  along with this program. If not, see <https://www.gnu.org/licenses/agpl.txt>.
 *
 */

package moba.server.datatypes.base;

import moba.server.json.JSONException;
import moba.server.json.JSONToStringI;

import java.io.IOException;

public class Time implements JSONToStringI {
    protected int timeInMinutes;

    public Time() {
    }

    public Time(int time)
    throws IllegalArgumentException {
        setTimeInMinutes(time);
    }

    public final void setTimeInMinutes(int timeInMinutes)
    throws IllegalArgumentException {
        if(timeInMinutes < 0 || timeInMinutes > ((60 * 24) - 1)) {
            throw new IllegalArgumentException();
        }
        this.timeInMinutes = timeInMinutes;
    }

    public final boolean appendTime(int minutes)
    throws IllegalArgumentException {
        if(minutes < 1) {
            throw new IllegalArgumentException("invalid time diff given");
        }

        timeInMinutes = (timeInMinutes + minutes) % (60 * 24);
        return timeInMinutes < minutes;
    }

    public boolean hasDayChange(int minutes) {
        return ((timeInMinutes + minutes) % (60 * 24)) < minutes;
    }

    public int getTimeInMinutes() {
        return timeInMinutes;
    }

    public String getTime() {
        return getTime(0);
    }

    public String getTime(int offsetInMinutes) {
        int timeInMinutes = this.timeInMinutes + offsetInMinutes;

        int hours = timeInMinutes / 60;
        int minutes = timeInMinutes % 60;
        return String.format("%02d:%02d", hours, minutes);
    }

    public boolean isFullHour() {
        return (timeInMinutes % 60 == 0);
    }

    @Override
    public String toString() {
        return getTime();
    }

    @Override
    public String toJsonString(boolean formated, int indent)
    throws JSONException, IOException {
        return '"' + getTime() + '"';
    }
}
