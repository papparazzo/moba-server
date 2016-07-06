
package com;

import java.util.*;
import java.util.logging.*;
import java.io.*;

import json.*;
import json.streamwriter.*;
import messages.*;
import utilities.*;

public class Dispatcher implements SenderI {
    protected final Set<Endpoint> broadcastEP = new HashSet<>();

    protected static final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    protected final EnumMap<MessageType.MessageGroup, Set<Endpoint>>
        groupEP = new EnumMap<>(MessageType.MessageGroup.class);

    public void addEndpoint(Endpoint ep) {
        Dispatcher.logger.log(
            Level.INFO,
            "try to add endpoint <{0}> appName <{1}> ver<{2}>",
            new Object[]{
                ep.getSocket(),
                ep.getAppName(),
                ep.getVersion().toString()
            }
        );

        Set<Endpoint> set;
        for(MessageType.MessageGroup msgGroup : ep.getMsgGroups()) {
            if(this.groupEP.containsKey(msgGroup)) {
                set = this.groupEP.get(msgGroup);
            } else {
                set = new HashSet();
            }
            set.add(ep);
            this.groupEP.put(msgGroup, set);
        }

        this.broadcastEP.add(ep);
    }

    public void removeEndpoint(Endpoint ep) {
        this.shutDownEndpoint(ep);

        Iterator<Endpoint> iter = this.broadcastEP.iterator();

        boolean removed = false;

        while(iter.hasNext()) {
            if(iter.next() == ep) {
                iter.remove();
                removed = true;
                break;
            }
        }

        if(!removed) {
            Dispatcher.logger.log(
                Level.WARNING,
                "could not remove endpoint <{0}> from set!",
                new Object[]{ep.getSocket()}
            );
        }

        for(MessageType.MessageGroup msgGroup : ep.getMsgGroups()) {
            if(this.groupEP.containsKey(msgGroup)) {
                Set<Endpoint> set = this.groupEP.get(msgGroup);
                iter = set.iterator();

                while(iter.hasNext()) {
                    if(iter.next() == ep) {
                        iter.remove();
                    }
                }
            }
        }

        this.dispatch(new Message(MessageType.CLIENT_CLOSED, ep));

        Dispatcher.logger.log(
            Level.INFO,
            "endpoint <{0}> succesfully removed!",
            new Object[]{ep.getSocket()}
        );
    }

    protected void shutDownEndpoint(Endpoint ep) {
        if(ep.isAlive()) {
            try {
                ep.interrupt();
                ep.join(250);
            } catch(InterruptedException e) {
                Dispatcher.logger.log(
                    Level.WARNING,
                    "InterruptedException occured! <{0}>",
                    new Object[]{e.toString()}
                );
            }
        }
        try {
            ep.closeEndpoint();
        } catch(Exception e) {
            Dispatcher.logger.log(
                Level.WARNING,
                "Exception occured! <{0}> Closing socket failed!",
                new Object[]{e.toString()}
            );
        }
    }

    public int getEndPointsCount() {
        return this.broadcastEP.size();
    }

    public void resetDispatcher() {
        Iterator<Endpoint> iter = this.broadcastEP.iterator();

        while(iter.hasNext()) {
            this.shutDownEndpoint(iter.next());
        }
    }

    public Set<Endpoint> getEndpoints() {
        return this.broadcastEP;
    }

    public Endpoint getEndpointByAppId(long appID) {
        for(Endpoint item : this.broadcastEP) {
            if(item.getAppId() == appID) {
                return item;
            }
        }
        return null;
    }

    @Override
    public boolean dispatch(Message msg) {
        try {
            if(msg == null) {
                Dispatcher.logger.log(Level.SEVERE, "msg is null!");
                return false;
            }
            MessageLogger.out(msg);
            Dispatcher.logger.log(
                Level.INFO,
                "try to send message <{0}>",
                new Object[]{msg.getMsgType().toString()}
            );

            JSONMessageEncoder encoder = new JSONMessageEncoder();

            MessageType.MessageClass cls = msg.getMsgType().getMessageClass();

            if(msg.getEndpoint() != null) {
                cls = MessageType.MessageClass.SINGLE;
            }

            switch(cls) {
                case INTERN:
                    Dispatcher.logger.log(
                        Level.INFO,
                        "msg-class is intern!"
                    );
                    return false;

                case SINGLE:
                    if(msg.getEndpoint() == null) {
                        Dispatcher.logger.log(
                            Level.WARNING,
                            "msg contains not endpoint"
                        );
                        return false;
                    }
                    encoder.addAdditionalWriter(
                        new JSONStreamWriterSocket(
                            msg.getEndpoint().getSocket()
                        )
                    );
                    break;

                case GROUP:
                    MessageType.MessageGroup grp =
                        msg.getMsgType().getMessageGroup();

                    if(!this.groupEP.containsKey(grp)) {
                        return false;
                    }

                    for(Endpoint item : this.groupEP.get(grp)) {
                        encoder.addAdditionalWriter(
                            new JSONStreamWriterSocket(item.getSocket())
                        );
                    }
                    break;

                case BROADCAST:
                    for(Endpoint item : this.broadcastEP) {
                        encoder.addAdditionalWriter(
                            new JSONStreamWriterSocket(item.getSocket())
                        );
                    }
                    break;
            }
            encoder.encodeMsg(msg);
            return true;
        } catch(IOException | JSONException e) {
            Dispatcher.logger.log(
                Level.WARNING,
                "<{0}>",
                new Object[]{e.toString()}
            );
        }
        return false;
    }
}