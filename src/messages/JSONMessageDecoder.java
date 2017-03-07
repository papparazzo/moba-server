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
import json.streamreader.JSONStreamReaderI;

public class JSONMessageDecoder extends JSONDecoder {

    public JSONMessageDecoder(JSONStreamReaderI reader)
    throws JSONException {
        super(reader);
    }

    public Message decodeMsg(Endpoint ep)
    throws IOException, JSONException {
        this.checkNext('{');
        this.checkNext('"');

        MessageType msgtype = null;
        Object o = null;

        for(int i = 0; i < 2; i++) {
            String key = this.nextKey();
            this.checkNext(':');
            switch(key) {
                case Message.MSG_HEADER:
                    this.checkNext('"');
                    String msgkey = this.nextKey();
                    try {
                        msgtype = MessageType.valueOf(msgkey);
                    } catch(IllegalArgumentException e) {
                        throw new JSONException(
                            "unknown message <" + msgkey + "> arrived",
                            e
                        );
                    }
                    break;

                case Message.DATA_HEADER:
                    o = this.nextValue();
                    break;

                default:
                    throw new JSONException(
                        "key <" + key + "> is neither msg-header nor data-header!"
                    );
            }
            if(i == 0) {
                this.checkNext(',');
                this.checkNext('"');
            }
        }
        this.checkNext('}');

        if(msgtype == null) {
            throw new JSONException("invalid message arrived");
        }
        return new Message(msgtype, o, ep);
    }
}