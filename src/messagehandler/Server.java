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

package messagehandler;

import java.text.SimpleDateFormat;
import java.util.HashMap;

import application.ServerApplication;
import com.Dispatcher;
import com.Endpoint;
import datatypes.objects.ErrorData;
import datatypes.enumerations.ErrorId;
import messages.Message;
import messages.MessageHandlerA;
import messages.MessageType;


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
            case SERVER_INFO_REQ:
                this.handleServerInfoReq(msg.getEndpoint());
                return;

            case CON_CLIENTS_REQ:
                this.dispatcher.dispatch(
                    new Message(
                        MessageType.CON_CLIENTS_RES,
                        this.dispatcher.getEndpoints(),
                        msg.getEndpoint()
                    )
                );
                return;
        }

        if(!this.checkForSameOrigin(msg.getEndpoint())) {
            this.dispatcher.dispatch(new Message(
                    MessageType.ERROR,
                    new ErrorData(ErrorId.SAME_ORIGIN_NEEDED),
                    msg.getEndpoint()
                )
            );
            return;
        }

        switch(msg.getMsgType()) {
            case RESET_CLIENT:
                this.sendToClient(msg, MessageType.CLIENT_RESET);
                break;

            case SELF_TESTING_CLIENT:
                this.sendToClient(msg, MessageType.CLIENT_SELF_TESTING);
                break;

            default:
                throw new UnsupportedOperationException(
                    "unknow msg <" + msg.getMsgType().toString() + ">."
                );
        }
    }

    protected void sendToClient(Message msg, MessageType mType) {
        Endpoint ep = this.dispatcher.getEndpointByAppId((long)msg.getData());
        if(ep != null) {
            this.dispatcher.dispatch(new Message(mType, null, ep));
            return;
        }
        this.dispatcher.dispatch(new Message(
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
        SimpleDateFormat dfu        = new SimpleDateFormat("HH:mm:ss");
        SimpleDateFormat dfs        = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        SimpleDateFormat dfb        = new SimpleDateFormat("dd.MM.yyyy");

        map.put("appName",           this.app.getAppName());
        map.put("version",           this.app.getVersion());
        map.put("buildDate",         dfb.format(this.app.getBuildDate()));
        map.put("startTime",         dfs.format(this.app.getStartTime()));
        map.put("upTime",            dfu.format(java.lang.System.currentTimeMillis() - this.app.getStartTime()));

        map.put("maxClients",        this.app.getMaxClients());
        map.put("connectedClients",  this.dispatcher.getEndPointsCount());

        map.put("supportedMessages", MessageType.values());

        map.put("osArch",            java.lang.System.getProperty("os.arch", ""));
        map.put("osName",            java.lang.System.getProperty("os.name", ""));
        map.put("osVersion",         java.lang.System.getProperty("os.version", ""));

        map.put("fwType",            java.lang.System.getProperty("java.vm.vendor", ""));
        map.put("fwVersion",         java.lang.System.getProperty("java.version", ""));

        this.dispatcher.dispatch(new Message(MessageType.SERVER_INFO_RES, map, ep));
    }
}