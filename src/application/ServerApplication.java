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

package application;

import java.util.HashMap;

import application.Application;
import com.Acceptor;
import com.Dispatcher;
import database.Database;
import database.DatabaseException;
import messagehandler.Environment;
import messagehandler.GlobalTimer;
import messagehandler.Layout;
import messagehandler.Link;
import messagehandler.Server;
import messagehandler.Systems;
import messages.MessageLoop;
import messages.MessageType;

public class ServerApplication extends Application {

    protected int maxClients = -1;

    @Override
    protected void loop() throws Exception {
        try {
            boolean restart;
            this.maxClients = (int)(long)this.config.getSection("common.serverConfig.maxClients");
            do {
                Dispatcher dispatcher = new Dispatcher();
                Acceptor acceptor = new Acceptor(
                    this.in,
                    dispatcher,
                    (int)(long)this.config.getSection("common.serverConfig.port"),
                    this.maxClients
                );
                MessageLoop loop = new MessageLoop(dispatcher);
                loop.addHandler(MessageType.MessageGroup.CLIENT, new Link(dispatcher, this.in));
                loop.addHandler(MessageType.MessageGroup.SERV, new Server(dispatcher, this));
                loop.addHandler(MessageType.MessageGroup.TIMER, new GlobalTimer(dispatcher, this.config));
                loop.addHandler(MessageType.MessageGroup.ENV, new Environment(dispatcher, this.config));
                loop.addHandler(MessageType.MessageGroup.SYSTEM, new Systems(dispatcher, this.in));
                loop.addHandler(MessageType.MessageGroup.LAYOUT, new Layout(dispatcher, new Database((HashMap<String, Object>)this.config.getSection("common.database"))));

                acceptor.startAcceptor();
                restart = loop.loop(this.in);
                dispatcher.resetDispatcher();
                acceptor.stopAcceptor();
            } while(restart);
        } catch(DatabaseException | InterruptedException e) {
            throw new Exception(e);
        }
    }

    public int getMaxClients() {
        return this.maxClients;
    }
}
