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

import com.Endpoint;

public class Message implements Comparable {
    protected long   trigger;

    protected Endpoint    endpoint = null;
    protected Object      data     = null;
    protected MessageType msgType;

    public static final String MSG_HEADER_GROUP = "msgGroup";
    public static final String MSG_HEADER_NAME  = "msgName";
    public static final String MSG_HEADER_DATA  = "msgData";

    public Message(MessageType msgType, Object data, Endpoint ep) {
        this(msgType, data);
        endpoint = ep;
    }

    public Message(MessageType msgType, Object data) {
        this(msgType);
        this.data = data;
    }

    public Message(MessageType msgType) {
        // throw Excpetion if msgType == null
        this.msgType = msgType;
        trigger = System.currentTimeMillis() + msgType.getMessagePriority().getOffset();
    }

    @Override
    public int compareTo(Object o) {
        long i = trigger;
        long j = ((Message)o).trigger;

        if(i < j) {
            return -1;
        }
        if(i > j) {
            return 1;
        }
        return 0;
    }

    public Endpoint getEndpoint() {
        return endpoint;
    }

    public Object getData() {
        return data;
    }

    public MessageType getMsgType() {
        return msgType;
    }

    public MessageType.MessageGroup getMsgGroup() {
        return msgType.getMessageGroup();
    }

    @Override
    public String toString() {
        String rv = "";

        if(endpoint != null) {
            rv = "<" + endpoint.getSocket().toString() + ">";
        }
        rv += "<" + msgType.toString() + ">";

        if(data != null) {
            rv += "<" + data.toString() + ">";
        }
        return rv;
    }
}
