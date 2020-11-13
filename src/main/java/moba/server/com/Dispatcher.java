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
import java.util.logging.Logger;
import moba.server.json.JSONEncoder;

import moba.server.json.JSONException;
import moba.server.json.streamwriter.JSONStreamWriterStringBuilder;
import moba.server.messages.Message;
import moba.server.utilities.MessageLogger;

public class Dispatcher implements SenderI {
    protected final Set<Endpoint> allEndpoints = new HashSet<>();
    protected final Map<Long, Set<Endpoint>> groupEP = new HashMap<>();

    protected static final Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    public boolean addEndpoint(Endpoint ep) {
        Dispatcher.LOGGER.log(Level.INFO, "try to add endpoint <{0}> appName <{1}> ver<{2}>", new Object[]{ep, ep.getAppName(), ep.getVersion()});

        Iterator<Endpoint> iter = allEndpoints.iterator();

        while(iter.hasNext()) {
            if(iter.next() == ep) {
                Dispatcher.LOGGER.log(Level.WARNING, "Enpoint <{0}> allready set", new Object[]{ep});
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
            Dispatcher.LOGGER.log(Level.WARNING, "could not remove endpoint <{0}> from set!", new Object[]{ep});
        }

        removeEndpointFromGroup((long)-1, ep);

        ep.getMsgGroups().forEach((msgGroup) -> {
            removeEndpointFromGroup(msgGroup, ep);
        });
        Dispatcher.LOGGER.log(Level.INFO, "endpoint <{0}> succesfully removed!", new Object[]{ep});
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
        Iterator<Endpoint> iter = groupEP.get(grpId).iterator();

        while(iter.hasNext()) {
            if(iter.next() == ep) {
                iter.remove();
            }
        }
    }

    protected void shutDownEndpoint(Endpoint ep) {
        if(ep.isAlive()) {
            try {
                ep.interrupt();
                ep.join(250);
            } catch(InterruptedException e) {
                Dispatcher.LOGGER.log(Level.WARNING, "InterruptedException occured! <{0}>", new Object[]{e.toString()});
            }
        }
        try {
            ep.closeEndpoint();
        } catch(Exception e) {
            Dispatcher.LOGGER.log(Level.WARNING, "Exception occured! <{0}> Closing socket failed!", new Object[]{e.toString()});
        }
    }

    public int getEndPointsCount() {
        return allEndpoints.size();
    }

    public void resetDispatcher() {
        Iterator<Endpoint> iter = allEndpoints.iterator();

        while(iter.hasNext()) {
            shutDownEndpoint(iter.next());
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

    @Override
    public synchronized void dispatch(Message msg) {
        dispatch(msg, null);
    }

    @Override
    public void dispatch(Message msg, Endpoint ep) {
        try {
            if(msg == null) {
                Dispatcher.LOGGER.log(Level.SEVERE, "msg is null!");
                return;
            }
            MessageLogger.out(msg, ep);

            StringBuilder sb = new StringBuilder();
            JSONStreamWriterStringBuilder jsb = new JSONStreamWriterStringBuilder(sb);
            JSONEncoder encoder = new JSONEncoder(jsb);
            encoder.encode(msg.getData());

            int grpId = msg.getGroupId();
            int msgId = msg.getMessageId();
            String data = sb.toString();

            if(ep != null) {
                sendMessage(grpId, msgId, data, ep);
                return;
            }
            sendBroadCastMessage(grpId, msgId, data, grpId, msg.getEndpoint());
            sendBroadCastMessage(grpId, msgId, data, -1, msg.getEndpoint());
        } catch(IOException | JSONException e) {
            Dispatcher.LOGGER.log(Level.WARNING, "<{0}>", new Object[]{e.toString()});
        }
    }

    protected void sendBroadCastMessage(int grpId, int msgId, String data, int groupKey, Endpoint exclEp)
    throws IOException, JSONException {
        if(this.groupEP.containsKey((long)groupKey)) {
            for(Endpoint ep : this.groupEP.get((long)groupKey)) {
                if(ep == exclEp) {
                    continue;
                }
                sendMessage(grpId, msgId, data, ep);
            }
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
