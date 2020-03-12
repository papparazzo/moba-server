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

public enum TimerMessage implements MessageType {
    GLOBAL_TIMER_EVENT(1),
    GET_GLOBAL_TIMER  (2),
    SET_GLOBAL_TIMER  (3),
    GET_COLOR_THEME   (4),
    SET_COLOR_THEME   (5),
    COLOR_THEME_EVENT (6);

    public final static int GROUP_ID = 4;

    protected int messageId;

    TimerMessage(int msgId) {
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

    public static TimerMessage fromId(int id) {
        for(TimerMessage type : values()) {
            if(type.messageId == id) {
                return type;
            }
        }
        return null;
    }
}

