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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import moba.server.application.ServerApplication;
import moba.server.com.Dispatcher;
import moba.server.com.Endpoint;
import moba.server.datatypes.base.Date;
import moba.server.datatypes.base.DateTime;
import moba.server.datatypes.enumerations.ClientError;
import moba.server.messages.AbstractMessageHandler;
import moba.server.messages.Message;
import moba.server.messages.MessageTypeInterface;
import moba.server.messages.messagetypes.ClientMessage;
import moba.server.messages.messagetypes.ServerMessage;
import moba.server.utilities.AllowList;
import moba.server.utilities.config.Config;
import moba.server.exceptions.ClientErrorException;

final public class Server extends AbstractMessageHandler {
    private final ServerApplication app;
    private final AllowList allowList;
    private final Config config;

    public Server(Dispatcher dispatcher, ServerApplication app, AllowList allowList, Config config) {
        this.dispatcher = dispatcher;
        this.app = app;
        this.allowList = allowList;
        this.config = config;
    }

    @Override
    public int getGroupId() {
        return ServerMessage.GROUP_ID;
    }

    @Override
    public void handleMsg(Message msg)
    throws ClientErrorException, IOException {
        switch(ServerMessage.fromId(msg.getMessageId())) {
            case INFO_REQ              -> handleServerInfoReq(msg.getEndpoint());
            case CON_CLIENTS_REQ       -> handleClientsReq(msg.getEndpoint());
            case RESET_CLIENT          -> sendToClient(msg, ClientMessage.RESET, false);
            case RESET_CLIENT_HARDWARE -> sendToClient(msg, ClientMessage.RESET, true);
            case SELF_TESTING_CLIENT   -> sendToClient(msg, ClientMessage.SELF_TESTING, null);
            case ADD_ALLOWED_IP        -> handleAddIpAddress(msg);
            case GET_ALLOWED_IP_LIST   -> handleGetAllowedIpList(msg.getEndpoint());
            case SET_ALLOWED_IP_LIST   -> handleSetAllowedIpList(msg);
        }
    }

    private void sendToClient(Message msg, MessageTypeInterface mType, Object data)
    throws ClientErrorException {
        Endpoint ep = dispatcher.getEndpointByAppId((long)msg.getData());
        if(ep == null) {
            throw new ClientErrorException(ClientError.INVALID_APP_ID, "app-id <" + msg.getData().toString() + "> is invalid");
        }
        dispatcher.sendSingle(new Message(mType, data), ep);
    }

    private void handleAddIpAddress(Message msg)
    throws IOException {
        String address = (String)msg.getData();
        this.allowList.add(address);
        var list = allowList.getList();
        config.setSection("common.serverConfig.allowedIPs", list);
        config.writeFile();
        dispatcher.sendGroup(new Message(ServerMessage.SET_ALLOWED_IP_LIST, list));
    }

    private void handleGetAllowedIpList(Endpoint endpoint) {
        dispatcher.sendSingle(new Message(ServerMessage.SET_ALLOWED_IP_LIST, allowList.getList()), endpoint);
    }

    @SuppressWarnings("unchecked")
    private void handleSetAllowedIpList(Message message)
    throws IOException {
        ArrayList<String> list = (ArrayList<String>)message.getData();
        allowList.setList(list);
        config.setSection("common.serverConfig.allowedIPs", list);
        config.writeFile();
        dispatcher.sendGroup(new Message(ServerMessage.SET_ALLOWED_IP_LIST, list));
    }

    private void handleClientsReq(Endpoint endpoint) {
        dispatcher.sendSingle(new Message(ServerMessage.CON_CLIENTS_RES, dispatcher.getEndpoints()), endpoint);
    }

    private void handleServerInfoReq(Endpoint ep) {
        HashMap<String, Object> map = new HashMap<>();

        map.put("appName",           app.getAppName());
        map.put("version",           app.getVersion());
        map.put("buildDate",         new Date(app.getBuildDate()));
        map.put("startTime",         new DateTime(app.getStartTime()));

        map.put("maxClients",        app.getMaxClients());
        map.put("connectedClients",  dispatcher.getEndPointsCount());

        map.put("osArch",            java.lang.System.getProperty("os.arch", ""));
        map.put("osName",            java.lang.System.getProperty("os.name", ""));
        map.put("osVersion",         java.lang.System.getProperty("os.version", ""));

        map.put("fwType",            java.lang.System.getProperty("java.vm.vendor", ""));
        map.put("fwVersion",         java.lang.System.getProperty("java.version", ""));

        dispatcher.sendSingle(new Message(ServerMessage.INFO_RES, map), ep);
    }
}
