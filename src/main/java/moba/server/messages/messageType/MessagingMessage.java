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
 *  along with this program. If not, see <https://www.gnu.org/licenses/agpl.txt>.
 *
 */

package moba.server.messages.messageType;

import moba.server.datatypes.enumerations.ClientError;
import moba.server.messages.MessageType;
import moba.server.utilities.exceptions.ClientErrorException;

public enum MessagingMessage implements MessageType {
    GET_INCIDENT_LIST(1),
    SET_INCIDENT_LIST(2),
    NOTIFY_INCIDENT  (3);

    public final static int GROUP_ID = 9;

    private final int messageId;

    MessagingMessage(int msgId) {
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

    public static MessagingMessage fromId(int id)
    throws ClientErrorException {
        for(MessagingMessage type : values()) {
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
