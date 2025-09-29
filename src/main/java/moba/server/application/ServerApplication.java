/*
 *  Project:    moba-server
 *
 *  Copyright (C) 2025 Stefan Paproth <pappi-@gmx.de>
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

package moba.server.application;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.logging.Logger;

import moba.server.actionhandler.Interlock;
import moba.server.actionhandler.Scheduler;
import moba.server.actionhandler.TrainRunner;
import moba.server.actionhandler.TrainRunnerInitializer;
import moba.server.com.Acceptor;
import moba.server.com.BackgroundHandlerComposite;
import moba.server.com.Dispatcher;
import moba.server.com.IPC;
import moba.server.com.KeepAlive;
import moba.server.messagehandler.*;
import moba.server.repositories.*;
import moba.server.timedaction.FunctionExecution;
import moba.server.timedaction.TrainRun;
import moba.server.utilities.Database;
import moba.server.datatypes.base.Version;
import moba.server.datatypes.objects.IncidentData;
import moba.server.utilities.layout.ActiveTrackLayout;
import moba.server.messages.MessageLoop;
import moba.server.messages.MessageQueue;
import moba.server.utilities.AllowList;
import moba.server.utilities.config.Config;
import moba.server.utilities.layout.TrackLayoutLock;
import moba.server.utilities.messaging.IncidentHandler;
import moba.server.utilities.logger.Loggable;
import moba.server.utilities.logger.MessageLogger;
import org.apache.commons.collections4.queue.CircularFifoQueue;

final public class ServerApplication implements Loggable {

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
        this.msgQueueIn = new MessageQueue(new MessageLogger(getLogger()));
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
        Logger logger = getLogger();
        boolean restart;
        maxClients = (int)(long)config.getSection("common.serverConfig.maxClients");
        int port = (int)(long)config.getSection("common.serverConfig.port");
        var allowed = (ArrayList<String>)config.getSection("common.serverConfig.allowedIPs");
        int maxEntries = (int)(long)config.getSection("common.serverConfig.maxIncidentEntries");
        int keepAlivePingIntervall = (int)(long)config.getSection("common.serverConfig.keepAlivePingIntervall");
        AllowList allowList = new AllowList(maxClients, allowed);
        CircularFifoQueue<IncidentData> list = new CircularFifoQueue<>(maxEntries);

        do {
            Dispatcher dispatcher = new Dispatcher(new MessageLogger(logger), logger);
            Database database = new Database((HashMap<String, Object>)config.getSection("common.database"), logger);
            ActiveTrackLayout activeLayout = new ActiveTrackLayout(dispatcher, config);
            TrackLayoutLock trackLayoutLock = new TrackLayoutLock(database);
            IncidentHandler incidentHandler = new IncidentHandler(logger, dispatcher, list);
            TrainRunner trainRunner = (new TrainRunnerInitializer(database)).getTrainRunner(activeLayout.getActiveLayout());
            Interlock interlock = new Interlock(database);
            TrackLayoutRepository trackLayoutRepository = new TrackLayoutRepository(database);

            BlockListRepository blocklistRepository = new BlockListRepository(database);
            SwitchStateRepository switchStateRepository = new SwitchStateRepository(database);
            TrainlistRepository trainlistRepository = new TrainlistRepository(database);
            TrainRepository trainRepository = new TrainRepository();

            Scheduler scheduler = new Scheduler(dispatcher, null);
            scheduler.addTimedAction(new FunctionExecution(new FunctionTimeTableRepository(database), dispatcher));
            scheduler.addTimedAction(new TrainRun(new TrainTimeTableRepository(database), trainRunner, trainRepository));

            BackgroundHandlerComposite handler = new BackgroundHandlerComposite();
            handler.add(new Acceptor(msgQueueIn, dispatcher, port, maxClients, allowList, incidentHandler));
            handler.add(new IPC((String)config.getSection("common.serverConfig.ipc"), msgQueueIn, logger));
            handler.add(new KeepAlive(dispatcher, keepAlivePingIntervall, logger));

            MessageLoop  loop = new MessageLoop(dispatcher, incidentHandler);
            loop.addHandler(new Client(dispatcher, msgQueueIn));
            loop.addHandler(new Server(dispatcher, this, allowList, config));
            loop.addHandler(new Timer(dispatcher, config, scheduler));
            loop.addHandler(new Environment(dispatcher, new FunctionAddressesRepository(database)));
            loop.addHandler(new Systems(dispatcher, trackLayoutLock, activeLayout, msgQueueIn, incidentHandler));
            loop.addHandler(new Layout(dispatcher, trackLayoutRepository, activeLayout, trackLayoutLock));
            loop.addHandler(new Interface(dispatcher, msgQueueIn, incidentHandler, trainRunner));
            loop.addHandler(new Control(dispatcher, blocklistRepository, switchStateRepository, trainlistRepository, activeLayout, trackLayoutLock));
            loop.addHandler(new Messaging(dispatcher, list));

            handler.start();
            restart = loop.loop(msgQueueIn);
            dispatcher.resetDispatcher();
            handler.halt();
        } while(restart);
    }
}
