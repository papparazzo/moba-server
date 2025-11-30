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

package moba.server.messages.messagetypes;

import moba.server.datatypes.enumerations.ClientError;
import moba.server.messages.MessageTypeInterface;
import moba.server.exceptions.ClientErrorException;

public enum EnvironmentMessage implements MessageTypeInterface {
    GET_FUNCTION_LIST     (1),
    SET_FUNCTION_LIST     (2),
    SET_FUNCTION          (3),
    SET_FUNCTIONS         (4),
    FUNCTION_STATE_CHANGED(5);

    public final static int GROUP_ID = 5;

    private final int messageId;

    EnvironmentMessage(int msgId) {
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

    public static EnvironmentMessage fromId(int id)
    throws ClientErrorException {
        for(EnvironmentMessage type : values()) {
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
