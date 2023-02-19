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
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;

import moba.server.messages.MessageQueue;
import moba.server.utilities.logger.Loggable;

public class Acceptor extends Thread implements Loggable {

    private MessageQueue in = null;

    private ServerSocket serverSocket = null;
    private Dispatcher   dispatcher   = null;
    private int          serverport   = 0;
    private int          maxClients   = 0;

    public Acceptor(MessageQueue in, Dispatcher dispatcher, int serverport, int maxClients) {
        this.in         = in;
        this.dispatcher = dispatcher;
        this.serverport = serverport;
        this.maxClients = maxClients;
    }

    public void startAcceptor() {
        setName("acceptor");
        start();
    }

    public void stopAcceptor() {
        try {
            if(serverSocket != null) {
                serverSocket.close();
            }
            interrupt();
            join(250);
        } catch(IOException e) {
            getLogger().log(Level.WARNING, "could not close server socket! <{0}>", new Object[]{e.toString()});
        } catch(InterruptedException e) {
            getLogger().log(Level.WARNING, "InterruptedException occured! <{0}>", new Object[]{e.toString()});
        }
        getLogger().info("Acceptor sucessfull stoped.");
    }

    @Override
    public void run() {
        long    id = 0;
        boolean isinit = false;

        getLogger().info("acceptor-thread started");

        try {
            do {
                try {
                    serverSocket = new ServerSocket(serverport);
                    isinit = true;
                } catch(IOException e) {
                    getLogger().log(Level.WARNING, "binding on port <{0}> failed! <{1}>", new Object[]{this.serverport, e.toString()});
                    Thread.sleep(2500);
                }
            } while(!isinit && !isInterrupted());

            getLogger().log(Level.INFO, "Succesfull bind on port <{0}>", new Object[]{serverport});

            while(!isInterrupted()) {
                Socket socket = serverSocket.accept();
                getLogger().log(Level.INFO, "new client <{0}> socket <{1}>", new Object[]{++id, socket.toString()});
                if(dispatcher.getEndPointsCount() == maxClients) {
                    getLogger().log(Level.SEVERE, "Max amount of clients <{0}> connected!", new Object[]{maxClients});
                    break;
                }
                (new Endpoint(id, socket, in)).start();
            }
        } catch (InterruptedException | IOException e) {
            getLogger().log(Level.WARNING, "<{0}>", new Object[]{e.toString()});
        }
        getLogger().info("acceptor-thread terminated");
    }
}