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

package utilities.logger;

import datatypes.base.Version;
import java.util.logging.*;
import java.io.*;
import java.text.*;
import java.util.Date;

public class CustomFormatter extends Formatter {

    private final String  applName;
    private final Version versionStr;
    private final Date    buildDate;

    public CustomFormatter(String appl, Version ver, Date buildDate) {
        this.applName   = appl;
        this.versionStr = ver;
        this.buildDate  = buildDate;
    }

    @Override
    public synchronized String format(LogRecord record) {
        StringBuilder sb = new StringBuilder();

        SimpleDateFormat df = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss.SSSS ");
        sb.append(df.format(new Date()));
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

        String message = this.formatMessage(record);
        sb.append(record.getLevel().getLocalizedName());
        sb.append(" ");
        sb.append(message);
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
        /*
        buf.append("java.version = " + System.getProperty("java.version", "") + "\n");
        buf.append("java.vendor = " + System.getProperty("java.vendor", "") + "\n");
        buf.append("java.vendor.url = " + System.getProperty("java.vendor.url", "") + "\n");
        buf.append("java.home = " + System.getProperty("java.home", "") + "\n");
        buf.append("java.class.version = " + System.getProperty("java.class.version", "") + "\n");
        buf.append("java.class.path = " + System.getProperty("java.class.path", "") + "\n");
        buf.append("os.name = " + System.getProperty("os.name", "") + "\n");
        buf.append("os.arch = " + System.getProperty("os.arch", "") + "\n");
        buf.append("os.version = " + System.getProperty("os.version", "") + "\n");
        buf.append("user.name=" + System.getProperty("user.name", "") + "\n");
        buf.append("user.home=" + System.getProperty("user.home", "") + "\n");
        buf.append("user.dir=" + System.getProperty("user.dir", "") + "\n");
        buf.append("java.vm.specification.version=" + System.getProperty("java.vm.specification.version", "") + "\n");
        buf.append("java.vm.specification.vendor=" + System.getProperty("java.vm.specification.vendor", "") + "\n");
        buf.append("java.vm.specification.name=" + System.getProperty("java.vm.specification.name", "") + "\n");
        buf.append("java.vm.version=" + System.getProperty("java.vm.version", "") + "\n");
        buf.append("java.vm.vendor=" + System.getProperty("java.vm.vendor", "") + "\n");
        buf.append("java.vm.name=" + System.getProperty("java.vm.name", "") + "\n");
        buf.append("java.specification.version=" + System.getProperty("java.specification.version", "") + "\n");
        buf.append("java.specification.vendor=" + System.getProperty("java.specification.vendor", "") + "\n");
        buf.append("java.specification.name=" + System.getProperty("java.specification.name", "") + "\n");
        */
        buf.append("------------------------------------------------------");
        buf.append("------------------------------------------------------");
        buf.append("\n");
        buf.append("  name:     ");
        buf.append(this.applName);
        buf.append("\n");
        buf.append("  version:  ");
        buf.append(this.versionStr);
        buf.append("\n");
        buf.append("  author:   Stefan Paproth (Pappi-@gmx.de)");
        buf.append("\n");
        buf.append("  build on: ");
        if(this.buildDate == null){
            buf.append("-");
        }else{
            SimpleDateFormat df = new SimpleDateFormat("dd.MM.yyyy");
            buf.append(df.format(this.buildDate));
        }
        buf.append("\n------------------------------------------------------");
        buf.append("------------------------------------------------------\n");

        // build on: Feb  7 2008 17:54:09
        return buf.toString();
    }

    @Override
    public String getTail(Handler h){
        StringBuilder buf = new StringBuilder(500);
        buf.append("------------------------------------------------------");
        buf.append("------------------------------------------------------\n");
        return buf.toString();
    }
}
