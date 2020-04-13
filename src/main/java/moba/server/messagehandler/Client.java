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

import moba.server.com.Dispatcher;
import moba.server.com.Endpoint;
import moba.server.datatypes.enumerations.ErrorId;
import moba.server.datatypes.objects.ErrorData;
import moba.server.messages.Message;
import moba.server.messages.MessageHandlerA;
import moba.server.messages.MessageQueue;
import moba.server.messages.messageType.ClientMessage;
import moba.server.messages.messageType.ServerMessage;
import moba.server.utilities.exceptions.ErrorException;

public class Client extends MessageHandlerA {

    protected Dispatcher dispatcher = null;
    protected MessageQueue in = null;

    public Client(Dispatcher dispatcher, MessageQueue msgQueue) {
        this.dispatcher = dispatcher;
        this.in  = msgQueue;
    }

    @Override
    public int getGroupId() {
        return ClientMessage.GROUP_ID;
    }

    @Override
    public void handleMsg(Message msg) throws ErrorException {
        switch(ClientMessage.fromId(msg.getMessageId())) {
            case VOID:
                break;

            case ECHO_REQ:
                dispatcher.dispatch(
                    new Message(ClientMessage.ECHO_RES, msg.getData(), msg.getEndpoint())
                );
                break;

            case START:
                handleClientStart(msg);
                break;

            case ERROR:
                dispatcher.dispatch(msg);
                break;

            default:
                throw new ErrorException(ErrorId.UNKNOWN_MESSAGE_ID, "unknow msg <" + Long.toString(msg.getMessageId()) + ">.");
        }
    }

    protected void handleClientStart(Message msg) {
        Endpoint ep = msg.getEndpoint();
        if(!dispatcher.addEndpoint(ep)) {
            dispatcher.dispatch(
                new Message(
                    ClientMessage.ERROR,
                    new ErrorData(ErrorId.INVALID_DATA_SEND, "Endpoint <" + ep.toString() + "> allready exists"),
                    msg.getEndpoint()
                )
            );
            return;
        }
        dispatcher.dispatch(new Message(ClientMessage.CONNECTED, ep.getAppId(), ep));
        dispatcher.dispatch(new Message(ServerMessage.NEW_CLIENT_STARTED, ep));
    }
}

