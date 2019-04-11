/*
 *  Project:    moba-server
 *
 *  Copyright (C) 2019 Stefan Paproth <pappi-@gmx.de>
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
import java.util.Map;
import json.JSONException;
import json.JSONToStringI;

public class PositionData implements JSONToStringI {
    protected int xPos;
    protected int yPos;

    public PositionData(int x, int y) {
        xPos = x;
        yPos = y;
    }

    public void fromJsonObject(Map<String, Object> map) {
       xPos = (int)map.get("xPos");
       yPos = (int)map.get("yPos");
    }

    @Override
    public String toJsonString(boolean formated, int indent) throws JSONException, IOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
