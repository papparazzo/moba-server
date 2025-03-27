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
import org.apache.commons.cli.*;

final public class App {
    private static final String APP_CONFIG  = "config.yaml";
    private static final String APP_NAME    = "moba-server";
    private static final String APP_DATE    = "2025-03-25";
    private static final String APP_VERSION = "1.4.0-0000";

    public static void main(String[] args) {
/*
        //try {
             Enumeration<URL> resources = App.class.getClassLoader()
   .getResources("META-INF/MANIFEST.MF");

             while (resources.hasMoreElements()) {
     try {
       Manifest manifest = new Manifest(resources.nextElement().openStream());
                   Attributes mainAttributes = manifest.getMainAttributes();
            String implVersion = mainAttributes.getValue("Built-By");

            System.out.println(implVersion);


       // If the line above leads to <null> manifest Attributes try from JarInputStream:
       // Manifest manifest = resources.nextElement().openStream().getManifest();

       // check that this is your manifest and do what you need or get the next one

     } catch (IOException E) {
       // handle
     }
 }



                     URLClassLoader cl = (URLClassLoader) App.class.getClassLoader();
            URL url = cl.findResource("META-INF/MANIFEST.MF");
            Manifest manifest = new Manifest(url.openStream());
            Attributes mainAttributes = manifest.getMainAttributes();
            String implVersion = mainAttributes.getValue("Implementation-Version");

            System.out.println(implVersion);
        } catch (IOException E) {
            // handle
        }
*/

        try {
            CommandLineParser parser = new DefaultParser();
            CommandLine cmd = parser.parse(getOptions(), args);
            if(cmd.hasOption("h")) {
                printHelp();
                System.exit(0);
            }
            if(cmd.hasOption("v")) {
                printInfo();
                System.exit(0);
            }

            TimeZone.setDefault(TimeZone.getTimeZone("Europe/Berlin"));
            DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
            Version ver = new Version(App.APP_VERSION);
            setUpLogger(ver, formatter.parse(App.APP_DATE));
            ServerApplication app = new ServerApplication(
                App.APP_NAME,
                ver,
                formatter.parse(App.APP_DATE),
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

    private static Options getOptions() {
        return
            new Options().
            addOption("h", "help", false, "print this message").
            addOption("c", "config", true, "configuration-file").
            addOption("v", "version", false, "print version");
    }

    private static void printInfo() {
        String header =
            App.APP_NAME + " " + App.APP_VERSION + " (build on " + App.APP_DATE + ")" +
            System.lineSeparator() + System.lineSeparator() +
           "Copyright (C) 2016 Stefan Paproth <pappi-@gmx.de>" + System.lineSeparator() +
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
           "along with this program. If not, see <http://www.gnu.org/licenses/agpl.txt>.";
        System.err.println(header);
    }

    private static void printHelp() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp( App.APP_NAME + " [OPTION]", System.lineSeparator(), getOptions(), "");
    }
}
