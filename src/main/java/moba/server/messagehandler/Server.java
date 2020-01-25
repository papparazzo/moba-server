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
import moba.server.datatypes.objects.ErrorData;
import moba.server.datatypes.enumerations.ErrorId;
import moba.server.messages.Message;
import moba.server.messages.MessageHandlerA;
import moba.server.messages.MessageType;


public class Server extends MessageHandlerA {

    protected Dispatcher dispatcher = null;
    protected ServerApplication app = null;

    public Server(Dispatcher dispatcher, ServerApplication app) {
        this.dispatcher = dispatcher;
        this.app = app;
    }

    @Override
    public void handleMsg(Message msg) {
        switch(msg.getMsgType()) {
            case INFO_REQ:
                handleServerInfoReq(msg.getEndpoint());
                return;

            case CON_CLIENTS_REQ:
                dispatcher.dispatch(new Message(MessageType.CON_CLIENTS_RES, dispatcher.getEndpoints(), msg.getEndpoint()));
                return;
        }

        if(!checkForSameOrigin(msg.getEndpoint())) {
            dispatcher.dispatch(new Message(MessageType.ERROR, new ErrorData(ErrorId.SAME_ORIGIN_NEEDED), msg.getEndpoint()));
            return;
        }

        switch(msg.getMsgType()) {
            case RESET_CLIENT:
                sendToClient(msg, MessageType.RESET);
                break;

            case SELF_TESTING_CLIENT:
                sendToClient(msg, MessageType.SELF_TESTING);
                break;

            default:
                throw new UnsupportedOperationException(
                    "unknow msg <" + msg.getMsgType().toString() + ">."
                );
        }
    }

    protected void sendToClient(Message msg, MessageType mType) {
        Endpoint ep = dispatcher.getEndpointByAppId((long)msg.getData());
        if(ep != null) {
            dispatcher.dispatch(new Message(mType, null, ep));
            return;
        }
        dispatcher.dispatch(new Message(
                MessageType.ERROR,
                new ErrorData(ErrorId.INVALID_APP_ID, "app-id <" + msg.getData().toString() + "> is invalid"),
                msg.getEndpoint()
            )
        );
    }

    private boolean checkForSameOrigin(Endpoint ep) {
//        if( FIXME: Implementieren
//            ep.getSocket().getInetAddress().getHostAddress() ==
//            Inet4Address.getLocalHost().getHostAddress()
//        ) {
            return true;
//        }
//        return false;
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

        map.put("supportedMessages", MessageType.values());

        map.put("osArch",            java.lang.System.getProperty("os.arch", ""));
        map.put("osName",            java.lang.System.getProperty("os.name", ""));
        map.put("osVersion",         java.lang.System.getProperty("os.version", ""));

        map.put("fwType",            java.lang.System.getProperty("java.vm.vendor", ""));
        map.put("fwVersion",         java.lang.System.getProperty("java.version", ""));

        dispatcher.dispatch(new Message(MessageType.INFO_RES, map, ep));
    }
}
