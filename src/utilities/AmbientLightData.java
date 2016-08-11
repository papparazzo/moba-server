/*
 *  AppServer
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

package utilities;

import java.io.*;
import java.util.*;
import json.*;
import json.streamwriter.*;

public class AmbientLightData implements JSONToStringI {
    protected int red = 0;
    protected int blue = 0;
    protected int white = 0;

    public AmbientLightData() {

    }

    public AmbientLightData(int red, int blue, int white) {
        this.setRed(red);
        this.setBlue(blue);
        this.setWhite(white);
    }

    public final void setRed(int red) {
        this.checkValue(red);
        this.red = red;
    }

    public final void setBlue(int blue) {
        this.checkValue(blue);
        this.blue = blue;
    }

    public final void setWhite(int white) {
        this.checkValue(white);
        this.white = white;
    }

    protected void checkValue(int val) {
        if(red < 0 || red > 100) {
            throw new IllegalArgumentException("value is less 0 or greate 100");
        }
    }

    @Override
    public String toJsonString(boolean formated, int indent)
    throws JSONException, IOException {
        HashMap<String, Object> map = new HashMap<>();
        map.put("red",   this.red);
        map.put("blue",  this.blue);
        map.put("white", this.white);

        StringBuilder sb = new StringBuilder();
        JSONStreamWriterStringBuilder jsb = new JSONStreamWriterStringBuilder(sb);
        JSONEncoder encoder = new JSONEncoder(jsb, formated);
        encoder.encode(map, indent);
        return sb.toString();
    }
}
