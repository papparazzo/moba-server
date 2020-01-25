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

package moba.server.messagehandler;

import java.util.concurrent.PriorityBlockingQueue;

import moba.server.com.Dispatcher;
import moba.server.com.Endpoint;
import moba.server.datatypes.enumerations.ErrorId;
import moba.server.datatypes.objects.ErrorData;
import moba.server.messages.Message;
import moba.server.messages.MessageHandlerA;
import moba.server.messages.MessageType;

public class Client extends MessageHandlerA {

    protected Dispatcher dispatcher = null;
    protected PriorityBlockingQueue<Message> msgQueueIn = null;

    public Client(Dispatcher dispatcher, PriorityBlockingQueue<Message> msgQueue) {
        this.dispatcher = dispatcher;
        this.msgQueueIn  = msgQueue;
    }

    @Override
    public void handleMsg(Message msg) {
        switch(msg.getMsgType()) {
            case VOID:
                break;

            case ECHO_REQ:
                dispatcher.dispatch(new Message(MessageType.ECHO_RES, msg.getData(), msg.getEndpoint()));
                break;

            case START:
                handleClientStart(msg);
                break;

            case CLOSE:
                handleClientClose(msg);
                break;

            case ERROR:
                dispatcher.dispatch(msg);
                break;

            default:
                throw new UnsupportedOperationException(
                    "unknow msg <" + msg.getMsgType().toString() + ">."
                );
        }
    }

    protected void handleClientStart(Message msg) {
        Endpoint ep = msg.getEndpoint();
        if(!dispatcher.addEndpoint(ep)) {
            dispatcher.dispatch(
                new Message(
                    MessageType.ERROR,
                    new ErrorData(ErrorId.INVALID_DATA_SEND, "Endpoint <" + ep.toString() + "> allready exists"),
                    msg.getEndpoint()
                )
            );
            return;
        }
        dispatcher.dispatch(new Message(MessageType.CONNECTED, ep.getAppId(), ep));
        dispatcher.dispatch(new Message(MessageType.NEW_CLIENT_STARTED, ep));
    }

    protected void handleClientClose(Message msg) {
        msgQueueIn.add(new Message(MessageType.FREE_RESOURCES, (long)msg.getEndpoint().getAppId()));
        dispatcher.removeEndpoint(msg.getEndpoint());
        dispatcher.dispatch(new Message(MessageType.CLIENT_CLOSED, msg.getEndpoint().getAppId()));
    }
}

