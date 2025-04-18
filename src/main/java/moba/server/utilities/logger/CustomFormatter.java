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
 *  along with this program. If not, see <https://www.gnu.org/licenses/agpl.txt>.
 *
 */

package moba.server.utilities.logger;

import moba.server.datatypes.base.Version;

import java.util.logging.*;
import java.io.*;
import java.text.*;
import java.util.Date;

public class CustomFormatter extends Formatter {

    private final String  appName;
    private final Version appVersion;
    private final Date    buildDate;

    public CustomFormatter(String appName, Version appVersion, Date buildDate) {
        this.appName = appName;
        this.appVersion = appVersion;
        this.buildDate = buildDate;
    }

    @Override
    public synchronized String format(LogRecord record) {
        StringBuilder sb = new StringBuilder();

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSS ");
        sb.append(df.format(new Date()));
        sb.append(record.getLevel().getLocalizedName());
        sb.append(" ");

        if(record.getSourceClassName() != null) {
            var name = record.getSourceClassName();
            sb.append(name, name.lastIndexOf(".") + 1, name.length());
        } else {
            sb.append(record.getLoggerName());
        }

        if(record.getSourceMethodName() != null) {
            sb.append(".");
            sb.append(record.getSourceMethodName());
            sb.append("()");
        }
        sb.append(" ");
        sb.append(formatMessage(record));
        sb.append("\n");
        if(record.getThrown() != null) {
            try {
                StringWriter sw = new StringWriter();
                try(PrintWriter pw = new PrintWriter(sw)) {
                    record.getThrown().printStackTrace(pw);
                }
                sb.append(sw);
            } catch(Exception ex) {
                // noop
            }
        }
        return sb.toString();
    }

    @Override
    public String getHead(Handler h) {
        return
            "-".repeat(108) + "\n" +
            "  name:     " + appName + "\n" +
            "  version:  " + appVersion + "\n" +
            "  build on: " + buildDate + "\n" +
            "-".repeat(108) + "\n";
    }

    @Override
    public String getTail(Handler h) {
        return "-".repeat(108) + "\n";
    }
}
