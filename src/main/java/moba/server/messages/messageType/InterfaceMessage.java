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

public enum InterfaceMessage implements MessageType {
    CONNECTIVITY_STATE_CHANGED(1),
    CONTACT_TRIGGERED         (2),
    SET_BRAKE_VECTOR          (3),
    RESET_BRAKE_VECTOR        (4),
    SET_LOCO_SPEED            (5),
    SET_LOCO_DIRECTION        (6),
    SET_LOCO_FUNCTION         (7),
    SWITCH_ACCESSORY_DECODERS (8);

    public final static int GROUP_ID = 6;

    private final int messageId;

    InterfaceMessage(int msgId) {
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

    public static InterfaceMessage fromId(int id)
    throws ClientErrorException {
        for(InterfaceMessage type : values()) {
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
