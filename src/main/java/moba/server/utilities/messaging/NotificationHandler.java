/*
 *  Project:    moba-server
 *
 *  Copyright (C) 2022 Stefan Paproth <pappi-@gmx.de>
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

package moba.server.utilities.messaging;

import moba.server.com.Dispatcher;
import moba.server.datatypes.enumerations.NotificationLevel;
import moba.server.datatypes.objects.NotificationData;
import moba.server.exceptions.ClientErrorException;
import moba.server.json.JsonException;
import moba.server.json.JsonSerializerInterface;
import moba.server.messages.Message;
import moba.server.messages.messagetypes.MessagingMessage;
import org.apache.commons.collections4.queue.CircularFifoQueue;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

final public class NotificationHandler implements JsonSerializerInterface<CircularFifoQueue<NotificationData>> {
    private final Logger logger;
    private final Dispatcher dispatcher;
    CircularFifoQueue<NotificationData> list;

    public NotificationHandler(Logger logger, Dispatcher dispatcher, CircularFifoQueue<NotificationData> list) {
        this.logger = logger;
        this.dispatcher = dispatcher;
        this.list = list;
    }

    public synchronized void add(NotificationData notificationData) {
        logger.log(convertLevel(notificationData.getLevel()), notificationData.toString());
        list.add(notificationData);
        dispatcher.sendGroup(new Message(MessagingMessage.SEND_NOTIFICATION, notificationData));
    }

    public synchronized void add(Message msg)
    throws ClientErrorException {
        add(new NotificationData(msg));
    }

    public synchronized void clear() {
        list.clear();
        dispatcher.sendGroup(new Message(MessagingMessage.CLEAR_NOTIFICATION_LIST));
    }

    private Level convertLevel(NotificationLevel level) {
        return switch(level) {
            case CRITICAL, ERROR -> Level.SEVERE;
            case WARNING -> Level.WARNING;
            case NOTICE -> Level.INFO;
        };
    }

    @Override
    public CircularFifoQueue<NotificationData> toJson() throws JsonException, IOException {
        return list;
    }
}
