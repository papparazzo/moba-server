/*
 *  common
 *
 *  Copyright (C) 2013 Stefan Paproth <pappi-@gmx.de>
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
package app;

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
    protected PriorityBlockingQueue<Message> in = null;

    public void run(
        String appName, Version appVer, Date date, Config config
    )
    throws IOException {
        this.appVer    = appVer;
        this.appName   = appName;
        this.startTime = System.currentTimeMillis();
        this.buildDate = date;
        this.config    = config;
        this.in        = new PriorityBlockingQueue<>();

        this.setUpLogger();
        this.loop();
    }

    abstract protected void loop();

    public Version getVersion() {
        return this.appVer;
    }

    public long getStartTime() {
        return this.startTime;
    }

    public String getAppName() {
        return this.appName;
    }

    public Date getBuildDate() {
        return this.buildDate;
    }

    public PriorityBlockingQueue<Message> getQueue() {
        return this.in;
    }

    protected void setUpLogger()
    throws IOException {
        //Level level    = Level.ALL;

        String logfile = (String)this.config.getSection("common.logging.logfilepath");
        Level level = Level.parse((String)this.config.getSection("common.logging.level"));

        Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
        logger.setUseParentHandlers(false);
        logger.setLevel(level);

        if(logfile != null && !logfile.isEmpty()) {
            FileHandler fh = new FileHandler(logfile);
            fh.setFormatter(
                new CustomFormatter(this.appName, this.appVer, this.buildDate)
            );
            fh.setLevel(level);
            logger.addHandler(fh);
        }

        ConsoleHandler ch = new ConsoleHandler();
        ch.setFormatter(
            new CustomFormatter(this.appName, this.appVer, this.buildDate)
        );
        ch.setLevel(level);
        logger.addHandler(ch);
    }
}
