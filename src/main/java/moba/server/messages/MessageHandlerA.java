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

package moba.server.messages;

import java.net.InetAddress;
import moba.server.com.Dispatcher;
import moba.server.datatypes.enumerations.ErrorId;
import moba.server.datatypes.enumerations.HardwareState;
import moba.server.utilities.exceptions.ErrorException;

public abstract class MessageHandlerA {

    protected Dispatcher dispatcher = null;

    public abstract void handleMsg(Message msg)
    throws ErrorException;

    public void freeResources(long appId) {
    }

    public abstract int getGroupId();

    public void shutdown() {
    }

    public void hardwareStateChanged(HardwareState state) {
    }

    public void defaultLayoutChanged(long id) {
    }

    protected void checkForSameOrigin(InetAddress addr)
    throws ErrorException {
        if (addr.isAnyLocalAddress() || addr.isLoopbackAddress()) {
            return;
        }
        throw new ErrorException(ErrorId.SAME_ORIGIN_NEEDED, "same origin needed");
    }
}
