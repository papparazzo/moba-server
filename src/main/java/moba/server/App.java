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

package moba.server;

import moba.server.datatypes.base.Version;
import moba.server.utilities.config.Config;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.logging.*;

import moba.server.application.ServerApplication;
import moba.server.utilities.logger.CustomFormatter;

final public class App {
    private static final String APP_CONFIG  = "config.yaml";
    private static final String APP_NAME    = "moba-server";
    private static final String APP_DATE    = "25.03.2025";
    private static final String APP_VERSION = "1.3.1-0000";

    public static void main(String[] args) {
        try {
            TimeZone.setDefault(TimeZone.getTimeZone("Europe/Berlin"));
            DateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");
            Version ver = new Version(App.APP_VERSION);
            setUpLogger(ver, formatter.parse(App.APP_DATE));
            ServerApplication app = new ServerApplication(
                App.APP_NAME,
                ver,
                formatter.parse(App.APP_DATE),
                new Config(App.APP_CONFIG)
            );
            app.run();
        } catch(Exception e) {
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
            System.err.println(e.getMessage());
            System.exit(1);
        } finally {
            System.exit(0);
        }
    }

    private static void setUpLogger(Version ver, Date buildDate) {
        Level level = Level.INFO;

        Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
        logger.setUseParentHandlers(false);
        logger.setLevel(level);

        ConsoleHandler ch = new ConsoleHandler();
        ch.setFormatter(new CustomFormatter(App.APP_NAME, ver, buildDate));
        ch.setLevel(level);
        logger.addHandler(ch);
    }
}
