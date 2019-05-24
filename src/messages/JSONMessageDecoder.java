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

import com.Endpoint;
import json.JSONDecoder;
import json.JSONException;
import json.stringreader.JSONStringReader;

public class JSONMessageDecoder extends JSONDecoder {

    public JSONMessageDecoder(JSONStringReader reader)
    throws JSONException {
        super(reader);
    }

    public Message decodeMsg(Endpoint ep)
    throws IOException, JSONException, JSONMessageDecoderException {
        reader.checkNext('{');
        reader.checkNext('"');

        MessageType msgtype = null;
        Object o = null;

        for(int i = 0; i < 2; i++) {
            String key = nextKey();
            reader.checkNext(':');
            switch(key) {
                case Message.MSG_HEADER_NAME:
                    reader.checkNext('"');
                    String msgName = nextKey();
                    try {
                        msgtype = MessageType.valueOf(msgName);
                    } catch(IllegalArgumentException e) {
                        throw new JSONMessageDecoderException("unknown message <" + msgName + "> arrived", e);
                    }
                    break;

                case Message.MSG_HEADER_DATA:
                    o = nextValue();
                    break;

                default:
                    throw new JSONMessageDecoderException(
                        "key <" + key + "> is neither <" + Message.MSG_HEADER_NAME + "> for name nor <" + Message.MSG_HEADER_DATA + "> for data!"
                    );
            }
            if(i == 0) {
                reader.checkNext(',');
                reader.checkNext('"');
            }
        }
        reader.checkNext('}');

        if(msgtype == null) {
            throw new JSONMessageDecoderException("invalid message arrived");
        }
        return new Message(msgtype, o, ep);
    }
}