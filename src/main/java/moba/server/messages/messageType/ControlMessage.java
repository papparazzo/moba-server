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
import moba.server.messages.MessageTypeInterface;
import moba.server.exceptions.ClientErrorException;

public enum ControlMessage implements MessageTypeInterface {
    GET_BLOCK_LIST_REQ        (1),
    GET_BLOCK_LIST_RES        (2),
    SAVE_BLOCK_LIST           (3),
    GET_SWITCH_STAND_LIST_REQ (4),
    GET_SWITCH_STAND_LIST_RES (5),
    // SAVE_SWITCH_STAND_LIST    (6),
    GET_TRAIN_LIST_REQ        (7),
    GET_TRAIN_LIST_RES        (8),
    // SAVE_TRAIN_LIST           (9),

	ROUTE_SWITCHED            (10),
	ROUTE_RELEASED            (11),
	BLOCK_RELEASED            (12),
    PUSH_TRAIN                (13);

    public final static int GROUP_ID = 10;

    private final int messageId;

    ControlMessage(int msgId) {
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

    public static ControlMessage fromId(int id)
    throws ClientErrorException {
        for(ControlMessage type : values()) {
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
