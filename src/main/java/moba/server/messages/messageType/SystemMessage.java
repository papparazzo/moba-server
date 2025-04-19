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

import moba.server.datatypes.enumerations.SystemError;
import moba.server.messages.MessageType;
import moba.server.utilities.exceptions.SystemErrorException;

public enum SystemMessage implements MessageType {
    SET_AUTOMATIC_MODE      (1),
    TOGGLE_AUTOMATIC_MODE   (2),
    TRIGGER_EMERGENCY_STOP  (3),
    RELEASE_EMERGENCY_STOP  (4),
    SET_STANDBY_MODE        (5),
    TOGGLE_STANDBY_MODE     (6),
    GET_HARDWARE_STATE      (7),
    HARDWARE_STATE_CHANGED  (8),
    HARDWARE_SHUTDOWN       (9),
    HARDWARE_RESET          (10);

    public final static int GROUP_ID = 7;

    private final int messageId;

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
    throws SystemErrorException {
        for(SystemMessage type : values()) {
            if(type.messageId == id) {
                return type;
            }
        }
        throw new SystemErrorException(
            SystemError.UNKNOWN_MESSAGE_ID,
            "unknown msg [" + Long.toString(GROUP_ID) + ":" + Long.toString(id) + "]."
        );
    }
}
