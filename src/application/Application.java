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

import java.io.IOException;
import java.util.Date;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import datatypes.base.Version;
import messages.Message;
import utilities.config.Config;
import utilities.logger.CustomFormatter;

abstract public class Application {
    protected Version    appVer;
    protected String     appName;
    protected Date       buildDate;
    protected long       startTime;

    protected Config config = null;
    protected PriorityBlockingQueue<Message> msgQueue = null;

    public void run(String appName, Version appVer, Date date, Config config)
    throws IOException, Exception {
        this.appVer    = appVer;
        this.appName   = appName;
        this.startTime = System.currentTimeMillis();
        this.buildDate = date;
        this.config    = config;
        this.msgQueue  = new PriorityBlockingQueue<>();

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

    public PriorityBlockingQueue<Message> getQueue() {
        return msgQueue;
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
