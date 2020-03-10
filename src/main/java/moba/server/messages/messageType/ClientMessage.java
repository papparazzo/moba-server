/*
 *  Project:    moba-server
 *
 *  Copyright (C) 2020 Stefan Paproth <pappi-@gmx.de>
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

package moba.server.messages.messageType;

import moba.server.messages.MessageType;

public class ClientMessage extends MessageType {
    public final static int GROUP_ID     = 1;

    public final static int VOID         = 1;
    public final static int ECHO_REQ     = 2;
    public final static int ECHO_RES     = 3;
    public final static int ERROR        = 4;
    public final static int START        = 5;
    public final static int CONNECTED    = 6;
    public final static int CLOSE        = 7;
    public final static int SHUTDOWN     = 8;
    public final static int RESET        = 9;
    public final static int SELF_TESTING = 10;

}

