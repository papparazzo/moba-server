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

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.MessageFormat;
import java.util.logging.Level;

import moba.server.datatypes.enumerations.IncidentLevel;
import moba.server.datatypes.enumerations.IncidentType;
import moba.server.messages.MessageQueue;
import moba.server.utilities.AllowList;
import moba.server.datatypes.objects.IncidentData;
import moba.server.utilities.messaging.IncidentHandler;
import moba.server.utilities.logger.Loggable;

final public class Acceptor extends Thread implements Loggable, BackgroundHandlerInterface {

    private ServerSocket          serverSocket = null;
    private final MessageQueue    msgQueue;
    private final Dispatcher      dispatcher;
    private final int             serverPort;
    private final int             maxClients;
    private final AllowList       allowList;
    private final IncidentHandler incidentHandler;

    public Acceptor(
        MessageQueue msgQueue,
        Dispatcher dispatcher,
        int serverPort,
        int maxClients,
        AllowList allowList,
        IncidentHandler incidentHandler
    ) {
        this.msgQueue        = msgQueue;
        this.dispatcher      = dispatcher;
        this.serverPort      = serverPort;
        this.maxClients      = maxClients;
        this.allowList       = allowList;
        this.incidentHandler = incidentHandler;
    }

    public void start() {
        setName("acceptor");
        try {
            serverSocket = new ServerSocket(serverPort);
        } catch(IOException e) {
            throw new RuntimeException(e);
        }

        getLogger().log(Level.INFO, "Successful bind on port <{0}>", new Object[]{serverPort});
        super.start();
    }

    public void halt() {
        try {
            if(serverSocket != null) {
                serverSocket.close();
            }
            interrupt();
            join(250);
        } catch(IOException e) {
            getLogger().log(Level.WARNING, "could not close server socket! <{0}>", new Object[]{e.toString()});
        } catch(InterruptedException e) {
            getLogger().log(Level.WARNING, "InterruptedException occurred! <{0}>", new Object[]{e.toString()});
        }
        getLogger().info("Acceptor successful stopped.");
    }

    @Override
    public void run() {
        long id = 0;

        getLogger().info("acceptor-thread started");

        while(!isInterrupted()) {
            try {
                Socket socket = serverSocket.accept();
                getLogger().log(Level.INFO, "new client <{0}> socket <{1}>", new Object[]{++id, socket.toString()});

                if(!allowedOrigin(socket)) {
                    socket.close();
                    continue;
                }
                if(dispatcher.getEndPointsCount() == maxClients) {
                    socket.close();
                    incidentHandler.add(
                        new IncidentData(
                            IncidentLevel.WARNING,
                            IncidentType.SERVER_NOTICE,
                            "Max amount of clients",
                            MessageFormat.format("Max amount of clients <{0}> connected!", maxClients),
                            "Acceptor.run()"
                        )
                    );
                    continue;
                }
                (new Endpoint(id, socket, msgQueue)).start();
            } catch (Exception e) {
                getLogger().log(Level.WARNING, "<{0}>", new Object[]{e.toString()});
                incidentHandler.add(new IncidentData(e));
            }
        }
        getLogger().info("acceptor-thread terminated");
    }

    private boolean allowedOrigin(Socket socket) {
        InetAddress addr = socket.getInetAddress();

        if(allowList.isAllowed(addr)) {
            return true;
        }
        getLogger().log(Level.WARNING, "access of ip <{0}> is forbidden!", new Object[]{addr});
        return false;
    }
}
