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

package moba.server.datatypes.objects;

import moba.server.datatypes.base.Time;
import moba.server.datatypes.enumerations.Day;

public class PointOfTime {
    protected Day  day = Day.SUNDAY;
    protected Time time = new Time();

    public void setDay(Day val) {
        day = val;
    }

    public void setTime(int val) {
        time = new Time(val);
    }

    public Day getDay() {
        return day;
    }

    public int getTime() {
        return time.getTime();
    }

    public boolean setTick(int multiplicator) {
        if(time.appendTime(multiplicator)) {
            day = day.next();
        }
        return time.isFullHour();
    }
}
