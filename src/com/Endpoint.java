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

package com;

import java.io.IOException;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import datatypes.base.Version;
import java.util.Arrays;
import java.util.Collections;
import json.JSONEncoder;
import json.JSONException;
import json.JSONToStringI;
import json.streamreader.JSONStreamReaderSocket;
import json.streamwriter.JSONStreamWriterStringBuilder;
import messages.JSONMessageDecoder;
import messages.Message;
import messages.MessageType;
import utilities.MessageLogger;

public class Endpoint extends Thread implements JSONToStringI {

    protected long     id;
    protected long     startTime;
    protected Socket   socket;
    protected boolean  closing;

    protected Version  version;
    protected String   appName;

    protected Set<MessageType.MessageGroup>  msgGroups = new HashSet<>();
    protected PriorityBlockingQueue<Message> in = null;

    protected static final Logger logger =
        Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    public Endpoint(
        long id, Socket socket, PriorityBlockingQueue<Message> in
    ) {
        this.id        = id;
        this.startTime = System.currentTimeMillis();
        this.socket    = socket;
        this.in        = in;
        this.setName("endpoint #" + String.valueOf(id));
    }

    public void closeEndpoint() {
        this.closing = true;
        try {
            this.socket.close();
        } catch(IOException e) {

        }
    }

    public Message getNextMessage()
    throws IOException {
        try {
            JSONMessageDecoder decoder = new JSONMessageDecoder(
                new JSONStreamReaderSocket(this.socket)
            );
            Message msg = decoder.decodeMsg(this);
            Endpoint.logger.log(
                Level.INFO,
                "Endpoint #{0}: new message <{1}> arrived",
                new Object[]{this.id, msg.getMsgType().toString()}
            );
            MessageLogger.in(msg);
            return msg;
        } catch (IOException | JSONException e) {
            if(!this.closing) {
                throw new IOException(e);
            }
            return new Message(MessageType.VOID);
        }
    }

    @Override
    public String toString() {
        if(this.socket == null) {
            return "intern";
        }
        return String.valueOf(this.id) + ": " + this.socket.toString();
    }

    @Override
    public String toJsonString(boolean formated, int indent)
    throws JSONException, IOException {
        HashMap<String, Object> app = new HashMap<>();
        app.put("appName",   this.appName);
        app.put("version",   this.version);
        app.put("msgGroups", this.msgGroups);

        HashMap<String, Object> map = new HashMap<>();
        SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss.SSSS");

        map.put("appInfo",   app);
        map.put("appID",     this.id);
        map.put("upTime",    df.format(System.currentTimeMillis() - this.startTime));
        map.put("addr",      this.socket.getInetAddress());
        map.put("port",      this.socket.getPort());

        StringBuilder sb = new StringBuilder();
        JSONStreamWriterStringBuilder jsb = new JSONStreamWriterStringBuilder(sb);
        JSONEncoder encoder = new JSONEncoder(jsb, formated);
        encoder.encode(map, indent);
        return sb.toString();
    }

    @Override
    public void run() {
        Endpoint.logger.log(
            Level.INFO,
            "Endpoint #{0}: thread started",
            new Object[]{this.id}
        );
        try {
            if(!this.init()) {
                Endpoint.logger.log(
                    Level.WARNING,
                    "Endpoint #{0}: init failed!>",
                    new Object[]{this.id}
                );
                this.in.add(new Message(MessageType.CLIENT_CLOSE, null, this));
                return;
            }
            while(!isInterrupted()) {
                this.in.add(this.getNextMessage());
            }
        } catch(IOException e) {
            Endpoint.logger.log(
                Level.WARNING,
                "Endpoint #{0}: IOException, send <CLIENT_CLOSE> <{1}>",
                new Object[]{this.id, e.toString()}
            );
            this.in.add(new Message(MessageType.CLIENT_CLOSE, null, this));
        }
        Endpoint.logger.log(
            Level.INFO,
            "Endpoint #{0}: thread terminated",
            new Object[]{this.id}
        );
    }

    public Socket getSocket() {
        return this.socket;
    }

    public long getAppId() {
        return this.id;
    }

    public Set<MessageType.MessageGroup> getMsgGroups() {
        return this.msgGroups;
    }

    public Version getVersion() {
        return this.version;
    }

    public String getAppName() {
        return this.appName;
    }

    @SuppressWarnings("unchecked")
    private boolean init()
    throws IOException {
        Message msg = this.getNextMessage();
        MessageType mtype = msg.getMsgType();

        if(
            mtype != MessageType.CLIENT_START &&
            mtype != MessageType.CLIENT_CONNECTED
        ) {
            Endpoint.logger.log(
                Level.SEVERE,
                "first msg is neither CLIENT_START nor CLIENT_CONNECTED"
            );
            return false;
        }
        if(mtype == MessageType.CLIENT_CONNECTED) {
            return true;
        }
        Map<String, Object> map = (Map<String, Object>)msg.getData();
        this.appName = (String)map.get("appName");
        this.version = new Version((String)map.get("version"));
        Object o = map.get("msgGroups");
        if(o instanceof ArrayList) {
            ArrayList<String> arrayList = (ArrayList<String>)o;
            if(arrayList.isEmpty()) {
                Endpoint.logger.log(Level.INFO, "arrayList is empty. Take all groups");
                Collections.addAll(this.msgGroups, MessageType.MessageGroup.values());
            } else {
                for(String item : arrayList) {
                    try {
                        MessageType.MessageGroup grp = MessageType.MessageGroup.valueOf(item);
                        this.msgGroups.add(grp);
                    } catch(IllegalArgumentException e) {
                        Endpoint.logger.log(
                            Level.WARNING,
                            "ignoring unknown message-group <{0}>",
                            new Object[]{item}
                        );
                    }
                }
            }
        }
        this.in.add(msg);
        return true;
    }
}
