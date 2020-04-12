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
 *  along with this program. If not, see <http://www.gnu.org/licenses/agpl.txt>.
 *
 */

package moba.server.messages;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import moba.server.com.Dispatcher;
import moba.server.datatypes.enumerations.ErrorId;
import moba.server.datatypes.enumerations.HardwareState;
import moba.server.datatypes.objects.ErrorData;
import moba.server.messages.messageType.ClientMessage;
import moba.server.messages.messageType.InternMessage;
import moba.server.messages.messageType.ServerMessage;
import moba.server.utilities.exceptions.ErrorException;

public class MessageLoop {
    protected static final Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    protected Map<Integer, MessageHandlerA> handlers = new HashMap<>();
    protected Dispatcher dispatcher = null;

    public MessageLoop(Dispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    public void addHandler(MessageHandlerA msgHandler) {
        msgHandler.init();
        handlers.put(msgHandler.getGroupId(), msgHandler);
    }

    public Set<Integer> getRegisteredHandlers() {
        return handlers.keySet();
    }

    public boolean loop(PriorityBlockingQueue<Message> in)
    throws InterruptedException {
        while(true) {
            Message msg = in.take();
            MessageLoop.LOGGER.log(Level.INFO, "handle msg [{0}:{1}] from <{2}>", new Object[]{msg.getGroupId(), msg.getMessageId(), msg.getEndpoint()});

            if(msg.getGroupId() == InternMessage.GROUP_ID) {
                switch(InternMessage.fromId(msg.getMessageId())) {
                    case SERVER_RESET:
                        in.clear();
                        resetHandler();
                        return true;

                    case SERVER_SHUTDOWN:
                        shutdownHandler();
                        return false;

                    case SET_HARDWARE_STATE:
                        hardwareStateChangedHandler((HardwareState)msg.getData());
                        continue;

                    case CLIENT_SHUTDOWN:
                        continue;
                }
            }

            if(!handlers.containsKey(msg.getGroupId())) {
                ErrorData err = new ErrorData(ErrorId.UNKNOWN_GROUP_ID, "handler for group <" + Integer.toString(msg.getGroupId()) + "> was not registered!");

                MessageLoop.LOGGER.log(Level.WARNING, err.toString());

                dispatcher.dispatch(new Message(ClientMessage.ERROR, err, msg.getEndpoint()));
                continue;
            }
            try {
                handlers.get(msg.getGroupId()).handleMsg(msg);
            } catch(ErrorException e) {
                MessageLoop.LOGGER.log(Level.WARNING, e.toString());
                dispatcher.dispatch(new Message(ClientMessage.ERROR, e.getErrorData(), msg.getEndpoint()));
            }
        }
    }

    protected void freeResources(long appId) {
        Iterator<Integer> iter = handlers.keySet().iterator();

        while(iter.hasNext()) {
            handlers.get(iter.next()).freeResources(appId);
        }
    }

    protected void resetHandler() {
        dispatcher.getEndpoints().forEach((ep) -> {
            dispatcher.dispatch(new Message(ClientMessage.RESET, null, ep));
        });
        Iterator<Integer> iter = handlers.keySet().iterator();
        while(iter.hasNext()) {
            handlers.get(iter.next()).reset();
        }
    }

    protected void shutdownHandler() {
        dispatcher.getEndpoints().forEach((ep) -> {
            dispatcher.dispatch(new Message(ClientMessage.SHUTDOWN, null, ep));
        });
        Iterator<Integer> iter = handlers.keySet().iterator();
        while(iter.hasNext()) {
            handlers.get(iter.next()).shutdown();
        }
    }

    protected void hardwareStateChangedHandler(HardwareState state) {
        Iterator<Integer> iter = handlers.keySet().iterator();

        while(iter.hasNext()) {
            handlers.get(iter.next()).hardwareStateChanged(state);
        }
    }

    protected void handleClientClose(Message msg) {
        long appId = msg.getEndpoint().getAppId();
        freeResources(appId);
        dispatcher.removeEndpoint(msg.getEndpoint());
        dispatcher.dispatch(new Message(ServerMessage.CLIENT_CLOSED, appId));
    }
}
