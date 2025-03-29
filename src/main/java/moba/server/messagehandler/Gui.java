/*
 *  Project:    moba-server
 *
 *  Copyright (C) 2016 Stefan Paproth <pappi-@gmx.de>
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

package moba.server.messagehandler;

import moba.server.datatypes.enumerations.ErrorId;
import moba.server.messages.Message;
import moba.server.messages.MessageHandlerA;
import moba.server.utilities.exceptions.ErrorException;

public class Gui extends MessageHandlerA {
    protected static final int GROUP_ID = 9;

    protected static final int SYSTEM_NOTICE = 1;

    @Override
    public int getGroupId() {
        return GROUP_ID;
    }

    @Override
    public void handleMsg(Message msg)
    throws ErrorException {
        switch(msg.getMessageId()) {
            case SYSTEM_NOTICE ->
                sendSystemNotice(msg);

            default ->
                throw new ErrorException(ErrorId.UNKNOWN_MESSAGE_ID, "unknown msg <" + Long.toString(msg.getMessageId()) + ">.");
        }
    }

    public void sendSystemNotice(Message msg) {
        dispatcher.dispatch(new Message(GROUP_ID, SYSTEM_NOTICE, msg.getData()));
    }
}
