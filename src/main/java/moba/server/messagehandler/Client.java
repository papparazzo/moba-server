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
import moba.server.datatypes.enumerations.ErrorId;
import moba.server.datatypes.objects.ErrorData;
import moba.server.messages.Message;
import moba.server.messages.MessageHandlerA;
import moba.server.messages.messageType.ClientMessage;
import moba.server.messages.messageType.ServerMessage;
import moba.server.utilities.exceptions.ErrorException;

public class Client extends MessageHandlerA {

    public Client(Dispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    @Override
    public int getGroupId() {
        return ClientMessage.GROUP_ID;
    }

    @Override
    public void handleMsg(Message msg)
    throws ErrorException {
        switch(ClientMessage.fromId(msg.getMessageId())) {
            case VOID -> {}

            case ECHO_REQ -> dispatcher.send(new Message(ClientMessage.ECHO_RES, msg.getData()), msg.getEndpoint());

            case START -> handleClientStart(msg);

            case ERROR -> dispatcher.broadcast(msg);

            default -> 
                throw new ErrorException(ErrorId.UNKNOWN_MESSAGE_ID, "unknown msg <" + Long.toString(msg.getMessageId()) + ">.");
        }
    }

    protected void handleClientStart(Message msg) {
        Endpoint ep = msg.getEndpoint();
        if(!dispatcher.addEndpoint(ep)) {
            dispatcher.send(
                new Message(
                    ClientMessage.ERROR,
                    new ErrorData(ErrorId.INVALID_DATA_SEND, "Endpoint <" + ep + "> already exists")
                ),
                msg.getEndpoint()
            );
            return;
        }
        dispatcher.send(new Message(ClientMessage.CONNECTED, ep.getAppId()), ep);
        dispatcher.broadcast(new Message(ServerMessage.NEW_CLIENT_STARTED, ep));
    }
}

