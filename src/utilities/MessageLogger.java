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

package utilities;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import datatypes.objects.NoticeData;
import messages.Message;
import messages.MessageType;

public class MessageLogger {
    protected static final String ANSI_RESET  = "\u001B[0m";
    protected static final String ANSI_BLACK  = "\u001B[30m";
    protected static final String ANSI_RED    = "\u001B[31m";
    protected static final String ANSI_GREEN  = "\u001B[32m";
    protected static final String ANSI_YELLOW = "\u001B[33m";
    protected static final String ANSI_BLUE   = "\u001B[34m";
    protected static final String ANSI_PURPLE = "\u001B[35m";
    protected static final String ANSI_CYAN   = "\u001B[36m";
    protected static final String ANSI_WHITE  = "\u001B[37m";

    public static void in(Message msg) {
        MessageLogger.print(msg, true);
    }

    public static void out(Message msg) {
        MessageLogger.print(msg, false);
    }

    protected static void print(Message msg, boolean in) {
        if(msg == null) {
            return;
        }

        if(msg.getMsgType() == MessageType.GUI_SYSTEM_NOTICE) {
            MessageLogger.printSystemNotice(msg);
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append(ANSI_BLUE);
        SimpleDateFormat df = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss.SSSS ");
        sb.append(df.format(new Date()));
        if(in) {
            sb.append("--> ");
        } else {
            sb.append("<-- ");
        }
        sb.append(msg.getMsgType().getMessageGroup().toString());
        sb.append(":");
        sb.append(msg.getMsgType().toString());
        sb.append(" [");
        sb.append(msg.getMsgType().getMessageClass().toString());
        sb.append("] EP-");
        if(msg.getEndpoint() == null) {
            sb.append("0: NULL");
        } else {
            sb.append(msg.getEndpoint().toString());
        }
        sb.append(MessageLogger.ANSI_RESET);
        System.out.println(sb);
    }

    protected static void printSystemNotice(Message msg) {
        Object o = msg.getData();
        Map<String, Object> map;

        if(o instanceof NoticeData) {
            NoticeData n = (NoticeData)o;
            map = new HashMap<>();
            map.put("type", n.getType().toString());
            map.put("caption", n.getCaption());
            map.put("text", n.getText());
        } else {
            map = (Map<String, Object>)o;
        }

        StringBuilder sb = new StringBuilder();
        SimpleDateFormat df = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss.SSSS ");

        switch((String)map.get("type")) {
            case "INFO":
                sb.append(ANSI_GREEN);
                break;

            case "WARNING":
                sb.append(ANSI_YELLOW);
                break;

            case "ERROR":
                sb.append(ANSI_RED);
                break;

            default:
        }
        sb.append(df.format(new Date()));
        sb.append("<-> ");
        sb.append((String)map.get("type"));
        sb.append(": [");
        sb.append((String)map.get("caption"));
        sb.append("] ");
        sb.append((String)map.get("text"));
        sb.append(MessageLogger.ANSI_RESET);
        System.out.println(sb);
    }
}
