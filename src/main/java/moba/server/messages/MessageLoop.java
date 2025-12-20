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

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import moba.server.com.Dispatcher;
import moba.server.com.Endpoint;
import moba.server.datatypes.enumerations.ClientError;
import moba.server.datatypes.enumerations.ServerState;
import moba.server.datatypes.enumerations.IncidentLevel;
import moba.server.datatypes.enumerations.IncidentType;
import moba.server.datatypes.objects.ErrorData;
import moba.server.datatypes.objects.IncidentData;
import moba.server.messages.messagetypes.ClientMessage;
import moba.server.messages.messagetypes.InternMessage;
import moba.server.messages.messagetypes.ServerMessage;
import moba.server.exceptions.ClientErrorException;
import moba.server.utilities.messaging.IncidentHandler;

final public class MessageLoop {

    private final Map<Integer, AbstractMessageHandler> handlers   = new HashMap<>();
    private final Dispatcher                    dispatcher;
    private final IncidentHandler               incidentHandler;

    public MessageLoop(Dispatcher dispatcher, IncidentHandler incidentHandler) {
        this.dispatcher      = dispatcher;
        this.incidentHandler = incidentHandler;
    }

    public void addHandler(AbstractMessageHandler msgHandler) {
        handlers.put(msgHandler.getGroupId(), msgHandler);
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
                            handleServerReset();
                            return true;
                        }

                        case SERVER_SHUTDOWN -> {
                            in.clear();
                            handleServerShutdown();
                            return false;
                        }

                        case SET_SERVER_STATE -> {
                            handleServerStateChanged((ServerState)msg.getData());
                            continue;
                        }

                        case REMOVE_CLIENT -> {
                            handleRemoveClient(msg);
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
                incidentHandler.add(new IncidentData(IncidentType.CLIENT_ERROR, e, ep));
            } catch(Throwable e) {
                incidentHandler.add(new IncidentData(IncidentType.EXCEPTION, e, msg.getEndpoint()));
                incidentHandler.add(new IncidentData(
                    IncidentLevel.CRITICAL,
                    IncidentType.SERVER_NOTICE,
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

    private void freeResources(long appId)
    throws SQLException {
        for(Integer key: handlers.keySet()) {
            handlers.get(key).freeResources(appId);
        }
    }

    private void handleServerReset()
    throws Exception {
        incidentHandler.add(new IncidentData(
            IncidentLevel.NOTICE,
            IncidentType.SERVER_NOTICE,
            "Server Neustart (reset)",
            "Neustart der Serverapplikation aufgrund eines Server-Resets",
            "ServerApplication.handleServerReset()"
        ));
        dispatcher.sendAll(new Message(ClientMessage.RESET, true));
        for(Integer key: handlers.keySet()) {
            handlers.get(key).reset();
        }
    }

    private void handleServerShutdown()
    throws Exception {
        incidentHandler.add(new IncidentData(
            IncidentLevel.NOTICE,
            IncidentType.SERVER_NOTICE,
            "Server reset",
            "Shutdown der Serverapplikation",
            "ServerApplication.handleServerShutdown()"
        ));
        for(Integer key: handlers.keySet()) {
            handlers.get(key).reset();
        }
    }

    private void handleServerStateChanged(ServerState state)
    throws Exception {
        for(Integer key: handlers.keySet()) {
            handlers.get(key).serverStateChanged(state);
        }
        if(state != ServerState.READY_FOR_SHUTDOWN) {
            return;
        }
        dispatcher.sendAll(new Message(ClientMessage.SHUTDOWN, null));
        incidentHandler.add(new IncidentData(
            IncidentLevel.NOTICE,
            IncidentType.SERVER_NOTICE,
            "System beenden",
            "Neustart der Serverapplikation aufgrund eines Server-Resets",
            "ServerApplication.handleServerStateChanged()"
        ));
        for(Integer key: handlers.keySet()) {
            handlers.get(key).reset();
        }
    }

    private void handleRemoveClient(Message msg)
    throws SQLException {
        long appId = msg.getEndpoint().getAppId();

        incidentHandler.add((IncidentData)msg.getData());

        freeResources(appId);
        dispatcher.removeEndpoint(msg.getEndpoint());
        dispatcher.sendGroup(new Message(ServerMessage.CLIENT_CLOSED, appId));
    }
}
