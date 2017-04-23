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

import datatypes.base.Percent;
import java.util.Map;
import json.JSONEncoder;
import json.JSONException;
import json.JSONToStringI;
import json.streamwriter.JSONStreamWriterStringBuilder;

public class AmbientLightData implements JSONToStringI {
    protected Percent red;
    protected Percent blue;
    protected Percent green;
    protected Percent white;

    public AmbientLightData() {
    }

    public AmbientLightData(Percent red, Percent blue, Percent green, Percent white) {
        this.setRed(red);
        this.setBlue(blue);
        this.setGreen(green);
        this.setWhite(white);
    }

    public final void setRed(Percent red) {
        this.red = red;
    }

    public final void setBlue(Percent blue) {
        this.blue = blue;
    }

    public final void setGreen(Percent green) {
        this.green = green;
    }

    public final void setWhite(Percent white) {
        this.white = white;
    }

    public void fromJsonObject(Map<String, Object> map) {
        this.red = new Percent((int)map.get("red"));
        this.blue = new Percent((int)map.get("blue"));
        this.green = new Percent((int)map.get("green"));
        this.white = new Percent((int)map.get("white"));
    }

    @Override
    public String toJsonString(boolean formated, int indent)
    throws JSONException, IOException {
        HashMap<String, Object> map = new HashMap<>();
        map.put("red",   this.red);
        map.put("blue",  this.blue);
        map.put("green", this.green);
        map.put("white", this.white);

        StringBuilder sb = new StringBuilder();
        JSONStreamWriterStringBuilder jsb = new JSONStreamWriterStringBuilder(sb);
        JSONEncoder encoder = new JSONEncoder(jsb, formated);
        encoder.encode(map, indent);
        return sb.toString();
    }
}
