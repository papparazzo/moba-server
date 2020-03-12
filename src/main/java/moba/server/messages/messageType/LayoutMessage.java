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

public enum LayoutMessage implements MessageType {
    GET_LAYOUTS_REQ         (1, DispatchType.SINGLE),
    GET_LAYOUTS_RES         (2, DispatchType.SINGLE),
    DELETE_LAYOUT           (3, DispatchType.SINGLE),
    LAYOUT_DELETED          (4, DispatchType.GROUP),
    CREATE_LAYOUT           (5, DispatchType.SINGLE),
    LAYOUT_CREATED          (6, DispatchType.GROUP),
    UPDATE_LAYOUT           (7, DispatchType.SINGLE),
    LAYOUT_UPDATED          (8, DispatchType.GROUP),
    UNLOCK_LAYOUT           (9, DispatchType.SINGLE),
    LAYOUT_UNLOCKED         (10, DispatchType.GROUP),
    LOCK_LAYOUT             (11, DispatchType.SINGLE),
    LAYOUT_LOCKED           (12, DispatchType.GROUP),
    GET_LAYOUT_REQ          (13, DispatchType.SINGLE),
    GET_LAYOUT_READ_ONLY_REQ(14, DispatchType.SINGLE),
    GET_LAYOUT_RES          (15, DispatchType.SINGLE),
    SAVE_LAYOUT             (16, DispatchType.SINGLE),
    LAYOUT_CHANGED          (17, DispatchType.GROUP);

    public final static int GROUP_ID = 8;

    protected int messageId;
    protected DispatchType dispatchType;

    LayoutMessage(int msgId, DispatchType dt) {
        messageId = msgId;
        dispatchType = dt;
    }

    @Override
    public int getGroupId() {
        return GROUP_ID;
    }

    @Override
    public int getMessageId() {
        return messageId;
    }

    @Override
    public DispatchType getDispatchType() {
        return dispatchType;
    }

    public static LayoutMessage fromId(int id) {
        for(LayoutMessage type : values()) {
            if(type.messageId == id) {
                return type;
            }
        }
        return null;
    }

}
