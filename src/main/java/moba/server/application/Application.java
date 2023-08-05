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

import java.io.IOException;
import java.util.Date;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import moba.server.datatypes.base.Version;
import moba.server.messages.MessageQueue;
import moba.server.utilities.config.Config;
import moba.server.utilities.logger.CustomFormatter;
import moba.server.utilities.logger.MessageLogger;

abstract public class Application {
    protected Version      appVer;
    protected String       appName;
    protected Date         buildDate;
    protected long         startTime;

    protected Config       config = null;
    protected MessageQueue msgQueueIn = null;

    public void run(String appName, Version appVer, Date date, Config config)
    throws Exception {
        this.appVer     = appVer;
        this.appName    = appName;
        this.startTime  = System.currentTimeMillis();
        this.buildDate  = date;
        this.config     = config;
        this.msgQueueIn = new MessageQueue(new MessageLogger());

        setUpLogger();
        loop();
    }

    abstract protected void loop()
    throws Exception;

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

    protected void setUpLogger()
    throws IOException {
        String logfile = (String)config.getSection("common.logging.logfilepath");
        Level level = Level.parse((String)config.getSection("common.logging.level"));

        Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
        logger.setUseParentHandlers(false);
        logger.setLevel(level);

        if(logfile != null && !logfile.isEmpty()) {
            FileHandler fh = new FileHandler(logfile);
            fh.setFormatter(new CustomFormatter(appName, appVer, buildDate));
            fh.setLevel(level);
            logger.addHandler(fh);
        }

        ConsoleHandler ch = new ConsoleHandler();
        ch.setFormatter(new CustomFormatter(appName, appVer, buildDate));
        ch.setLevel(level);
        logger.addHandler(ch);
    }
}
