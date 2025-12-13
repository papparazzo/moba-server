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
import moba.server.datatypes.objects.IncidentData;
import moba.server.messages.AbstractMessageHandler;
import moba.server.messages.Message;
import moba.server.messages.messagetypes.MessagingMessage;
import moba.server.exceptions.ClientErrorException;
import org.apache.commons.collections4.queue.CircularFifoQueue;

import java.io.IOException;

final public class Messaging extends AbstractMessageHandler {

    private final CircularFifoQueue<IncidentData> list;

    public Messaging(Dispatcher dispatcher, CircularFifoQueue<IncidentData> list) {
        this.dispatcher = dispatcher;
        this.list = list;
    }

    @Override
    public int getGroupId() {
        return MessagingMessage.GROUP_ID;
    }

    @Override
    public void handleMsg(Message msg)
    throws ClientErrorException, IOException {
        switch(MessagingMessage.fromId(msg.getMessageId())) {
            case GET_INCIDENT_LIST   -> handleGetMessageList(msg.getEndpoint());
            case NOTIFY_INCIDENT     -> handleNotifyIncident(msg);
            case CLEAR_INCIDENT_LIST -> handleClearIncidentList();
        }
    }

    private void handleGetMessageList(Endpoint endpoint) {
        dispatcher.sendSingle(new Message(MessagingMessage.SET_INCIDENT_LIST, list), endpoint);
    }

    private void handleNotifyIncident(Message msg)
    throws ClientErrorException {
        list.add(new IncidentData(msg));
        dispatcher.sendGroup(new Message(MessagingMessage.NOTIFY_INCIDENT, msg.getData()));
    }

    private void handleClearIncidentList() {
        list.clear();
        dispatcher.sendGroup(new Message(MessagingMessage.CLEAR_INCIDENT_LIST));
    }
}
