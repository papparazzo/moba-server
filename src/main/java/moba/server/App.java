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
import java.util.TimeZone;
import moba.server.application.ServerApplication;

public class App {
    protected static final String INI_FILE_NAME = "config.yaml";
    protected static final String APP_NAME      = "moba-server";
    protected static final String APP_DATE      = "23.03.2025";
    protected static final String APP_VERSION   = "1.2.0-0000";
    protected static final String APP_AUTHOR    = "Stefan Paproth (Pappi-@gmx.de)";

    public static void main(String[] args) {
        ServerApplication app = new ServerApplication();

        try {
            TimeZone.setDefault(TimeZone.getTimeZone("Europe/Berlin"));
            DateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");
            app.run(
                App.APP_NAME,
                new Version(App.APP_VERSION),
                App.APP_AUTHOR,
                formatter.parse(App.APP_DATE),
                new Config(App.INI_FILE_NAME)
            );
        } catch(Exception e) {
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
            System.err.println(e.getMessage());
            System.exit(1);
        } finally {
            System.exit(0);
        }
    }
}
