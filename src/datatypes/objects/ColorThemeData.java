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

package datatypes.objects;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import datatypes.base.Time;
import datatypes.enumerations.ThreeState;
import json.JSONEncoder;
import json.JSONException;
import json.JSONToStringI;
import json.streamwriter.JSONStreamWriterStringBuilder;

public class ColorThemeData implements JSONToStringI {

    protected Time dimTime;
    protected Time brightTime;
    protected ThreeState condition;

    public ColorThemeData()
    throws IllegalArgumentException {
        this.dimTime = new Time(9, 0);
        this.brightTime = new Time(21, 0);
        this.condition = ThreeState.AUTO;
    }

    public Time getDimTime() {
        return this.dimTime;
    }

    public Time getBrightTime() {
        return this.brightTime;
    }

    public ThreeState getColorThemeCondition() {
        return this.condition;
    }

    public void setColorThemeChangeTimes(
        Time dimTime, Time brightTime, ThreeState condition
    )
    throws IllegalArgumentException {
        if(dimTime == null || brightTime == null) {
            throw new IllegalArgumentException();
        }
        this.dimTime = dimTime;
        this.brightTime = brightTime;
        this.condition = condition;
    }

    public void fromJsonObject(Map<String, Object> map) {
        this.dimTime = new Time((String)map.get("dimTime"));
        this.brightTime = new Time((String)map.get("brightTime"));
        this.condition = ThreeState.getValue((String)map.get("condition"), this.condition);
    }

    @Override
    public String toJsonString(boolean formated, int indent)
    throws JSONException, IOException {
        HashMap<String, Object> map = new HashMap<>();
        map.put("dimTime",    this.dimTime);
        map.put("brightTime", this.brightTime);
        map.put("condition",  this.condition);

        StringBuilder sb = new StringBuilder();
        JSONStreamWriterStringBuilder jsb = new JSONStreamWriterStringBuilder(sb);
        JSONEncoder encoder = new JSONEncoder(jsb, formated);
        encoder.encode(map, indent);
        return sb.toString();
    }
}
