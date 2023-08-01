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

public enum LayoutMessage implements MessageType {
    GET_LAYOUTS_REQ         (1),
    GET_LAYOUTS_RES         (2),
    DELETE_LAYOUT           (3),
    CREATE_LAYOUT           (4),
    UPDATE_LAYOUT           (5),
    UNLOCK_LAYOUT           (6),
    LOCK_LAYOUT             (7),
    GET_LAYOUT_REQ          (8),
    GET_LAYOUT_READ_ONLY_REQ(9),
    GET_LAYOUT_RES          (10),
    SAVE_LAYOUT             (11),
    LAYOUT_CHANGED          (12),
    DEFAULT_LAYOUT_CHANGED  (13);

    public final static int GROUP_ID = 8;

    protected int messageId;

    LayoutMessage(int msgId) {
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

    public static LayoutMessage fromId(int id)
    throws ErrorException {
        for(LayoutMessage type : values()) {
            if(type.messageId == id) {
                return type;
            }
        }
        throw new ErrorException(ErrorId.UNKNOWN_MESSAGE_ID, "unknown msg [" + Long.toString(GROUP_ID) + ":" + Long.toString(id) + "].");
    }
}
