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
 *  along with this program. If not, see <https://www.gnu.org/licenses/agpl.txt>.
 *
 */

package moba.server.datatypes.objects;

import moba.server.datatypes.base.Time;
import java.util.Map;

import moba.server.datatypes.enumerations.ClientError;
import moba.server.datatypes.enumerations.Day;
import moba.server.utilities.CheckedEnum;
import moba.server.exceptions.ClientErrorException;

public class GlobalTimerData {
    protected PointInTime modelTime = new PointInTime();

    protected int  multiplicator = 4;

    protected Time nightStartTime = new Time();
    protected Time sunriseStartTime = new Time();
    protected Time dayStartTime = new Time();
    protected Time sunsetStartTime = new Time();

    public void setModelTime(PointInTime val) {
        modelTime = val;
    }

    public PointInTime getModelTime() {
        return modelTime;
    }

    public boolean setTick() {
        return modelTime.setTick(multiplicator);
    }

    public void setMultiplicator(int multiplicator)
    throws IllegalArgumentException {
        if(multiplicator != 1 && multiplicator != 2 && multiplicator != 4) {
            throw new IllegalArgumentException("multiplicator out of range (only 1, 2 and 4 are allowed)");
        }
        this.multiplicator = multiplicator;
    }

    public int getMultiplicator() {
        return multiplicator;
    }

    public Time getNightStartTime() {
        return nightStartTime;
    }

    public void setNightStartTime(Time val) {
        nightStartTime = val;
    }

    public Time getSunriseStartTime() {
        return sunriseStartTime;
    }

    public void setSunriseStartTime(Time val) {
        sunriseStartTime = val;
    }

    public Time getDayStartTime() {
        return dayStartTime;
    }

    public void setDayStartTime(Time val) {
        dayStartTime = val;
    }

    public Time getSunsetStartTime() {
        return sunsetStartTime;
    }

    public void setSunsetStartTime(Time val) {
        sunsetStartTime = val;
    }

    public static GlobalTimerData fromJsonObject(Map<String, Object> map)
    throws ClientErrorException {
        @SuppressWarnings("unchecked")
        var pt = (Map<String, Object>)map.get("modelTime");

        GlobalTimerData gtd = new GlobalTimerData();

        gtd.modelTime.setDay(CheckedEnum.getFromString(Day.class, (String)pt.get("day")));
        gtd.modelTime.setTime(new Time(((Long)pt.get("time")).intValue()));

        gtd.setMultiplicator(((Long)map.get("multiplicator")).intValue());

        gtd.setNightStartTime(new Time((String)map.get("nightStartTime")));
        gtd.setSunriseStartTime(new Time((String)map.get("sunriseStartTime")));
        gtd.setDayStartTime(new Time((String)map.get("dayStartTime")));
        gtd.setSunsetStartTime(new Time((String)map.get("sunsetStartTime")));
        gtd.validate();

        return gtd;
    }

    protected void validate()
    throws ClientErrorException {

        if(sunriseStartTime.getTimeInMinutes() > dayStartTime.getTimeInMinutes()) {
            throw new ClientErrorException(ClientError.INVALID_VALUE_GIVEN, "Tag vor Sonnenaufgang");
        }

        if(dayStartTime.getTimeInMinutes() > sunsetStartTime.getTimeInMinutes()) {
            throw new ClientErrorException(ClientError.INVALID_VALUE_GIVEN, "Sonnenuntergang vor Tag");
        }

        if(sunsetStartTime.getTimeInMinutes() > nightStartTime.getTimeInMinutes()) {
            throw new ClientErrorException(ClientError.INVALID_VALUE_GIVEN, "Nacht vor Sonnenuntergang");
        }
    }
}
