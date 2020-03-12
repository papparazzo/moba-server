/*
 *  Project:    moba-server
 *
 *  Copyright (C) 2020 Stefan Paproth <pappi-@gmx.de>
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

package moba.server.messages.messageType;

import moba.server.messages.MessageType;

public enum ClientMessage implements MessageType {
    VOID        (1),
    ECHO_REQ    (2),
    ECHO_RES    (3),
    ERROR       (4),
    START       (5),
    CONNECTED   (6),
    CLOSE       (7),
    SHUTDOWN    (8),
    RESET       (9),
    SELF_TESTING(10);

    public final static int GROUP_ID = 2;

    protected int messageId;

    ClientMessage(int msgId) {
        messageId = msgId;
    }

    @Override
    public int getGroupId() {
        return GROUP_ID;
    }

    @Override
    public int getMessageId() {
        return messageId;
    }

    @Override
    public DispatchType getDispatchType() {
        return DispatchType.SINGLE;
    }

    public static ClientMessage fromId(int id) {
        for(ClientMessage type : values()) {
            if(type.messageId == id) {
                return type;
            }
        }
        return null;
    }
}
