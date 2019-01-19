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

package messages;

import java.io.IOException;
import java.util.HashMap;

import json.JSONEncoder;
import json.JSONException;
import json.streamwriter.JSONStreamWriterI;

public class JSONMessageEncoder extends JSONEncoder {
    public JSONMessageEncoder()
    throws IOException {
        super();
    }

    public JSONMessageEncoder(JSONStreamWriterI writer)
    throws IOException {
        super(writer);
    }

    public void encodeMsg(Message msg)
    throws IOException, JSONException {
        HashMap<String, Object> map = new HashMap<>();
        map.put(Message.MSG_HEADER_NAME, String.valueOf(msg.getMsgType()));
        map.put(Message.MSG_HEADER_DATA, msg.getData());
        encode(map);
    }
}
