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
import moba.server.utilities.exceptions.ClientErrorException;

public class GlobalTimerData {
    protected PointOfTime modelTime = new PointOfTime();

    protected int  multiplicator = 4;

    protected Time nightStartTime = new Time();
    protected Time sunriseStartTime = new Time();
    protected Time dayStartTime = new Time();
    protected Time sunsetStartTime = new Time();

    public void setModelTime(PointOfTime val) {
        modelTime = val;
    }

    public PointOfTime getModelTime() {
        return modelTime;
    }

    public boolean setTick() {
        return modelTime.setTick(multiplicator);
    }

    public void setMultiplicator(int multiplicator)
    throws IllegalArgumentException {
        if(multiplicator < 1 || multiplicator > 4) {
            throw new IllegalArgumentException("multiplicator out of range (< 1 || > 4)");
        }
        this.multiplicator = multiplicator;
    }

    public int getMultiplicator() {
        return multiplicator;
    }

    public int getNightStartTime() {
        return nightStartTime.getTime();
    }

    public void setNightStartTime(int val) {
        nightStartTime = new Time(val);
    }

    public int getSunriseStartTime() {
        return sunriseStartTime.getTime();
    }

    public void setSunriseStartTime(int val) {
        sunriseStartTime = new Time(val);
    }

    public int getDayStartTime() {
        return dayStartTime.getTime();
    }

    public void setDayStartTime(int val) {
        dayStartTime = new Time(val);
    }

    public int getSunsetStartTime() {
        return sunsetStartTime.getTime();
    }

    public void setSunsetStartTime(int val) {
        sunsetStartTime = new Time(val);
    }

    public void fromJsonObject(Map<String, Object> map)
    throws ClientErrorException {
        @SuppressWarnings("unchecked")
        var pt = (Map<String, Object>)map.get("modelTime");

        modelTime.setDay(CheckedEnum.getFromString(Day.class, (String)pt.get("day")));
        modelTime.setTime(new Time(((Long)pt.get("time")).intValue()));

        setMultiplicator(((Long)map.get("multiplicator")).intValue());

        setNightStartTime(((Long)map.get("nightStartTime")).intValue());
        setSunriseStartTime(((Long)map.get("sunriseStartTime")).intValue());
        setDayStartTime(((Long)map.get("dayStartTime")).intValue());
        setSunsetStartTime(((Long)map.get("sunsetStartTime")).intValue());
        validate();
    }

    protected void validate()
    throws ClientErrorException {

        if(sunriseStartTime.getTime() > dayStartTime.getTime()) {
            throw new ClientErrorException(ClientError.INVALID_VALUE_GIVEN, "Tag vor Sonnenaufgang");
        }

        if(dayStartTime.getTime() > sunsetStartTime.getTime()) {
            throw new ClientErrorException(ClientError.INVALID_VALUE_GIVEN, "Sonnenuntergang vor Tag");
        }

        if(sunsetStartTime.getTime() > nightStartTime.getTime()) {
            throw new ClientErrorException(ClientError.INVALID_VALUE_GIVEN, "Nacht vor Sonnenuntergang");
        }
    }
}
