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

import java.io.IOException;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import moba.server.json.JSONException;
import moba.server.json.streamwriter.JSONStreamWriterSocket;
import moba.server.messages.JSONMessageEncoder;
import moba.server.messages.Message;
import moba.server.messages.MessageType;
import moba.server.utilities.MessageLogger;

public class Dispatcher implements SenderI {
    protected final Set<Endpoint> allEndpoints = new HashSet<>();

    protected static final Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    protected final EnumMap<MessageType.MessageGroup, Set<Endpoint>>
        groupEP = new EnumMap<>(MessageType.MessageGroup.class);

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
        for(MessageType.MessageGroup msgGroup : ep.getMsgGroups()) {
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

        for(MessageType.MessageGroup msgGroup : ep.getMsgGroups()) {
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
    public synchronized boolean dispatch(Message msg) {
        try {
            if(msg == null) {
                Dispatcher.LOGGER.log(Level.SEVERE, "msg is null!");
                return false;
            }
            MessageLogger.out(msg);
            Dispatcher.LOGGER.log(Level.INFO, "try to send message <{0}>", new Object[]{msg.getMsgType().toString()});

            JSONMessageEncoder encoder = new JSONMessageEncoder();

            MessageType.MessageClass cls = msg.getMsgType().getMessageClass();

            if(msg.getEndpoint() != null) {
                cls = MessageType.MessageClass.SINGLE;
            }

            switch(cls) {
                case INTERN:
                    Dispatcher.LOGGER.log(Level.INFO, "msg-class is intern!");
                    return false;

                case SINGLE:
                    if(msg.getEndpoint() == null) {
                        Dispatcher.LOGGER.log(Level.WARNING, "msg contains not endpoint");
                        return false;
                    }
                    encoder.addAdditionalWriter(new JSONStreamWriterSocket(msg.getEndpoint().getSocket()));
                    break;

                case GROUP:
                    MessageType.MessageGroup grp = msg.getMsgType().getMessageGroup();

                    if(!this.groupEP.containsKey(grp)) {
                        return false;
                    }

                    for(Endpoint item : this.groupEP.get(grp)) {
                        encoder.addAdditionalWriter(new JSONStreamWriterSocket(item.getSocket()));
                    }
                    break;
            }
            encoder.encodeMsg(msg);
            return true;
        } catch(IOException | JSONException e) {
            Dispatcher.LOGGER.log(Level.WARNING, "<{0}>", new Object[]{e.toString()});
        }
        return false;
    }
}
