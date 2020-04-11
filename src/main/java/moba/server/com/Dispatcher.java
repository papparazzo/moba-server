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
import java.io.OutputStream;
import java.net.Socket;
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
import moba.server.messages.MessageType;
import moba.server.utilities.MessageLogger;

public class Dispatcher implements SenderI {
    protected final Set<Endpoint> allEndpoints = new HashSet<>();

    protected static final Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    protected final Map<Long, Set<Endpoint>> groupEP = new HashMap<>();

    public boolean addEndpoint(Endpoint ep) {
        Dispatcher.LOGGER.log(
            Level.INFO,
            "try to add endpoint <{0}> appName <{1}> ver<{2}>",
            new Object[]{ep.getSocket(), ep.getAppName(), ep.getVersion().toString()}
        );

        Iterator<Endpoint> iter = allEndpoints.iterator();

        while(iter.hasNext()) {
            if(iter.next() == ep) {
                Dispatcher.LOGGER.log(Level.WARNING, "Enpoint <{0}> allready set", new Object[]{ep.toString()});
                return false;
            }
        }

        Set<Endpoint> set;
        for(Long msgGroup : ep.getMsgGroups()) {
            if(groupEP.containsKey(msgGroup)) {
                set = groupEP.get(msgGroup);
            } else {
                set = new HashSet();
            }
            set.add(ep);
            groupEP.put(msgGroup, set);
        }

        allEndpoints.add(ep);
        return true;
    }

    public void removeEndpoint(Endpoint ep) {
        this.shutDownEndpoint(ep);

        Iterator<Endpoint> iter = allEndpoints.iterator();

        boolean removed = false;

        while(iter.hasNext()) {
            if(iter.next() == ep) {
                iter.remove();
                removed = true;
                break;
            }
        }

        if(!removed) {
            Dispatcher.LOGGER.log(Level.WARNING, "could not remove endpoint <{0}> from set!", new Object[]{ep.getSocket()});
        }

        for(Long msgGroup : ep.getMsgGroups()) {
            if(groupEP.containsKey(msgGroup)) {
                Set<Endpoint> set = groupEP.get(msgGroup);
                iter = set.iterator();

                while(iter.hasNext()) {
                    if(iter.next() == ep) {
                        iter.remove();
                    }
                }
            }
        }
        Dispatcher.LOGGER.log(Level.INFO, "endpoint <{0}> succesfully removed!", new Object[]{ep.getSocket()});
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
        try {
            if(msg == null) {
                Dispatcher.LOGGER.log(Level.SEVERE, "msg is null!");
                return;
            }
            MessageLogger.out(msg);
            MessageType mt = msg.getMessageType();

            StringBuilder sb = new StringBuilder();
            JSONStreamWriterStringBuilder jsb = new JSONStreamWriterStringBuilder(sb);
            JSONEncoder encoder = new JSONEncoder(jsb);
            encoder.encode(msg.getData());

            int grpId = msg.getGroupId();
            int msgId = msg.getMessageId();
            String data = sb.toString();

            if(mt == null || mt.getDispatchType() == MessageType.DispatchType.SINGLE) {
                if(msg.getEndpoint() == null) {
                    Dispatcher.LOGGER.log(Level.WARNING, "msg contains not endpoint");
                    return;
                }
                sendMessage(grpId, msgId, data, msg.getEndpoint().getSocket());
                return;
            }
            if(this.groupEP.containsKey((long)grpId)) {
                for(Endpoint item : this.groupEP.get((long)grpId)) {
                    sendMessage(grpId, msgId, data, item.getSocket());
                }
            }
        } catch(IOException | JSONException e) {
            Dispatcher.LOGGER.log(Level.WARNING, "<{0}>", new Object[]{e.toString()});
        }
    }

    protected void sendMessage(int grpId, int msgId, String data, Socket socket)
    throws IOException, JSONException {
        OutputStream outputStream = socket.getOutputStream();
        try (DataOutputStream dataOutputStream = new DataOutputStream(outputStream)) {
            dataOutputStream.writeInt(grpId);
            dataOutputStream.writeInt(msgId);
            dataOutputStream.writeInt(data.length());
            dataOutputStream.write(data.getBytes());
        }
    }
}
