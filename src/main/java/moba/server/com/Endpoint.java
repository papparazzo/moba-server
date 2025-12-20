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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;

import moba.server.datatypes.base.DateTime;
import moba.server.datatypes.base.Version;
import moba.server.datatypes.enumerations.IncidentLevel;
import moba.server.datatypes.enumerations.IncidentType;
import moba.server.datatypes.enumerations.ServerState;
import moba.server.datatypes.objects.AppData;
import moba.server.datatypes.objects.EndpointData;
import moba.server.datatypes.objects.IncidentData;
import moba.server.datatypes.objects.SocketData;
import moba.server.exceptions.ClientClosingException;
import moba.server.json.JsonDecoder;
import moba.server.json.JsonException;
import moba.server.json.JsonSerializerInterface;
import moba.server.json.streamreader.JsonStreamReaderBytes;
import moba.server.json.stringreader.JsonStringReader;
import moba.server.messages.Message;
import moba.server.messages.MessageQueue;
import moba.server.messages.messagetypes.ClientMessage;
import moba.server.messages.messagetypes.InternMessage;
import moba.server.utilities.logger.Loggable;

final public class Endpoint extends Thread implements JsonSerializerInterface<Object>, Loggable {
    private AppData appData;
    private EndpointData endpointData;
    private final Socket socket;

    private final AtomicBoolean terminating = new AtomicBoolean(false);

    private final MessageQueue msgQueue;

    private final DataOutputStream dataOutputStream;
    private final DataInputStream  dataInputStream;

    public Endpoint(long id, Socket socket, MessageQueue msgQueue)
    throws IOException {
        this.endpointData = new EndpointData(null, id, new DateTime(), new SocketData(socket));

        this.msgQueue  = msgQueue;
        // TODO: Das hier ist nun nicht so schön: Wir haben einmal socket und einmal socketData!
        this.socket    = socket;

        this.dataOutputStream = new DataOutputStream(socket.getOutputStream());
        this.dataInputStream  = new DataInputStream(socket.getInputStream());

        setName("endpoint #" + id);
    }

    public void closeEndpoint() {
        terminating.set(true);
        if(isAlive()) {
            interrupt();
            try {
                join(250);
            } catch(InterruptedException e) {
                getLogger().log(Level.WARNING, "InterruptedException occurred! <{0}>", new Object[]{e.toString()});
            }
        }
        try {
            socket.close();
        } catch(Throwable e) {
            getLogger().log(Level.WARNING, "Exception occurred! <{0}> Closing socket failed!", new Object[]{e.toString()});
        }
    }

    @Override
    public String toString() {
        return appData.name() + "[" + appData.version() + "] #" + endpointData.appId() + "@" + endpointData.socket();
    }

    @Override
    public Object toJson() {
        return endpointData;
    }

    @Override
    public void run() {
        long id = endpointData.appId();
        getLogger().log(Level.INFO, "Endpoint #{0}: thread started", new Object[]{id});
        try {
            init();
            while(!isInterrupted()) {
                msgQueue.add(getNextMessage());
            }
        } catch(ClientClosingException e) {
            msgQueue.add(new Message(
                InternMessage.REMOVE_CLIENT,
                new IncidentData(
                    IncidentLevel.NOTICE,
                    IncidentType.CLIENT_NOTICE,
                    "Client closed",
                    "Client was closed",
                    "Endpoint.run()",
                    this
                ),
                this
            ));
            getLogger().log(Level.INFO, "Endpoint #{0}: thread terminated", new Object[]{id});
        } catch(Throwable e) {
            if(!terminating.get()) {
                msgQueue.add(new Message(InternMessage.SET_SERVER_STATE, ServerState.ERROR));
                msgQueue.add(new Message(
                    InternMessage.REMOVE_CLIENT,
                    new IncidentData(
                        IncidentLevel.ERROR,
                        IncidentType.CLIENT_ERROR,
                        "Client reset",
                        "Client was terminated. Reason: \"" + e + "\"",
                        "Endpoint.run()",
                        this
                    ),
                    this
                ));
            }
            getLogger().log(Level.SEVERE, "Endpoint #{0}: {1}-Exception, closing client... <{2}>", new Object[]{id, getClass().getSimpleName(), e.toString()});
        }
    }

    public long getAppId() {
        return endpointData.appId();
    }

    public ArrayList<Long> getMsgGroups() {
        return appData.msgGroups();
    }

    public DataOutputStream getDataOutputStream() {
        return dataOutputStream;
    }

    private Message getNextMessage()
    throws IOException, JsonException {
        int groupId = dataInputStream.readInt();
        int msgId = dataInputStream.readInt();
        int size = dataInputStream.readInt();

        byte[] buffer = new byte[size];
        int len = dataInputStream.read(buffer, 0, size);

        if(len != size) {
            throw new IOException("unexpected end of stream");
        }

        JsonDecoder decoder = new JsonDecoder(new JsonStringReader(new JsonStreamReaderBytes(buffer)));
        if(ClientMessage.GROUP_ID == groupId && ClientMessage.CLOSING.getMessageId() == msgId) {
            throw new ClientClosingException("closing message received, terminating endpoint");
        }

        return new Message(groupId, msgId, decoder.decode(), this);
    }

    @SuppressWarnings("unchecked")
    private void init()
    throws IOException, JsonException {
        Message msg = getNextMessage();
        if(
            ClientMessage.GROUP_ID != msg.getGroupId() ||
            ClientMessage.START.getMessageId() != msg.getMessageId()
        ) {
            throw new IOException("first msg is not CLIENT_START");
        }
        Map<String, Object> map = (Map<String, Object>)msg.getData();

        Object o = map.get("msgGroups");
        if(!(o instanceof ArrayList)) {
            throw new IOException("invalid msg groups given");
        }

        appData = new AppData(
            (String)map.get("name"),
            new Version((String)map.get("version")),
            (String)map.get("description"),
            (ArrayList<Long>)o
        );
        endpointData = endpointData.withAppData(appData);
        msgQueue.add(msg);
    }
}
