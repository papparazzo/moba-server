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

import moba.server.datatypes.enumerations.ErrorId;
import moba.server.messages.MessageType;
import moba.server.utilities.exceptions.ErrorException;

public enum ControlMessage implements MessageType {
    GET_BLOCK_LIST_REQ        (1),
    GET_BLOCK_LIST_RES        (2),
    GET_SWITCH_STAND_LIST_REQ (3),
    GET_SWITCH_STAND_LIST_RES (4),
    GET_TRAIN_LIST_REQ        (5),
    GET_TRAIN_LIST_RES        (6),
    LOCK_BLOCK                (7),
    LOCK_BLOCK_WAITING        (8),
    BLOCK_LOCKED              (9),
    BLOCK_LOCKING_FAILED     (10),
    UNLOCK_BLOCK             (11);

    public final static int GROUP_ID = 10;

    protected int messageId;

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
    throws ErrorException {
        for(ControlMessage type : values()) {
            if(type.messageId == id) {
                return type;
            }
        }
        throw new ErrorException(ErrorId.UNKNOWN_MESSAGE_ID, "unknow msg [" + Long.toString(GROUP_ID) + ":" + Long.toString(id) + "].");
    }
}
