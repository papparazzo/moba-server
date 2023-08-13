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

package moba.server.com;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import moba.server.json.JSONEncoder;

import moba.server.json.JSONException;
import moba.server.json.streamwriter.JSONStreamWriterStringBuilder;
import moba.server.messages.Message;
import moba.server.utilities.logger.MessageLogger;
import moba.server.utilities.logger.Loggable;

public class Dispatcher implements Loggable {
    protected final Set<Endpoint> allEndpoints = new HashSet<>();
    protected final Map<Long, Set<Endpoint>> groupEP = new HashMap<>();

    protected MessageLogger messageLogger;

    public Dispatcher(MessageLogger messageLogger) {
        this.messageLogger = messageLogger;
    }

    public boolean addEndpoint(Endpoint ep) {
        getLogger().log(Level.INFO, "try to add endpoint <{0}> appName <{1}> ver<{2}>", new Object[]{ep, ep.getAppName(), ep.getVersion()});

        for(Endpoint allEndpoint: allEndpoints) {
            if(allEndpoint == ep) {
                getLogger().log(Level.WARNING, "Endpoint <{0}> already set", new Object[]{ep});
                return false;
            }
        }

        ArrayList<Long> grps = ep.getMsgGroups();

        if(grps.isEmpty()) {
            addEndpointToGroup((long)-1, ep);
        }

        grps.forEach((msgGroup) -> {
            addEndpointToGroup(msgGroup, ep);
        });

        allEndpoints.add(ep);
        return true;
    }

    public void removeEndpoint(Endpoint ep) {
        this.shutDownEndpoint(ep);

        Iterator<Endpoint> iter = allEndpoints.iterator();

        boolean removed = false;

        while(iter.hasNext()) {
            if(iter.next() != ep) {
                continue;
            }
            iter.remove();
            removed = true;
            break;
        }

        if(!removed) {
            getLogger().log(Level.WARNING, "could not remove endpoint <{0}> from set!", new Object[]{ep});
        }

        removeEndpointFromGroup((long)-1, ep);

        ep.getMsgGroups().forEach((msgGroup) -> removeEndpointFromGroup(msgGroup, ep));
        getLogger().log(Level.INFO, "endpoint <{0}> successfully removed!", new Object[]{ep});
    }

    protected void addEndpointToGroup(Long grpId, Endpoint ep) {
        Set<Endpoint> set;
        if(groupEP.containsKey(grpId)) {
            set = groupEP.get(grpId);
        } else {
            set = new HashSet();
        }
        set.add(ep);
        groupEP.put(grpId, set);
    }

    protected void removeEndpointFromGroup(Long grpId, Endpoint ep) {
        if(!groupEP.containsKey(grpId)) {
            return;
        }

        groupEP.get(grpId).removeIf(endpoint -> endpoint == ep);
    }

    protected void shutDownEndpoint(Endpoint ep) {
        if(ep.isAlive()) {
            try {
                ep.interrupt();
                ep.join(250);
            } catch(InterruptedException e) {
                getLogger().log(Level.WARNING, "InterruptedException occurred! <{0}>", new Object[]{e.toString()});
            }
        }
        try {
            ep.closeEndpoint();
        } catch(Exception e) {
            getLogger().log(Level.WARNING, "Exception occurred! <{0}> Closing socket failed!", new Object[]{e.toString()});
        }
    }

    public int getEndPointsCount() {
        return allEndpoints.size();
    }

    public void resetDispatcher() {

        for(Endpoint allEndpoint: allEndpoints) {
            shutDownEndpoint(allEndpoint);
        }
    }

    public Set<Endpoint> getEndpoints() {
        return allEndpoints;
    }

    public Endpoint getEndpointByAppId(long appID) {
        for(Endpoint item : allEndpoints) {
            if(item.getAppId() == appID) {
                return item;
            }
        }
        return null;
    }

    public void dispatch(Message msg) {
        try {
            if(msg == null) {
                getLogger().log(Level.SEVERE, "msg is null!");
                return;
            }

            StringBuilder sb = new StringBuilder();
            JSONStreamWriterStringBuilder jsb = new JSONStreamWriterStringBuilder(sb);
            JSONEncoder encoder = new JSONEncoder(jsb);
            encoder.encode(msg.getData());

            int grpId = msg.getGroupId();
            int msgId = msg.getMessageId();
            String data = sb.toString();

            messageLogger.out(msg);

            if(msg.getEndpoint() != null) {
                sendMessage(grpId, msgId, data, msg.getEndpoint());
                return;
            }
            sendBroadCastMessage(grpId, msgId, data, grpId);
            sendBroadCastMessage(grpId, msgId, data, -1);
        } catch(IOException | JSONException e) {
            getLogger().log(Level.SEVERE, "<{0}>", new Object[]{e.toString()});
        }
    }

    protected void sendBroadCastMessage(int grpId, int msgId, String data, int groupKey)
    throws IOException, JSONException {
        if(!this.groupEP.containsKey((long)groupKey)) {
            return;
        }
        for(Endpoint ep : this.groupEP.get((long)groupKey)) {
            sendMessage(grpId, msgId, data, ep);
        }
    }

    protected void sendMessage(int grpId, int msgId, String data, Endpoint endpoint)
    throws IOException, JSONException {
        DataOutputStream dataOutputStream = endpoint.getDataOutputStream();
        dataOutputStream.writeInt(grpId);
        dataOutputStream.writeInt(msgId);
        dataOutputStream.writeInt(data.length());
        dataOutputStream.write(data.getBytes());
    }
}
