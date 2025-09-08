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
 *  along with this program. If not, see <https://www.gnu.org/licenses/agpl.txt>.
 *
 */

package moba.server.datatypes.objects;

import java.util.HashMap;
import java.util.Map;

import moba.server.datatypes.enumerations.Switch;
import moba.server.json.JsonSerializerInterface;
import moba.server.exceptions.ClientErrorException;

public class EnvironmentData implements JsonSerializerInterface<HashMap<String, Switch>> {
    protected Switch thunderStorm = Switch.OFF;
    protected Switch environmentSound = Switch.OFF;
    protected Switch wind = Switch.OFF;
    protected Switch rain = Switch.OFF;
    protected Switch aux01 = Switch.OFF;
    protected Switch aux02 = Switch.OFF;
    protected Switch aux03 = Switch.OFF;

    public void fromJsonObject(Map<String, Object> map)
    throws ClientErrorException {
        thunderStorm = Switch.getValue((String)map.get("thunderStorm"), thunderStorm);
        environmentSound = Switch.getValue((String)map.get("environmentSound"), environmentSound);
        wind =  Switch.getValue((String)map.get("wind"), wind);
        rain = Switch.getValue((String)map.get("rain"), rain);
        aux01 = Switch.getValue((String)map.get("aux01"), aux01);
        aux02 = Switch.getValue((String)map.get("aux02"), aux02);
        aux03 = Switch.getValue((String)map.get("aux03"), aux03);
    }

    @Override
    public HashMap<String, Switch> toJson() {
        HashMap<String, Switch> map = new HashMap<>();
        map.put("thunderStorm", thunderStorm);
        map.put("wind", wind);
        map.put("rain", rain);
        map.put("environmentSound", environmentSound);
        map.put("aux01", aux01);
        map.put("aux02", aux02);
        map.put("aux03", aux03);
        return map;
    }
}
