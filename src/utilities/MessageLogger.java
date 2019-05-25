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

import datatypes.objects.NoticeData;
import datatypes.objects.ErrorData;
import messages.Message;

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

    public static final String ANSI_BLACK_BACKGROUND = "\u001B[40m";
    public static final String ANSI_RED_BACKGROUND = "\u001B[41m";
    public static final String ANSI_GREEN_BACKGROUND = "\u001B[42m";
    public static final String ANSI_YELLOW_BACKGROUND = "\u001B[43m";
    public static final String ANSI_BLUE_BACKGROUND = "\u001B[44m";
    public static final String ANSI_PURPLE_BACKGROUND = "\u001B[45m";
    public static final String ANSI_CYAN_BACKGROUND = "\u001B[46m";
    public static final String ANSI_WHITE_BACKGROUND = "\u001B[47m";

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

        StringBuilder sb = new StringBuilder();
        SimpleDateFormat df = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss.SSSS ");
        sb.append(df.format(new Date()));
        if(in) {
            sb.append("--> ");
        } else {
            sb.append("<-- ");
        }
        sb.append(msg.getMsgType().toString());
        appendText(sb, msg);
        sb.append(" [");
        sb.append(msg.getMsgType().getMessageClass().toString());
        sb.append("] EP-");
        if(msg.getEndpoint() == null) {
            sb.append("0: NULL");
        } else {
            sb.append(msg.getEndpoint().toString());
        }
        System.out.println(sb);
    }

    protected static void appendText(StringBuilder sb, Message msg) {
        switch(msg.getMsgType()) {
            case GUI_SYSTEM_NOTICE:
                sb.append(ANSI_CYAN);
                sb.append(" >> ");
                printSystemNotice(sb, (NoticeData)msg.getData());
                sb.append(" <<");
                sb.append(ANSI_RESET);
                break;

            case ERROR:
                sb.append(ANSI_CYAN);
                sb.append(" >> ");
                sb.append(((ErrorData)msg.getData()).toString());
                sb.append(" <<");
                sb.append(ANSI_RESET);
                break;
        }
    }

    protected static void printSystemNotice(StringBuilder sb, NoticeData noticeData) {
        sb.append("[");
        sb.append(noticeData.getType().toString());
        sb.append("] ");
        sb.append(noticeData.getCaption());
        sb.append(": ");
        sb.append(noticeData.getText());
    }
}
