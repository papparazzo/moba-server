/*
 *  common
 *
 *  Copyright (C) 2013 Stefan Paproth <pappi-@gmx.de>
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

import com.Endpoint;

public class Message implements Comparable {
    protected long   trigger;

    protected Endpoint    endpoint = null;
    protected Object      data     = null;
    protected MessageType msgType;

    public static final String MSG_HEADER  = "msgType";
    public static final String DATA_HEADER = "msgData";

    public Message(MessageType msgType, Object data, Endpoint ep) {
        this(msgType, data);
        this.endpoint = ep;
    }

    public Message(MessageType msgType, Object data) {
        this(msgType);
        this.data = data;
    }

    public Message(MessageType msgType) {
        // throw Excpetion if msgType == null
        this.msgType = msgType;
        this.trigger =
            System.currentTimeMillis() +
            msgType.getMessagePriority().getOffset();
    }

    @Override
    public int compareTo(Object o) {
        long i = this.trigger;
        long j = ((Message)o).trigger;

        if(i < j) {
            return -1;
        } else if(i > j) {
            return 1;
        }
        return 0;
    }

    public Endpoint getEndpoint() {
        return this.endpoint;
    }

    public Object getData() {
        return this.data;
    }

    public MessageType getMsgType() {
        return this.msgType;
    }

    @Override
    public String toString() {
        String rv = "";

        if(this.endpoint != null) {
            rv = "<" + this.endpoint.getSocket().toString() + ">";
        }
        rv += "<" + this.msgType.toString() + ">";

        if(this.data != null) {
            rv += "<" + this.data.toString() + ">";
        }
        return rv;
    }
}
