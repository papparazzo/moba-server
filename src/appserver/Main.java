/*
 *  appserver2
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

package appserver;

import utilities.config.Config;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

import utilities.*;

public class Main {

    protected static final String INI_FILE_NAME = "config.json";
    protected static final String APP_NAME      = "appserver";
    protected static final String APP_DATE      = "13.05.2011";
    protected static final String APP_VERSION   = "1.0.0-0000";

    public static void main(String[] args)
    throws Exception {
        ServerApplication appl = new ServerApplication();

        try {
            TimeZone.setDefault(TimeZone.getTimeZone("Europe/Berlin"));
            DateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");
            appl.run(Main.APP_NAME,
                new Version(Main.APP_VERSION),
                formatter.parse(Main.APP_DATE),
                new Config(Main.INI_FILE_NAME)
            );
        } catch(Exception e) {
            e.printStackTrace();
            System.exit(1);
        } finally {
            System.exit(0);
        }
    }
}
