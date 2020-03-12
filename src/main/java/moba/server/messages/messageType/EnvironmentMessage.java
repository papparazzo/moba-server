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

public enum EnvironmentMessage implements MessageType {
    GET_ENVIRONMENT  (1),
    SET_ENVIRONMENT  (2),
    GET_AMBIENCE     (3),
    SET_AMBIENCE     (4),
    GET_AMBIENT_LIGHT(5),
    SET_AMBIENT_LIGHT(6);

    public final static int GROUP_ID = 5;

    protected int messageId;

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

    @Override
    public DispatchType getDispatchType() {
        return DispatchType.GROUP;
    }

    public static EnvironmentMessage fromId(int id) {
        for(EnvironmentMessage type : values()) {
            if(type.messageId == id) {
                return type;
            }
        }
        return null;
    }
}
