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

package moba.server.messages;

import java.util.HashMap;
import java.util.Map;

import moba.server.application.ServerStateMachine;
import moba.server.com.Dispatcher;
import moba.server.com.Endpoint;
import moba.server.datatypes.enumerations.*;
import moba.server.datatypes.objects.ErrorData;
import moba.server.datatypes.objects.NotificationData;
import moba.server.messages.messagetypes.ClientMessage;
import moba.server.messages.messagetypes.InternMessage;
import moba.server.exceptions.ClientErrorException;
import moba.server.utilities.logger.Loggable;
import moba.server.utilities.messaging.NotificationHandler;

final public class MessageLoop implements Loggable {

    private final ServerStateMachine stateMachine;

    private final Map<Integer, AbstractMessageHandler> handlers   = new HashMap<>();
    private final Dispatcher                           dispatcher;
    private final NotificationHandler                  notificationHandler;

    public MessageLoop(Dispatcher dispatcher, NotificationHandler notificationHandler, ServerStateMachine stateMachine) {
        this.dispatcher          = dispatcher;
        this.notificationHandler = notificationHandler;
        this.stateMachine        = stateMachine;
    }

    public void addHandler(AbstractMessageHandler msgHandler) {
        handlers.put(msgHandler.getGroupId(), msgHandler);
        stateMachine.addHandler(msgHandler);
    }

    public boolean loop(MessageQueue in)
    throws InterruptedException {
        while(true) {
            Message msg = in.take();
            try {
                if(msg.getGroupId() == InternMessage.GROUP_ID) {
                    switch(InternMessage.fromId(msg.getMessageId())) {
                        case SERVER_RESET -> {
                            in.clear();
                            stateMachine.handleServerReset();
                            return true;
                        }

                        case SERVER_SHUTDOWN -> {
                            in.clear();
                            stateMachine.handleServerShutdown();
                            return false;
                        }

                        case REMOVE_CLIENT -> {
                            stateMachine.handleRemoveClient(msg);
                            continue;
                        }
                    }
                }

                checkGroup(msg.getGroupId());
                handlers.get(msg.getGroupId()).handleMsg(msg);
            } catch(ClientErrorException e) {
                ClientError id = e.getErrorId();
                Endpoint ep = msg.getEndpoint();
                dispatcher.sendSingle(new Message(ClientMessage.ERROR, new ErrorData(id, e.getMessage())), ep);
                notificationHandler.add(new NotificationData(NotificationType.CLIENT_ERROR, e, ep));
            } catch(Throwable e) {
                notificationHandler.add(new NotificationData(NotificationType.EXCEPTION, e, msg.getEndpoint()));
                notificationHandler.add(new NotificationData(
                    NotificationLevel.CRITICAL,
                    NotificationType.SERVER_NOTICE,
                    "Restart of the server (reset)",
                    "Restart of the server application due to an error",
                    "MessageLoop.loop()")
                );
                in.clear();
                return true;
            }
        }
    }

    private void checkGroup(int groupId)
    throws ClientErrorException {
        if(!handlers.containsKey(groupId)) {
            throw new ClientErrorException(ClientError.UNKNOWN_GROUP_ID, "no handler for group <" + groupId + ">!");
        }
    }

}
