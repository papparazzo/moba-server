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

import moba.server.com.Dispatcher;
import moba.server.com.Endpoint;
import moba.server.datatypes.enumerations.ClientError;
import moba.server.datatypes.enumerations.HardwareState;
import moba.server.datatypes.enumerations.IncidentLevel;
import moba.server.datatypes.enumerations.IncidentType;
import moba.server.datatypes.objects.ErrorData;
import moba.server.datatypes.objects.IncidentData;
import moba.server.messages.messageType.ClientMessage;
import moba.server.messages.messageType.InternMessage;
import moba.server.messages.messageType.ServerMessage;
import moba.server.utilities.exceptions.ClientErrorException;
import moba.server.utilities.messaging.IncidentHandler;

final public class MessageLoop {

    private final Map<Integer, MessageHandlerA> handlers   = new HashMap<>();
    private final Dispatcher                    dispatcher;
    private final IncidentHandler               incidentHandler;

    public MessageLoop(Dispatcher dispatcher, IncidentHandler incidentHandler) {
        this.dispatcher      = dispatcher;
        this.incidentHandler = incidentHandler;
    }

    public void addHandler(MessageHandlerA msgHandler) {
        handlers.put(msgHandler.getGroupId(), msgHandler);
    }

    public boolean loop(MessageQueue in)
    throws InterruptedException, ClientErrorException {

        while(true) {
            Message msg = in.take();

            if(msg.getGroupId() == InternMessage.GROUP_ID) {
                switch(InternMessage.fromId(msg.getMessageId())) {
                    case SERVER_RESET -> {
                        incidentHandler.add(new IncidentData(
                            IncidentLevel.NOTICE,
                            IncidentType.SERVER_NOTICE,
                            "Server Neustart (reset)",
                            "Neustart der Serverapplikation aufgrund eines Server-Resets",
                            "moba-server:ServerApplication.run()"
                        ));
                        in.clear();
                        resetHandler();
                        return true;
                    }

                    case SERVER_SHUTDOWN -> {
                        incidentHandler.add(new IncidentData(
                            IncidentLevel.NOTICE,
                            IncidentType.SERVER_NOTICE,
                            "Server shutdown",
                            "Shutdown der Serverapplikation",
                            "moba-server:ServerApplication.run()"
                        ));
                        shutdownHandler();
                        return false;
                    }

                    case SET_HARDWARE_STATE -> {
                        hardwareStateChangedHandler((HardwareState)msg.getData());
                        continue;
                    }

                    case CLIENT_SHUTDOWN -> {
                        handleClientClose(msg);
                        continue;
                    }
                }
            }

            try {
                checkGroup(msg.getGroupId());
                handlers.get(msg.getGroupId()).handleMsg(msg);
            } catch(ClientErrorException e) {
                ClientError id = e.getErrorId();
                Endpoint ep = msg.getEndpoint();
                dispatcher.send(new Message(ClientMessage.ERROR, new ErrorData(id, e.getMessage())), ep);
                incidentHandler.add(new IncidentData(IncidentType.CLIENT_ERROR, e, ep));
            } catch(Throwable e) {
                incidentHandler.add(new IncidentData(IncidentType.EXCEPTION, e, msg.getEndpoint()));
                incidentHandler.add(new IncidentData(
                    IncidentLevel.CRITICAL,
                    IncidentType.SERVER_NOTICE,
                    "Restart of the server (reset)",
                    "Restart of the server application due to an error",
                    "moba-server:ServerApplication.run()")
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

    private void freeResources(long appId) {
        for(Integer integer: handlers.keySet()) {
            handlers.get(integer).freeResources(appId);
        }
    }

    private void resetHandler() {
        dispatcher.getEndpoints().forEach((ep) -> dispatcher.send(new Message(ClientMessage.RESET, null), ep));
        for(Integer integer: handlers.keySet()) {
            handlers.get(integer).shutdown();
        }
    }

    private void shutdownHandler() {
        dispatcher.getEndpoints().forEach((ep) -> dispatcher.send(new Message(ClientMessage.SHUTDOWN, null), ep));
        for(Integer integer: handlers.keySet()) {
            handlers.get(integer).shutdown();
        }
    }

    private void hardwareStateChangedHandler(HardwareState state) {
        for(Integer integer: handlers.keySet()) {
            handlers.get(integer).hardwareStateChanged(state);
        }
    }

    private void handleClientClose(Message msg) {
        long appId = msg.getEndpoint().getAppId();

        incidentHandler.add(new IncidentData(
            IncidentLevel.NOTICE,
            IncidentType.CLIENT_ERROR,
            "Client shutdown",
            "Client is terminated. Reason: \"" + msg.getData() + "\"",
            "moba-server:ServerApplication.handleClientClose()",
            msg.getEndpoint()
        ));

        freeResources(appId);
        dispatcher.removeEndpoint(msg.getEndpoint());
        dispatcher.broadcast(new Message(ServerMessage.CLIENT_CLOSED, appId));
    }
}
