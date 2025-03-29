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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.logging.Level;

import moba.server.datatypes.base.Version;
import moba.server.json.JSONDecoder;
import moba.server.json.JSONEncoder;
import moba.server.json.JSONException;
import moba.server.json.JSONToStringI;
import moba.server.json.streamreader.JSONStreamReaderBytes;
import moba.server.json.streamwriter.JSONStreamWriterStringBuilder;
import moba.server.json.stringreader.JSONStringReader;
import moba.server.messages.Message;
import moba.server.messages.messageType.ClientMessage;
import moba.server.messages.messageType.InternMessage;
import moba.server.utilities.logger.Loggable;

public class Endpoint extends Thread implements JSONToStringI, Loggable {

    protected long     id;
    protected long     startTime;
    protected Socket   socket;
    protected boolean  closing;

    protected Version  version;
    protected String   appName;

    protected ArrayList<Long>  msgGroups = new ArrayList<>();
    protected PriorityBlockingQueue<Message> in;

    protected DataOutputStream dataOutputStream;
    protected DataInputStream  dataInputStream;

    public Endpoint(long id, Socket socket, PriorityBlockingQueue<Message> in)
    throws IOException {
        this.id               = id;
        this.startTime        = System.currentTimeMillis();
        this.socket           = socket;
        this.in               = in;

        this.dataOutputStream = new DataOutputStream(socket.getOutputStream());
        this.dataInputStream  = new DataInputStream(socket.getInputStream());

        setName("endpoint #" + id);
    }

    public void closeEndpoint() {
        closing = true;
        try {
            socket.close();
        } catch(IOException ignored) {

        }
    }

    @Override
    public String toString() {
        return id + ": " + socket.toString();
    }

    @Override
    public String toJsonString(boolean formatted, int indent)
    throws JSONException, IOException {
        HashMap<String, Object> app = new HashMap<>();
        app.put("appName",   appName);
        app.put("version",   version);
        app.put("msgGroups", msgGroups);

        HashMap<String, Object> map = new HashMap<>();
        SimpleDateFormat dfs = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

        map.put("appInfo",   app);
        map.put("appID",     id);
        map.put("startTime", dfs.format(startTime));
        map.put("addr",      socket.getInetAddress());
        map.put("port",      socket.getPort());

        StringBuilder sb = new StringBuilder();
        JSONStreamWriterStringBuilder jsb = new JSONStreamWriterStringBuilder(sb);
        JSONEncoder encoder = new JSONEncoder(jsb, formatted);
        encoder.encode(map, indent);
        return sb.toString();
    }

    @Override
    public void run() {
        getLogger().log(Level.INFO, "Endpoint #{0}: thread started", new Object[]{id});
        try {
            init();
            while(!isInterrupted()) {
                in.add(getNextMessage());
            }
        } catch(NullPointerException e) {
            // noop -> getNextMessage returns null, when closing is set to true.
            //         That's lead to a NullPointerException in PriorityBlockingQueue.add()
        } catch(Exception e) {
            getLogger().log(Level.INFO, "Endpoint #{0}: Exception, closing client... <{1}>", new Object[]{id, e.toString()});
            in.add(new Message(InternMessage.CLIENT_SHUTDOWN, null, this));
        } catch(OutOfMemoryError e) {
            getLogger().log(Level.SEVERE, "Endpoint #{0}: OutOfMemoryError <{1}>", new Object[]{id, e.toString()});
            in.add(new Message(InternMessage.CLIENT_SHUTDOWN, null, this));
        }
        getLogger().log(Level.INFO, "Endpoint #{0}: thread terminated", new Object[]{id});
    }

    public Socket getSocket() {
        return socket;
    }

    public long getAppId() {
        return id;
    }

    public ArrayList<Long> getMsgGroups() {
        return msgGroups;
    }

    public Version getVersion() {
        return version;
    }

    public String getAppName() {
        return appName;
    }

    public DataOutputStream getDataOutputStream() {
        return dataOutputStream;
    }

    protected Message getNextMessage()
    throws IOException, JSONException {
        try {
            int groupId = dataInputStream.readInt();
            int msgId = dataInputStream.readInt();
            int size = dataInputStream.readInt();

            byte[] buffer = new byte[size];
            int len = dataInputStream.read(buffer, 0, size);

            JSONDecoder decoder = new JSONDecoder(new JSONStringReader(new JSONStreamReaderBytes(buffer, len)));
            return new Message(groupId, msgId, decoder.decode(), this);
        } catch(IOException e) {
            if(closing) {
                return null;
            }
            throw new IOException(e);
        }
    }

    @SuppressWarnings("unchecked")
    private void init()
    throws IOException, JSONException {
        Message msg = getNextMessage();
        if(
            ClientMessage.GROUP_ID != msg.getGroupId() ||
            ClientMessage.START.getMessageId() != msg.getMessageId()
        ) {
            throw new IOException("first msg is not CLIENT_START");
        }
        Map<String, Object> map = (Map<String, Object>)msg.getData();
        appName = (String)map.get("appName");
        version = new Version((String)map.get("version"));
        Object o = map.get("msgGroups");
        if(!(o instanceof ArrayList)) {
            throw new IOException("invalid msg groups given");
        }
        msgGroups = (ArrayList<Long>)o;
        in.add(msg);
    }
}
