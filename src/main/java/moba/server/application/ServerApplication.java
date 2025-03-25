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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import moba.server.com.Acceptor;
import moba.server.com.Dispatcher;
import moba.server.database.Database;
import moba.server.database.DatabaseException;
import moba.server.datatypes.base.Version;
import moba.server.datatypes.objects.helper.ActiveLayout;
import moba.server.messagehandler.Client;
import moba.server.messagehandler.Control;
import moba.server.messagehandler.Environment;
import moba.server.messagehandler.Timer;
import moba.server.messagehandler.Interface;
import moba.server.messagehandler.Layout;
import moba.server.messagehandler.Server;
import moba.server.messagehandler.Systems;
import moba.server.messages.MessageLoop;
import moba.server.messages.MessageQueue;
import moba.server.utilities.config.Config;
import moba.server.utilities.lock.TrackLayoutLock;
import moba.server.utilities.logger.MessageLogger;

final public class ServerApplication {

    private int           maxClients = -1;

    private final Version appVer;
    private final String  appName;
    private final Date    buildDate;
    private final long    startTime;

    private final Config        config;
    private final MessageQueue  msgQueueIn;

    public ServerApplication(String appName, Version appVer, Date date, Config config) {
        this.appVer     = appVer;
        this.appName    = appName;
        this.startTime  = System.currentTimeMillis();
        this.buildDate  = date;
        this.config     = config;
        this.msgQueueIn = new MessageQueue(new MessageLogger());
    }

    public Version getVersion() {
        return appVer;
    }

    public long getStartTime() {
        return startTime;
    }

    public String getAppName() {
        return appName;
    }

    public Date getBuildDate() {
        return buildDate;
    }

    public int getMaxClients() {
        return maxClients;
    }

    @SuppressWarnings("unchecked")
    public void run()
    throws Exception {
        try {
            boolean restart;
            maxClients = (int)(long)config.getSection("common.serverConfig.maxClients");
            int port = (int)(long)config.getSection("common.serverConfig.port");
            var allowList = (ArrayList<String>)config.getSection("common.serverConfig.allowedIPs");

            do {
                Dispatcher dispatcher = new Dispatcher(new MessageLogger());
                Acceptor acceptor = new Acceptor(msgQueueIn, dispatcher, port, maxClients, allowList);
                Database database = new Database((HashMap<String, Object>)config.getSection("common.database"));
                MessageLoop  loop = new MessageLoop(dispatcher);
                ActiveLayout activeLayout = new ActiveLayout(dispatcher, config);
                TrackLayoutLock lock = new TrackLayoutLock(database);

                loop.addHandler(new Client(dispatcher));
                loop.addHandler(new Server(dispatcher, this));
                loop.addHandler(new Timer(dispatcher, config));
                loop.addHandler(new Environment(dispatcher, config));
                loop.addHandler(new Systems(dispatcher, lock, activeLayout, msgQueueIn));
                loop.addHandler(new Layout(dispatcher, database, activeLayout));
                loop.addHandler(new Interface(dispatcher, msgQueueIn));
                loop.addHandler(new Control(dispatcher, database, activeLayout));
                acceptor.startAcceptor();
                restart = loop.loop(msgQueueIn);
                dispatcher.resetDispatcher();
                acceptor.stopAcceptor();
            } while(restart);
        } catch(DatabaseException | InterruptedException e) {
            throw new Exception(e);
        }
    }
}
