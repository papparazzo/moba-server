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

package moba.server.com;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import moba.server.json.JsonEncoder;

import moba.server.json.JsonException;
import moba.server.json.streamwriter.JsonStreamWriterStringBuilder;
import moba.server.messages.Message;
import moba.server.utilities.logger.MessageLogger;

public class Dispatcher implements AutoCloseable {
    protected final Set<Endpoint> allEndpoints = ConcurrentHashMap.newKeySet();
    protected final Map<Long, Set<Endpoint>> groupEP = new ConcurrentHashMap<>();

    protected final MessageLogger messageLogger;

    protected Logger logger;

    public Dispatcher(MessageLogger messageLogger, Logger logger) {
        this.logger = logger;
        this.messageLogger = messageLogger;
    }

    public boolean addEndpoint(Endpoint ep) {
        logger.log(Level.INFO, "try to add endpoint <{0}>", new Object[]{ep});

        if(allEndpoints.contains(ep)) {
            logger.log(Level.WARNING, "Endpoint <{0}> already set", new Object[]{ep});
            return false;
        }

        ArrayList<Long> msgGroups = ep.getMsgGroups();

        if(msgGroups.isEmpty()) {
            addEndpointToGroup((long)-1, ep);
        }

        msgGroups.forEach((msgGroup)->addEndpointToGroup(msgGroup, ep));

        allEndpoints.add(ep);
        return true;
    }

    public void removeEndpoint(Endpoint ep) {
        if(!allEndpoints.remove(ep)) {
            logger.log(Level.WARNING, "could not remove endpoint <{0}> from set!", new Object[]{ep});
        }

        removeEndpointFromGroup((long)-1, ep);

        ep.getMsgGroups().forEach((msgGroup)->removeEndpointFromGroup(msgGroup, ep));
        logger.log(Level.INFO, "endpoint <{0}> successfully removed!", new Object[]{ep});
    }

    protected void addEndpointToGroup(Long grpId, Endpoint ep) {
        groupEP.computeIfAbsent(grpId, k->ConcurrentHashMap.newKeySet()).add(ep);
    }

    protected void removeEndpointFromGroup(Long grpId, Endpoint ep) {
        Set<Endpoint> endpoints = groupEP.get(grpId);
        if(endpoints == null) {
            return;
        }
        endpoints.remove(ep);
        if(endpoints.isEmpty()) {
            groupEP.remove(grpId, endpoints);
        }
    }

    public int getEndPointsCount() {
        return allEndpoints.size();
    }

    @Override
    public void close() {
        allEndpoints.forEach(Endpoint::closeEndpoint);
        allEndpoints.clear();
        groupEP.clear();
    }

    public Set<Endpoint> getEndpoints() {
        return allEndpoints;
    }

    public Endpoint getEndpointByAppId(long appId) {
        for(Endpoint ep : allEndpoints) {
            if(ep.getAppId() == appId) {
                return ep;
            }
        }
        return null;
    }

    public void sendSingle(Message message, Endpoint endpoint) {
        try {
            if(endpoint == null) {
                return;
            }
            sendMessage(
                message.getGroupId(),
                message.getMessageId(),
                getMessageData(message),
                endpoint
            );
        } catch(IOException | JsonException e) {
            logger.log(Level.SEVERE, "<{0}>", new Object[]{e.toString()});
        }
    }

    public void sendGroup(Message message) {
        try {
            int grpId = message.getGroupId();
            int msgId = message.getMessageId();
            String data = getMessageData(message);

            sendBroadCastMessage(grpId, msgId, data, grpId);
            sendBroadCastMessage(grpId, msgId, data, -1);
        } catch(IOException | JsonException e) {
            logger.log(Level.SEVERE, "<{0}>", new Object[]{e.toString()});
        }
    }

    public void sendAll(Message message) {
        try {
            int grpId = message.getGroupId();
            int msgId = message.getMessageId();
            String data = getMessageData(message);

            allEndpoints.forEach(ep->{
                try {
                    sendMessage(grpId, msgId, data, ep);
                } catch(IOException e) {
                    logger.log(Level.SEVERE, "<{0}>", new Object[]{e.toString()});
                }
            });
        } catch(IOException | JsonException e) {
            logger.log(Level.SEVERE, "<{0}>", new Object[]{e.toString()});
        }
    }

    private String getMessageData(Message message)
    throws IOException, JsonException {
        messageLogger.out(message);
        StringBuilder sb = new StringBuilder();
        JsonStreamWriterStringBuilder jsb = new JsonStreamWriterStringBuilder(sb);
        JsonEncoder encoder = new JsonEncoder(jsb);
        encoder.encode(message.getData());
        return sb.toString();
    }

    protected void sendBroadCastMessage(int grpId, int msgId, String data, int groupKey)
    throws IOException, JsonException {
        Set<Endpoint> endpoints = this.groupEP.get((long)groupKey);
        if(endpoints == null) {
            return;
        }
        for(Endpoint ep : endpoints) {
            sendMessage(grpId, msgId, data, ep);
        }
    }

    protected void sendMessage(int grpId, int msgId, String data, Endpoint endpoint)
    throws IOException {
        DataOutputStream dataOutputStream = endpoint.getDataOutputStream();
        dataOutputStream.writeInt(grpId);
        dataOutputStream.writeInt(msgId);
        dataOutputStream.writeInt(data.length());
        dataOutputStream.write(data.getBytes());
    }
}
