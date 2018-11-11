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

package messages;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.Dispatcher;
import com.Endpoint;
import datatypes.enumerations.HardwareState;
import messages.MessageType.MessageGroup;

public class MessageLoop {
    protected static final Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    protected Map<MessageGroup, MessageHandlerA> handlers = new HashMap<>();
    protected Dispatcher dispatcher = null;

    public MessageLoop(Dispatcher dispatcher) {
        dispatcher = dispatcher;
    }

    public void addHandler(MessageGroup msgGroup, MessageHandlerA msgHandler) {
        msgHandler.init();
        handlers.put(msgGroup, msgHandler);
    }

    public Set<MessageGroup> getRegisteredHandlers() {
        return handlers.keySet();
    }

    public boolean loop(PriorityBlockingQueue<Message> in)
    throws InterruptedException {
        while(true) {
            Message msg = in.take();
            MessageLoop.LOGGER.log(
                Level.INFO,
                "handle msg <{0}> from <{1}>",
                new Object[]{msg.getMsgType(), msg.getEndpoint()}
            );

            if(msg.getMsgType().getMessageGroup() == MessageGroup.BASE) {
                switch(msg.getMsgType()) {
                    case SERVER_RESET:
                        in.clear();
                        resetHandler();
                        return true;

                    case SERVER_SHUTDOWN:
                        shutdownHandler();
                        return false;

                    case FREE_RESOURCES:
                        freeResources((long)msg.getData());
                        continue;

                    case SET_HARDWARE_STATE:
                        hardwareStateChangedHandler((HardwareState)msg.getData());
                        continue;
                }
            }

            if(handlers.containsKey(msg.getMsgType().getMessageGroup())) {
                handlers.get(msg.getMsgType().getMessageGroup()).handleMsg(msg);
                continue;
            }

            MessageLoop.LOGGER.log(
                Level.SEVERE,
                "handler for msg-group <{0}> was not registered!",
                new Object[]{msg.getMsgType().getMessageGroup()}
            );
        }
    }

    protected void freeResources(long id) {
        Iterator<MessageGroup> iter = handlers.keySet().iterator();

        while(iter.hasNext()) {
            handlers.get(iter.next()).freeResources(id);
        }
    }

    protected void resetHandler() {
        for(Endpoint ep : dispatcher.getEndpoints()) {
            dispatcher.dispatch(
                new Message(MessageType.CLIENT_RESET, null, ep)
            );
        }
        Iterator<MessageGroup> iter = handlers.keySet().iterator();
        while(iter.hasNext()) {
            handlers.get(iter.next()).reset();
        }
    }

    protected void shutdownHandler() {
        for(Endpoint ep : dispatcher.getEndpoints()) {
            dispatcher.dispatch(
                    new Message(MessageType.CLIENT_SHUTDOWN, null, ep)
            );
        }
        Iterator<MessageGroup> iter = handlers.keySet().iterator();
        while(iter.hasNext()) {
            handlers.get(iter.next()).shutdown();
        }
    }

    protected void hardwareStateChangedHandler(HardwareState state) {
        Iterator<MessageGroup> iter = handlers.keySet().iterator();

        while(iter.hasNext()) {
            handlers.get(iter.next()).hardwareStateChanged(state);
        }
    }
}
