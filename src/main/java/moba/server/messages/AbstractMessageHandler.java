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

package moba.server.messages;

import moba.server.com.Dispatcher;
import moba.server.datatypes.enumerations.SystemState;

import java.sql.SQLException;

public abstract class AbstractMessageHandler {

    protected Dispatcher dispatcher = null;

    public abstract void handleMsg(Message msg)
    throws Exception;

    public void freeResources(long appId)
    throws SQLException {
    }

    public abstract int getGroupId();

    public void shutdown()
    throws Exception {
    }

    public void hardwareStateChanged(SystemState state) {
    }
}
