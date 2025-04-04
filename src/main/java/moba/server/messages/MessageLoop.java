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

import moba.server.com.Dispatcher;
import moba.server.datatypes.enumerations.ErrorId;
import moba.server.datatypes.enumerations.HardwareState;
import moba.server.datatypes.objects.ErrorData;
import moba.server.messages.messageType.ClientMessage;
import moba.server.messages.messageType.InternMessage;
import moba.server.messages.messageType.ServerMessage;
import moba.server.utilities.exceptions.ErrorException;
import moba.server.utilities.logger.Loggable;

public class MessageLoop implements Loggable {

    protected Map<Integer, MessageHandlerA> handlers   = new HashMap<>();
    protected Dispatcher                    dispatcher;

    public MessageLoop(Dispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    public void addHandler(MessageHandlerA msgHandler) {
        handlers.put(msgHandler.getGroupId(), msgHandler);
    }

    public boolean loop(MessageQueue in)
    throws InterruptedException, ErrorException {

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

            if(!handlers.containsKey(msg.getGroupId())) {
                ErrorData err =
                    new ErrorData(
                        ErrorId.UNKNOWN_GROUP_ID,
                        "handler for group <" + msg.getGroupId() + "> was not registered!"
                    );

                getLogger().log(Level.WARNING, err.toString());

                dispatcher.send(new Message(ClientMessage.ERROR, err), msg.getEndpoint());
                continue;
            }
            try {
                handlers.get(msg.getGroupId()).handleMsg(msg);
            } catch(ErrorException e) {
                getLogger().log(Level.WARNING, "ErrorException! <{0}>", new Object[]{e.toString()});
                dispatcher.send(new Message(ClientMessage.ERROR, e.getErrorData()), msg.getEndpoint());
            } catch(Exception e) {
                getLogger().log(Level.SEVERE, e.toString());
                in.clear();
                return true;
            }
        }
    }

    protected void freeResources(long appId) {

        for(Integer integer: handlers.keySet()) {
            handlers.get(integer).freeResources(appId);
        }
    }

    protected void resetHandler() {
        dispatcher.getEndpoints().forEach((ep) -> dispatcher.send(new Message(ClientMessage.RESET, null), ep));
        for(Integer integer: handlers.keySet()) {
            handlers.get(integer).shutdown();
        }
    }

    protected void shutdownHandler() {
        dispatcher.getEndpoints().forEach((ep) -> dispatcher.send(new Message(ClientMessage.SHUTDOWN, null), ep));
        for(Integer integer: handlers.keySet()) {
            handlers.get(integer).shutdown();
        }
    }

    protected void hardwareStateChangedHandler(HardwareState state) {
        for(Integer integer: handlers.keySet()) {
            handlers.get(integer).hardwareStateChanged(state);
        }
    }

    protected void handleClientClose(Message msg) {
        long appId = msg.getEndpoint().getAppId();
        freeResources(appId);
        dispatcher.removeEndpoint(msg.getEndpoint());
        dispatcher.broadcast(new Message(ServerMessage.CLIENT_CLOSED, appId));
    }
}
