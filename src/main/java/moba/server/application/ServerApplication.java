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

package moba.server.application;

import java.util.HashMap;

import moba.server.com.Acceptor;
import moba.server.com.Dispatcher;
import moba.server.database.Database;
import moba.server.database.DatabaseException;
import moba.server.messagehandler.Client;
import moba.server.messagehandler.Control;
import moba.server.messagehandler.Environment;
import moba.server.messagehandler.Timer;
import moba.server.messagehandler.Interface;
import moba.server.messagehandler.Layout;
import moba.server.messagehandler.Server;
import moba.server.messagehandler.Systems;
import moba.server.messages.MessageLoop;

public class ServerApplication extends Application {

    protected int maxClients = -1;

    @Override
    protected void loop()
    throws Exception {
        try {
            boolean restart;
            maxClients = (int)(long)config.getSection("common.serverConfig.maxClients");
            do {
                Dispatcher dispatcher = new Dispatcher();
                Acceptor acceptor = new Acceptor(msgQueueIn, dispatcher, (int)(long)config.getSection("common.serverConfig.port"), maxClients);
                Database database = new Database((HashMap<String, Object>)config.getSection("common.database"));
                MessageLoop loop = new MessageLoop(dispatcher);
                loop.addHandler(new Client(dispatcher));
                loop.addHandler(new Server(dispatcher, this));
                loop.addHandler(new Timer(dispatcher, config));
                loop.addHandler(new Environment(dispatcher, config));
                loop.addHandler(new Systems(dispatcher, msgQueueIn));
                loop.addHandler(new Layout(dispatcher, database, config));
                loop.addHandler(new Interface(dispatcher, msgQueueIn));
                loop.addHandler(new Control(dispatcher, database, config));
                acceptor.startAcceptor();
                restart = loop.loop(msgQueueIn);
                dispatcher.resetDispatcher();
                acceptor.stopAcceptor();
            } while(restart);
        } catch(DatabaseException | InterruptedException e) {
            throw new Exception(e);
        }
    }

    public int getMaxClients() {
        return maxClients;
    }
}
