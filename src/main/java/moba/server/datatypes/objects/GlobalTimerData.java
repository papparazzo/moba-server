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

package moba.server.datatypes.objects;

import moba.server.datatypes.enumerations.Day;
import moba.server.datatypes.base.Time;
import java.util.Map;

public class GlobalTimerData {
    protected Day  curModelDay;
    protected Time curModelTime = new Time();
    protected int  multiplicator = 240;

    public int getMultiplicator() {
        return multiplicator;
    }

    public void setMultiplicator(int multiplicator)
    throws IllegalArgumentException {
        if(multiplicator < 60 || multiplicator > 240) {
            throw new IllegalArgumentException("multiplicator out of range (< 60 || > 240)");
        }

        if(multiplicator % 30 != 0) {
            throw new IllegalArgumentException("multiplicator modulo 30 check failed in multiplicator-setting");
        }
        this.multiplicator = multiplicator;
    }

    public Day getCurModelDay() {
        return curModelDay;
    }

    public void setCurModelDay(Day modelDay) {
        curModelDay = modelDay;
    }

    public Time getCurModelTime() {
        return curModelTime;
    }

    public void setCurModelTime(Time modelTime) {
        curModelTime = modelTime;
    }

    public void fromJsonObject(Map<String, Object> map) {
        curModelDay = Day.valueOf((String)map.get("curModelDay"));
        curModelTime = new Time((String)map.get("curModelTime"));
        setMultiplicator(((Long)map.get("multiplicator")).intValue());
    }

    public boolean setTick() {
        if(curModelTime.appendTime(multiplicator)) {
            curModelDay = curModelDay.next();
        }
        return curModelTime.isFullHour();
    }

    public boolean isTimeBetween(Time start, Time end) {
        int time = curModelTime.getTime() ;
        time %= (60 * 60 * 24);
        if(start.getTime() < time && end.getTime() > time) {
            return true;
        }
        return false;
    }
}
