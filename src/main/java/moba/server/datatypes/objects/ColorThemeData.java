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

import java.util.Map;

import moba.server.datatypes.base.Time;
import moba.server.datatypes.enumerations.ThreeState;

public class ColorThemeData {

    protected Time dimTime;
    protected Time brightTime;
    protected ThreeState condition;

    public ColorThemeData()
    throws IllegalArgumentException {
        dimTime = new Time(9, 0);
        brightTime = new Time(21, 0);
        condition = ThreeState.AUTO;
    }

    public Time getDimTime() {
        return dimTime;
    }

    public Time getBrightTime() {
        return brightTime;
    }

    public ThreeState getCondition() {
        return condition;
    }

    public void setDimTime(Time value) {
        if(value == null) {
            throw new IllegalArgumentException();
        }
        dimTime = value;
    }

    public void setBrightTime(Time value) {
        if(value == null) {
            throw new IllegalArgumentException();
        }
        brightTime = value;
    }

    public void setCondition(ThreeState value) {
        condition = value;
    }

    public void fromJsonObject(Map<String, Object> map) {
        dimTime = new Time((String)map.get("dimTime"));
        brightTime = new Time((String)map.get("brightTime"));
        condition = ThreeState.getValue((String)map.get("condition"), condition);
    }
}
