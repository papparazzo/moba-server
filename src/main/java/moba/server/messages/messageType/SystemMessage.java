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

public enum SystemMessage implements MessageType {
    SET_AUTOMATIC_MODE      (1),
    SET_EMERGENCY_STOP      (2),
    SET_STANDBY_MODE        (3),
    GET_HARDWARE_STATE      (4),
    HARDWARE_STATE_CHANGED  (5),
    HARDWARE_SHUTDOWN       (6),
    HARDWARE_RESET          (7);

    public final static int GROUP_ID = 7;

    protected int messageId;

    SystemMessage(int msgId) {
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

    public static SystemMessage fromId(int id)
    throws ErrorException {
        for(SystemMessage type : values()) {
            if(type.messageId == id) {
                return type;
            }
        }
        throw new ErrorException(ErrorId.UNKNOWN_MESSAGE_ID, "unknow msg [" + Long.toString(GROUP_ID) + ":" + Long.toString(id) + "].");
    }
}
