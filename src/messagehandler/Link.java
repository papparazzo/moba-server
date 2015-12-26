/*
 *  common
 *
 *  Copyright (C) 2013 Stefan Paproth <pappi-@gmx.de>
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
package messagehandler;

import com.Endpoint;
import com.*;
import java.util.concurrent.PriorityBlockingQueue;
import messages.*;

public class Link extends MessageHandlerA {

    protected Dispatcher dispatcher = null;
    protected PriorityBlockingQueue<Message> in = null;

    public Link(Dispatcher dispatcher, PriorityBlockingQueue<Message> in) {
        this.dispatcher = dispatcher;
        this.in = in;
    }

    @Override
    public void handleMsg(Message msg) {
        switch(msg.getMsgType()) {
            case VOID:
                break;

            case ECHO_REQ:
                this.dispatcher.dispatch(
                    new Message(
                        MessageType.ECHO_RES,
                        msg.getData(),
                        msg.getEndpoint()
                    )
                );
                break;

            case CLIENT_START:
                this.handleClientStart(msg);
                break;

            case CLIENT_CLOSE:
                this.handleClientClose(msg);
                break;

            default:
                throw new UnsupportedOperationException(
                    "unknow msg <" + msg.getMsgType().toString() + ">."
                );
        }
    }

    protected void handleClientStart(Message msg) {
        Endpoint ep = msg.getEndpoint();
        this.dispatcher.addEndpoint(ep);
        this.dispatcher.dispatch(
            new Message(
                MessageType.CLIENT_CONNECTED,
                ep.getAppId(),
                ep
            )
        );
        this.dispatcher.dispatch(
            new Message(
                MessageType.NEW_CLIENT_STARTED,
                ep
            )
        );
    }

    protected void handleClientClose(Message msg) {
        this.in.add(new Message(MessageType.FREE_RESOURCES, (long)msg.getEndpoint().getAppId()));
        this.dispatcher.removeEndpoint(msg.getEndpoint());
        this.dispatcher.dispatch(
            new Message(
                MessageType.CLIENT_CLOSED,
                msg.getEndpoint()
            )
        );
    }
}

