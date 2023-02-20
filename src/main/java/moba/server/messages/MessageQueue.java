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

package moba.server.messages;

import java.util.concurrent.PriorityBlockingQueue;
import moba.server.messages.messageType.InternMessage;
import moba.server.utilities.logger.MessageLogger;

public class MessageQueue extends PriorityBlockingQueue<Message> {

    protected MessageLogger logger;

    public MessageQueue(MessageLogger logger) {
        this.logger = logger;
    }

    @Override
    public boolean add(Message msg) {
        if(msg.getGroupId() == InternMessage.GROUP_ID) {
            logger.intern(msg);
        } else {
            logger.in(msg);
        }

        return super.add(msg);
    }
}
