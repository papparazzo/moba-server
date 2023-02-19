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

import moba.server.com.Endpoint;

public class Message implements Comparable {
    protected long   trigger;

    protected Endpoint    endpoint = null;
    protected Object      data     = null;
    protected int         groupId;
    protected int         messageId;

    public Message(MessageType msgType, Object data, Endpoint ep) {
        this(msgType, data);
        endpoint = ep;
    }

    public Message(MessageType msgType, Object data) {
        this(msgType.getGroupId(), msgType.getMessageId(), data);
    }

    public Message(int groupId, int msgId, Object data, Endpoint ep) {
        this(groupId, msgId, data);
        endpoint = ep;
    }

    public Message(int grpId, int msgId, Object data) {
        this(grpId, msgId);
        this.data = data;
    }

    public Message(int grpId, int msgId) {
        if (grpId < 1 || msgId < 1) {
            throw new ExceptionInInitializerError("invalid data given");
        }

        groupId = grpId;
        messageId = msgId;
        trigger = System.currentTimeMillis();
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

    public int getMessageId() {
        return messageId;
    }

    public int getGroupId() {
        return groupId;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("[");
        sb.append(groupId);
        sb.append(":");
        sb.append(messageId);
        sb.append("]");

        if(endpoint == null) {
            sb.append(" BC");
        } else {
            sb.append(" #");
            sb.append(endpoint.getAppId());
        }

        return sb.toString();
    }
}
