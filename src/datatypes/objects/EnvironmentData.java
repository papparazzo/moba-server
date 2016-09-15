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

import datatypes.enumerations.Switch;
import json.JSONEncoder;
import json.JSONException;
import json.JSONToStringI;
import json.streamwriter.JSONStreamWriterStringBuilder;

public class EnvironmentData implements JSONToStringI {
    protected Switch thunderStorm = Switch.OFF;
    protected Switch environmentSound = Switch.OFF;
    protected Switch wind = Switch.OFF;
    protected Switch rain = Switch.OFF;
    protected Switch aux01 = Switch.OFF;
    protected Switch aux02 = Switch.OFF;
    protected Switch aux03 = Switch.OFF;

    public void fromJsonObject(Map<String, Object> map) {
        this.thunderStorm = Switch.getValue((String)map.get("thunderStorm"), this.thunderStorm);
        this.environmentSound = Switch.getValue((String)map.get("environmentSound"), this.environmentSound);
        this.wind =  Switch.getValue((String)map.get("wind"), this.wind);
        this.rain = Switch.getValue((String)map.get("rain"), this.rain);
        this.aux01 = Switch.getValue((String)map.get("aux01"), this.aux01);
        this.aux02 = Switch.getValue((String)map.get("aux02"), this.aux02);
        this.aux03 = Switch.getValue((String)map.get("aux03"), this.aux03);
    }

    @Override
    public String toJsonString(boolean formated, int indent)
    throws JSONException, IOException {
        HashMap<String, Object> map = new HashMap<>();
        map.put("thunderStorm", this.thunderStorm);
        map.put("wind", this.wind);
        map.put("rain", this.rain);
        map.put("environmentSound", this.environmentSound);
        map.put("aux01", this.aux01);
        map.put("aux02", this.aux02);
        map.put("aux03", this.aux03);

        StringBuilder sb = new StringBuilder();
        JSONStreamWriterStringBuilder jsb = new JSONStreamWriterStringBuilder(sb);
        JSONEncoder encoder = new JSONEncoder(jsb, formated);
        encoder.encode(map, indent);
        return sb.toString();
    }
}
