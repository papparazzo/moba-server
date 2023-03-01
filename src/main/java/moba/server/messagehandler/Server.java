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

import java.text.SimpleDateFormat;
import java.util.HashMap;

import moba.server.application.ServerApplication;
import moba.server.com.Dispatcher;
import moba.server.com.Endpoint;
import moba.server.datatypes.enumerations.ErrorId;
import moba.server.messages.Message;
import moba.server.messages.MessageHandlerA;
import moba.server.messages.MessageType;
import moba.server.messages.messageType.ClientMessage;
import moba.server.messages.messageType.ServerMessage;
import moba.server.utilities.exceptions.ErrorException;

public class Server extends MessageHandlerA {
    protected ServerApplication app = null;

    public Server(Dispatcher dispatcher, ServerApplication app) {
        this.dispatcher = dispatcher;
        this.app = app;
    }

    @Override
    public int getGroupId() {
        return ServerMessage.GROUP_ID;
    }

    @Override
    public void handleMsg(Message msg)
    throws ErrorException {
        ServerMessage smsg = ServerMessage.fromId(msg.getMessageId());

        switch(smsg) {
            case INFO_REQ:
                handleServerInfoReq(msg.getEndpoint());
                return;

            case CON_CLIENTS_REQ:
                dispatcher.dispatch(new Message(ServerMessage.CON_CLIENTS_RES, dispatcher.getEndpoints(), msg.getEndpoint()));
                return;
        }

        checkForSameOrigin(msg.getEndpoint().getSocket().getInetAddress());

        switch(smsg) {
            case RESET_CLIENT:
                sendToClient(msg, ClientMessage.RESET);
                break;

            case SELF_TESTING_CLIENT:
                sendToClient(msg, ClientMessage.SELF_TESTING);
                break;

            default:
                throw new ErrorException(ErrorId.UNKNOWN_MESSAGE_ID, "unknow msg <" + Long.toString(msg.getMessageId()) + ">.");
        }
    }

    protected void sendToClient(Message msg, MessageType mType)
    throws ErrorException {
        Endpoint ep = dispatcher.getEndpointByAppId((long)msg.getData());
        if(ep == null) {
            throw new ErrorException(ErrorId.INVALID_APP_ID, "app-id <" + msg.getData().toString() + "> is invalid");
        }
        dispatcher.dispatch(new Message(mType, null, ep));
    }

    private void handleServerInfoReq(Endpoint ep) {
        HashMap<String, Object> map = new HashMap<>();
        SimpleDateFormat dfs        = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        SimpleDateFormat dfb        = new SimpleDateFormat("dd.MM.yyyy");

        map.put("appName",           app.getAppName());
        map.put("version",           app.getVersion());
        map.put("buildDate",         dfb.format(app.getBuildDate()));
        map.put("startTime",         dfs.format(app.getStartTime()));

        map.put("maxClients",        app.getMaxClients());
        map.put("connectedClients",  dispatcher.getEndPointsCount());

        map.put("osArch",            java.lang.System.getProperty("os.arch", ""));
        map.put("osName",            java.lang.System.getProperty("os.name", ""));
        map.put("osVersion",         java.lang.System.getProperty("os.version", ""));

        map.put("fwType",            java.lang.System.getProperty("java.vm.vendor", ""));
        map.put("fwVersion",         java.lang.System.getProperty("java.version", ""));

        dispatcher.dispatch(new Message(ServerMessage.INFO_RES, map, ep));
    }
}
