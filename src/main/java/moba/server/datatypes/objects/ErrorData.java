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

import moba.server.datatypes.enumerations.ErrorId;
import moba.server.json.JSONEncoder;
import moba.server.json.JSONException;
import moba.server.json.JSONToStringI;
import moba.server.json.streamwriter.JSONStreamWriterStringBuilder;

public class ErrorData implements JSONToStringI {

    protected final ErrorId errorId;
    protected final String additionalMsg;

    public ErrorData(ErrorId errorId, String additionalMsg) {
        this.errorId = errorId;
        this.additionalMsg = additionalMsg;
    }

    @Override
    public String toString() {
        return "[" + errorId.toString() + "] " + additionalMsg;
    }

    @Override
    public String toJsonString(boolean formatted, int indent)
    throws JSONException, IOException {
        HashMap<String, Object> map = new HashMap<>();
        map.put("errorId",      errorId);
        map.put("additionalMsg", additionalMsg);

        StringBuilder sb = new StringBuilder();
        JSONStreamWriterStringBuilder jsb = new JSONStreamWriterStringBuilder(sb);
        JSONEncoder encoder = new JSONEncoder(jsb, formatted);
        encoder.encode(map, indent);
        return sb.toString();
    }
}
