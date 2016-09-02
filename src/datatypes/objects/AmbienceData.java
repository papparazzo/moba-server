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
import java.util.Map;

import datatypes.enumerations.Switch;
import datatypes.enumerations.ToggleState;
import json.JSONEncoder;
import json.JSONException;
import json.JSONToStringI;
import json.streamwriter.JSONStreamWriterStringBuilder;

public class AmbienceData implements JSONToStringI {
    protected boolean curtainUp = false;
    protected boolean mainLightOn = false;

    public void fromJsonObject(Map<String, Object> map) {
        this.curtainUp = ToggleState.getValue((Switch)map.get("curtainUp"), this.curtainUp);
        this.mainLightOn = ToggleState.getValue((Switch)map.get("mainLightOn"), this.mainLightOn);
    }

    @Override
    public String toJsonString(boolean formated, int indent)
    throws JSONException, IOException {
        HashMap<String, Object> map = new HashMap<>();
        map.put("curtainUp",   ToggleState.getValue(this.curtainUp));
        map.put("mainLightOn", ToggleState.getValue(this.mainLightOn));

        StringBuilder sb = new StringBuilder();
        JSONStreamWriterStringBuilder jsb = new JSONStreamWriterStringBuilder(sb);
        JSONEncoder encoder = new JSONEncoder(jsb, formated);
        encoder.encode(map, indent);
        return sb.toString();
    }
}
