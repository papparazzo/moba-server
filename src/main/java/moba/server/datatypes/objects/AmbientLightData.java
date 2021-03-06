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

import java.io.IOException;
import java.util.HashMap;

import java.util.Map;
import moba.server.json.JSONEncoder;
import moba.server.json.JSONException;
import moba.server.json.JSONToStringI;
import moba.server.json.streamwriter.JSONStreamWriterStringBuilder;

public final class AmbientLightData implements JSONToStringI {
    protected long red;
    protected long blue;
    protected long green;
    protected long white;

    public AmbientLightData() {
    }

    public AmbientLightData(long red, long blue, long green, long white) {
        setRed(red);
        setBlue(blue);
        setGreen(green);
        setWhite(white);
    }

    public void setRed(long val) {
        red = validateValue(val);
    }

    public long getRed() {
        return red;
    }

    public void setBlue(long val) {
        blue = validateValue(val);
    }

    public long getBlue() {
        return blue;
    }

    public void setGreen(long val) {
        green = validateValue(val);
    }

    public long getGreen() {
        return green;
    }

    public void setWhite(long val) {
        white = validateValue(val);
    }

    public long getWhite() {
        return white;
    }

    public void fromJsonObject(Map<String, Object> map) {
        setRed((long)map.get("red"));
        setBlue((long)map.get("blue"));
        setGreen((long)map.get("green"));
        setWhite((long)map.get("white"));
    }

    @Override
    public String toJsonString(boolean formated, int indent)
    throws JSONException, IOException {
        HashMap<String, Object> map = new HashMap<>();
        map.put("red",   red);
        map.put("blue",  blue);
        map.put("green", green);
        map.put("white", white);

        StringBuilder sb = new StringBuilder();
        JSONStreamWriterStringBuilder jsb = new JSONStreamWriterStringBuilder(sb);
        JSONEncoder encoder = new JSONEncoder(jsb, formated);
        encoder.encode(map, indent);
        return sb.toString();
    }

    public final long validateValue(long val)
    throws IllegalArgumentException {
        if(val > 4095 || val < 0) {
            throw new IllegalArgumentException(String.format("Val <%d> out of range", val));
        }
        return val;
    }
}
