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

import moba.server.messages.Message;
import moba.server.messages.MessageQueue;
import moba.server.messages.messageType.InternMessage;
import moba.server.messages.messageType.ServerMessage;
import moba.server.utilities.logger.Loggable;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;

final public class IPC extends Thread implements Loggable, BackgroundHandlerInterface {

    private final MessageQueue msgQueue;
    private final String       fifoFile;

    public IPC(String fifoFile, MessageQueue msgQueue) {
        this.msgQueue = msgQueue;
        this.fifoFile = fifoFile;
    }

    public void halt() {
        try {
            interrupt();
            join(250);
        } catch(InterruptedException e) {
            getLogger().log(Level.WARNING, "InterruptedException occurred! <{0}>", new Object[]{e.toString()});
        }
        getLogger().info("IPC successful stopped.");
    }

    public void start() {
        setName("ipc");
        super.start();
    }

    @Override
    public void run() {
        getLogger().info("ipc-thread started");

        while(!isInterrupted()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(fifoFile))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    handleAction(line);
                }
            } catch (Exception e) {
               getLogger().log(Level.WARNING, "<{0}>", new Object[]{e.toString()});
            }
        }
        getLogger().info("ipc-thread terminated");
    }

    private void handleAction(String line) {
        String[] parts = line.split(":");

         switch(parts[0]) {
            case "SET_ALLOWED_IP_LIST":
                msgQueue.add(new Message(ServerMessage.SET_ALLOWED_IP_LIST, getAsArrayList(parts[1])));
                break;

            case "ADD_ALLOWED_IP":
                msgQueue.add(new Message(ServerMessage.ADD_ALLOWED_IP, parts[1]));
                break;

            case "HARDWARE_SHUTDOWN":
                msgQueue.add(new Message(InternMessage.SERVER_SHUTDOWN, null));
                break;

            case "HARDWARE_RESET":
                msgQueue.add(new Message(InternMessage.SERVER_RESET, null));
                break;

            default:
                getLogger().log(Level.WARNING, "unknown command <{0}> given", new Object[]{parts[0]});
         }
    }

    private ArrayList<String> getAsArrayList(String part)
    {
        return new ArrayList<>(Arrays.asList(part.split(";")));
    }
}
