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

public enum ServerMessage implements MessageType {
    NEW_CLIENT_STARTED (1),
    CLIENT_CLOSED      (2),
    RESET_CLIENT       (3),
    INFO_REQ           (4),
    INFO_RES           (5),
    CON_CLIENTS_REQ    (6),
    CON_CLIENTS_RES    (7),
    SELF_TESTING_CLIENT(8);

    public final static int GROUP_ID = 3;

    protected int messageId;

    ServerMessage(int msgId) {
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

    public static ServerMessage fromId(int id) {
        for(ServerMessage type : values()) {
            if(type.messageId == id) {
                return type;
            }
        }
        return null;
    }
}
