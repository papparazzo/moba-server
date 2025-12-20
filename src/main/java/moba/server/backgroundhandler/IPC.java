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

package moba.server.backgroundhandler;

import moba.server.messages.Message;
import moba.server.messages.MessageQueue;
import moba.server.messages.messagetypes.InternMessage;
import moba.server.messages.messagetypes.ServerMessage;
import moba.server.messages.messagetypes.SystemMessage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

final public class IPC extends Thread implements BackgroundHandlerInterface {

    private final MessageQueue msgQueue;
    private final String       fifoFile;
    private final Logger       logger;

    public IPC(String fifoFile, MessageQueue msgQueue, Logger logger) {
        this.msgQueue = msgQueue;
        this.fifoFile = fifoFile;
        this.logger   = logger;
    }

    public void halt() {
        try {
            interrupt();
            join(250);
        } catch(InterruptedException e) {
            logger.log(Level.WARNING, "InterruptedException occurred! <{0}>", new Object[]{e.toString()});
        }
        logger.info("ipc-thread stopped.");
    }

    public void start() {
        setName("ipc");

        try {
            File f = new File(fifoFile);
            if(!f.exists()) {
                new ProcessBuilder("bash", "-c", "mkfifo " + fifoFile).inheritIO().start();
                new ProcessBuilder("bash", "-c", "chmod 666 " + fifoFile).inheritIO().start();
            } else {
                var attr = Files.readAttributes(f.toPath(), BasicFileAttributes.class);
                if(!attr.isOther()) {
                    throw new RuntimeException("<" + fifoFile + "> is not a named pipe");
                }
            }
        } catch(IOException e) {
            throw new RuntimeException(e);
        }
        super.start();
    }

    @Override
    public void run() {
        logger.info("ipc-thread started");

        while(!isInterrupted()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(fifoFile))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    handleAction(line);
                }
            } catch (Exception e) {
               logger.log(Level.WARNING, "<{0}>", new Object[]{e.toString()});
            }
        }
        logger.info("ipc-thread terminated");
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

            case "SERVER_SHUTDOWN":
                msgQueue.add(new Message(InternMessage.SERVER_SHUTDOWN));
                break;

            case "SERVER_RESET":
                msgQueue.add(new Message(InternMessage.SERVER_RESET));
                break;

            case "SYSTEM_SHUTDOWN":
                msgQueue.add(new Message(SystemMessage.HARDWARE_SHUTDOWN));
                break;

            case "SYSTEM_RESET":
                msgQueue.add(new Message(SystemMessage.HARDWARE_RESET));
                break;

            default:
                logger.log(Level.WARNING, "unknown command <{0}> given", new Object[]{parts[0]});
         }
    }

    private ArrayList<String> getAsArrayList(String part)
    {
        return new ArrayList<>(Arrays.asList(part.split(";")));
    }
}
