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

package moba.server.messages;

import java.io.IOException;

import moba.server.com.Endpoint;
import moba.server.json.JSONDecoder;
import moba.server.json.JSONException;
import moba.server.json.stringreader.JSONStringReader;

public class JSONMessageDecoder extends JSONDecoder {

    public JSONMessageDecoder(JSONStringReader reader)
    throws JSONException {
        super(reader);
    }

    public Message decodeMsg(Endpoint ep)
    throws IOException, JSONException, JSONMessageDecoderException {
        reader.checkNext('{');
        reader.checkNext('"');

        int groupId = 0;
        int messageId = 0;
        Object o = null;

        for(int i = 0; i < 3; i++) {
            String key = nextKey();
            reader.checkNext(':');
            switch(key) {
                case Message.MSG_HEADER_GROUP:
                    reader.checkNext('"');
                    groupId = nextId();
                    break;

                case Message.MSG_HEADER_NAME:
                    reader.checkNext('"');
                    messageId = nextId();
                    break;

                case Message.MSG_HEADER_DATA:
                    o = nextValue();
                    break;

                default:
                    throw new JSONMessageDecoderException("invalid key <" + key + "> given");
            }
            if(i == 0) {
                reader.checkNext(',');
                reader.checkNext('"');
            }
        }
        reader.checkNext('}');

        if(groupId < 1 || messageId < 1) {
            throw new JSONMessageDecoderException("invalid message arrived");
        }
        return new Message(groupId, messageId, o, ep);
    }

    protected int nextId()
    throws JSONException, IOException {
        char c;
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < JSONDecoder.MAX_STRING_LENGTH; ++i) {
            c = reader.peek(!strict);

            if(",}".indexOf(c) != -1) {
                String s = sb.toString();
                s = s.trim();
                if(s.isEmpty()) {
                    throw new JSONException("empty value");
                }

                try {
                    return Integer.valueOf(s);
                } catch(NumberFormatException e) {
                    throw new JSONException("parsing, error could not determine value: <" + s + ">", e);
                }
            }
            reader.next();

            if(!Character.isDigit(c)) {
                throw new JSONException("parsing error");
            }
            sb.append(c);
        }
        throw new JSONException("maximum string-length of <" + JSONDecoder.MAX_STRING_LENGTH + "> reached!");
    }
}