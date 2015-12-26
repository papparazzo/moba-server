/*
 *  common
 *
 *  Copyright (C) 2014 stefan
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

import json.streamwriter.JSONStreamWriterStringBuilder;
import json.*;

import java.io.*;
import java.util.*;

public class ErrorInfo implements JSONToStringI {

    protected final ErrorId errorId;
    protected final String additonalMsg;

    public enum ErrorId {
        SAME_ORIGIN_NEEDED,
        INVALID_APP_ID,
        FAULTY_MESSAGE,
        INVALID_DATA_SEND,
        DATASET_LOCKED,
        DATASET_MISSING,
        DATABASE_ERROR,
        UNKNOWN_ERROR
    }

    public ErrorInfo(ErrorId errorId) {
        this(errorId, "");
    }

    public ErrorInfo(ErrorId errorId, String additonalMsg) {
        this.errorId = errorId;
        this.additonalMsg = additonalMsg;
    }

    @Override
    public String toJsonString(boolean formated, int indent)
    throws JSONException, IOException {
        HashMap<String, Object> map = new HashMap<>();
        map.put("errorId",      this.errorId);
        map.put("additonalMsg", this.additonalMsg);

        StringBuilder sb = new StringBuilder();
        JSONStreamWriterStringBuilder jsb = new JSONStreamWriterStringBuilder(sb);
        JSONEncoder encoder = new JSONEncoder(jsb, formated);
        encoder.encode(map, indent);
        return sb.toString();
    }
}
