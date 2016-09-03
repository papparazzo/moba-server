/*
 *  moba-appServer
 *
 *  Copyright (C) 2016 stefan
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

import datatypes.base.DayTime;
import datatypes.base.Time;
import java.util.Map;
import json.JSONEncoder;
import json.JSONException;
import json.JSONToStringI;
import json.streamwriter.JSONStreamWriterStringBuilder;

public class GlobalTimerData implements JSONToStringI {
    protected DayTime curModelTime = new DayTime();
    protected int     multiplicator = 240;


    public int getMultiplicator() {
        return this.multiplicator;
    }

    public void setMultiplicator(int multiplicator)
    throws IllegalArgumentException {
        if(multiplicator > 3600) {
            multiplicator = 3600;
        }
        if(multiplicator % 15 != 0) {
            throw new IllegalArgumentException(
                "modulo 15 check failed in multiplicator-setting"
            );
        }
        this.multiplicator = multiplicator;
    }

    public DayTime getModelTime() {
        return this.curModelTime;
    }

    public void setModelTime(DayTime modelTime) {
        this.curModelTime = modelTime;
    }

    public void fromJsonObject(Map<String, Object> map) {
        this.curModelTime = new DayTime((String)map.get("curModelTime"));
        this.multiplicator = (int)(long)map.get("multiplicator");
    }

    public boolean setTick() {
        this.curModelTime.setValue(
            (this.curModelTime.getValue() + this.multiplicator) % (60 * 60 * 24 * 7)
        );
        return this.curModelTime.isFullHour();
    }

    public boolean isTimeBetween(Time start, Time end) {
        int time = this.curModelTime.getValue() ;
        time %= (60 * 60 * 24);
        if(start.getValue() < time && end.getValue() > time) {
            return true;
        }
        return false;
    }

    @Override
    public String toJsonString(boolean formated, int indent)
    throws JSONException, IOException {
        HashMap<String, Object> map = new HashMap<>();
        map.put("curModelTime",  this.curModelTime);
        map.put("multiplicator", this.multiplicator);

        StringBuilder sb = new StringBuilder();
        JSONStreamWriterStringBuilder jsb = new JSONStreamWriterStringBuilder(sb);
        JSONEncoder encoder = new JSONEncoder(jsb, formated);
        encoder.encode(map, indent);
        return sb.toString();
    }
}
