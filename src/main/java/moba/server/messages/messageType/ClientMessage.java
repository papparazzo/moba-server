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
 *  along with this program. If not, see <https://www.gnu.org/licenses/agpl.txt>.
 *
 */

package moba.server.messages.messageType;

import moba.server.datatypes.enumerations.ClientError;
import moba.server.messages.MessageType;
import moba.server.utilities.exceptions.ClientErrorException;

public enum ClientMessage implements MessageType {
    PING        (1),
    ECHO_REQ    (2),
    ECHO_RES    (3),
    ERROR       (4),
    START       (5),
    CONNECTED   (6),
    SHUTDOWN    (8),
    RESET       (9),
    SELF_TESTING(10),
    CLOSING     (11);

    public final static int GROUP_ID = 2;

    private final int messageId;

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

    public static ClientMessage fromId(int id)
    throws ClientErrorException {
        for(ClientMessage type : values()) {
            if(type.messageId == id) {
                return type;
            }
        }
        throw new ClientErrorException(
            ClientError.UNKNOWN_MESSAGE_ID,
            "unknown msg [" + Long.toString(GROUP_ID) + ":" + Long.toString(id) + "]."
        );
    }
}
