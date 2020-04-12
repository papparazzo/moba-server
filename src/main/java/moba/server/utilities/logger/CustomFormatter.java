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

package moba.server.utilities.logger;

import moba.server.datatypes.base.Version;
import java.util.logging.*;
import java.io.*;
import java.text.*;
import java.util.Date;

public class CustomFormatter extends Formatter {

    private final String  applName;
    private final Version versionStr;
    private final Date    buildDate;

    public CustomFormatter(String appl, Version ver, Date buildDate) {
        applName   = appl;
        versionStr = ver;
        this.buildDate  = buildDate;
    }

    @Override
    public synchronized String format(LogRecord record) {
        StringBuilder sb = new StringBuilder();

        SimpleDateFormat df = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss.SSSS ");
        sb.append(df.format(new Date()));
        sb.append(record.getLevel().getLocalizedName());
        sb.append(" ");

        if(record.getSourceClassName() != null) {
            sb.append(record.getSourceClassName());
        } else {
            sb.append(record.getLoggerName());
        }

        if(record.getSourceMethodName() != null) {
            sb.append(" ");
            sb.append(record.getSourceMethodName());
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
                sb.append(sw.toString());
            }
            catch(Exception ex) {
                // noop
            }
        }
        return sb.toString();
    }

    @Override
    public String getHead(Handler h) {
        StringBuilder buf = new StringBuilder(1000);
        buf.append("------------------------------------------------------");
        buf.append("------------------------------------------------------");
        buf.append("\n");
        buf.append("  name:     ");
        buf.append(applName);
        buf.append("\n");
        buf.append("  version:  ");
        buf.append(versionStr);
        buf.append("\n");
        buf.append("  author:   Stefan Paproth (Pappi-@gmx.de)");
        buf.append("\n");
        buf.append("  build on: ");
        if(buildDate == null) {
            buf.append("-");
        } else {
            SimpleDateFormat df = new SimpleDateFormat("dd.MM.yyyy");
            buf.append(df.format(buildDate));
        }
        buf.append("\n------------------------------------------------------");
        buf.append("------------------------------------------------------\n");

        // build on: Feb  7 2008 17:54:09
        return buf.toString();
    }

    @Override
    public String getTail(Handler h) {
        StringBuilder buf = new StringBuilder(500);
        buf.append("------------------------------------------------------");
        buf.append("------------------------------------------------------\n");
        return buf.toString();
    }
}
