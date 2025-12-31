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

import moba.server.com.Dispatcher;
import moba.server.com.Endpoint;
import moba.server.messages.AbstractMessageHandler;
import moba.server.messages.Message;
import moba.server.messages.messagetypes.MessagingMessage;
import moba.server.exceptions.ClientErrorException;
import moba.server.utilities.messaging.NotificationHandler;

import java.io.IOException;

final public class Messaging extends AbstractMessageHandler {

    private final NotificationHandler notificationHandler;

    public Messaging(Dispatcher dispatcher, NotificationHandler notificationHandler) {
        this.dispatcher          = dispatcher;
        this.notificationHandler = notificationHandler;
    }

    @Override
    public int getGroupId() {
        return MessagingMessage.GROUP_ID;
    }

    @Override
    public void handleMsg(Message msg)
    throws ClientErrorException, IOException {
        switch(MessagingMessage.fromId(msg.getMessageId())) {
            case GET_NOTIFICATION_LIST   -> handleGetNotificationList(msg.getEndpoint());
            case SEND_NOTIFICATION       -> handleSendNotification(msg);
            case CLEAR_NOTIFICATION_LIST -> handleClearNotificationList();
        }
    }

    private void handleGetNotificationList(Endpoint endpoint) {
        dispatcher.sendSingle(new Message(MessagingMessage.SET_NOTIFICATION_LIST, notificationHandler), endpoint);
    }

    private void handleSendNotification(Message msg)
    throws ClientErrorException {
        notificationHandler.add(msg);
    }

    private void handleClearNotificationList() {
        notificationHandler.clear();
    }
}
