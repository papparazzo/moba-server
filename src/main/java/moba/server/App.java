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

package moba.server;

import moba.server.datatypes.base.Version;
import moba.server.utilities.ManifestReader;
import moba.server.utilities.config.Config;

import java.util.Date;
import java.util.TimeZone;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import moba.server.application.ServerApplication;
import moba.server.utilities.logger.CustomFormatter;

import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.HelpFormatter;

final public class App {
    private static final String APP_CONFIG  = "config.yaml";
    private static final String APP_NAME    = "moba-server";

    public static void main(String[] args) {
        try {
            ManifestReader manifest = new ManifestReader(App.class);
            CommandLineParser parser = new DefaultParser();
            CommandLine cmd = parser.parse(getOptions(), args);

            String appName = manifest.getAppName();
            Version appVersion = manifest.getVersion();
            Date buildDate = manifest.getBuildDate();

            if(cmd.hasOption("h")) {
                printHelp(appName);
                System.exit(0);
            }
            if(cmd.hasOption("v")) {
                printInfo(appName, appVersion, buildDate);
                System.exit(0);
            }

            TimeZone.setDefault(TimeZone.getTimeZone("Europe/Berlin"));
            setUpLogger(appName, appVersion, buildDate);
            ServerApplication app = new ServerApplication(
                appName,
                appVersion,
                buildDate,
                new Config(cmd.getOptionValue("c", App.APP_CONFIG))
            );
            app.run();
            System.exit(0);
        } catch(ParseException exp) {
            System.err.println(App.APP_NAME + " : " + exp.getMessage());
            System.err.println("Try '" + App.APP_NAME + " --help' for more information.");
            System.exit(2);
        } catch(Throwable e) {
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }

    private static void setUpLogger(String appName, Version ver, Date buildDate) {
        Level level = Level.INFO;

        Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
        logger.setUseParentHandlers(false);
        logger.setLevel(level);

        ConsoleHandler ch = new ConsoleHandler();
        ch.setFormatter(new CustomFormatter(appName, ver, buildDate));
        ch.setLevel(level);
        logger.addHandler(ch);
    }

    private static Options getOptions() {
        return
            new Options().
            addOption("h", "help", false, "print this message").
            addOption("c", "config", true, "configuration-file").
            addOption("v", "version", false, "print version");
    }

    private static void printInfo(String appName, Version appVersion, Date buildDate) {
        String header =
            appName + " " + appVersion.toString() + " (build on " + buildDate + ")" +
            System.lineSeparator() + System.lineSeparator() +
           "Copyright (C) 2025 Stefan Paproth <pappi-@gmx.de>" + System.lineSeparator() +
           System.lineSeparator() +
           "This program is free software: you can redistribute it and/or modify" + System.lineSeparator() +
           "it under the terms of the GNU Affero General Public License as" + System.lineSeparator() +
           "published by the Free Software Foundation, either version 3 of the" + System.lineSeparator() +
           "License, or (at your option) any later version." + System.lineSeparator() +
           System.lineSeparator() +
           "This program is distributed in the hope that it will be useful," + System.lineSeparator() +
           "but WITHOUT ANY WARRANTY; without even the implied warranty of" + System.lineSeparator() +
           "MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the" + System.lineSeparator() +
           "GNU Affero General Public License for more details." + System.lineSeparator() +
           System.lineSeparator() +
           "You should have received a copy of the GNU Affero General Public License" + System.lineSeparator() +
           "along with this program. If not, see <https://www.gnu.org/licenses/agpl.txt>.";
        System.err.println(header);
    }

    private static void printHelp(String appName) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp( appName + " [OPTION]", System.lineSeparator(), getOptions(), "");
    }
}
