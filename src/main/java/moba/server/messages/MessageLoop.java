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
import java.util.logging.Level;
import java.util.logging.Logger;

import moba.server.com.Dispatcher;
import moba.server.com.Endpoint;
import moba.server.datatypes.enumerations.ClientError;
import moba.server.datatypes.enumerations.HardwareState;
import moba.server.datatypes.objects.ErrorData;
import moba.server.datatypes.objects.IncidentData;
import moba.server.messages.messageType.ClientMessage;
import moba.server.messages.messageType.InternMessage;
import moba.server.messages.messageType.MessagingMessage;
import moba.server.messages.messageType.ServerMessage;
import moba.server.utilities.exceptions.ClientErrorException;
import org.apache.commons.collections4.queue.CircularFifoQueue;

final public class MessageLoop {

    private final Map<Integer, MessageHandlerA> handlers   = new HashMap<>();
    private final Dispatcher                    dispatcher;
    private final Logger                        logger;
    private final CircularFifoQueue<IncidentData> list;

    public MessageLoop(Dispatcher dispatcher, Logger logger, CircularFifoQueue<IncidentData> list) {
        this.dispatcher = dispatcher;
        this.logger = logger;
        this.list = list;
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
                        in.clear();
                        resetHandler();
                        return true;
                    }

                    case SERVER_SHUTDOWN -> {
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
            } catch(ErrorException e) {
                handleException(e.getErrorId(), e, msg.getEndpoint());
            } catch(IllegalArgumentException e) {
                handleException(ErrorId.INVALID_DATA_SEND, e, msg.getEndpoint());
            } catch(java.lang.ClassCastException | IOException | NullPointerException e) {
                handleException(ErrorId.FAULTY_MESSAGE, e, msg.getEndpoint());
            } catch(SQLException e) {
                handleException(ErrorId.DATABASE_ERROR, e, msg.getEndpoint());
            } catch(Exception e) {
                handleException(ErrorId.UNKNOWN_ERROR, e, msg.getEndpoint());
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
        freeResources(appId);
        dispatcher.removeEndpoint(msg.getEndpoint());
        dispatcher.broadcast(new Message(ServerMessage.CLIENT_CLOSED, appId));
    }

    private void handleException(ErrorId id, Throwable e, Endpoint ep) {
        String message = e.toString();
        String stack = e.getStackTrace()[0].toString();

        logger.log(Level.SEVERE, "Exception! <" + message + "> -> " + stack);
        dispatcher.send(new Message(ClientMessage.ERROR, new ErrorData(id, e.getMessage())), ep);
    }
}
