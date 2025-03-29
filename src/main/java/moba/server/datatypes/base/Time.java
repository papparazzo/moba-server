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

public class Time {
    protected int time;

    public Time() {
    }

    public Time(int time)
    throws IllegalArgumentException {
        setTime(time);
    }

    public final void setTime(int time)
    throws IllegalArgumentException {
        if(time < 0 || time > ((60 * 24) - 1)) {
            throw new IllegalArgumentException();
        }
        this.time = time;
    }

    public final boolean appendTime(int time)
    throws IllegalArgumentException {
        if(time < 1) {
            throw new IllegalArgumentException("invalid time diff given");
        }

        this.time = (this.time + time) % (60 * 24);
        return this.time < time;
    }

    public int getTime() {
        return time;
    }

    public boolean isFullHour() {
        return (time % 60 == 0);
    }
}
