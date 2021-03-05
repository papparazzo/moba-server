/*
 *  Project:    moba-server
 *
 *  Copyright (C) 2021 Stefan Paproth <pappi-@gmx.de>
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

package moba.server.utilities.lock;

import moba.server.utilities.exceptions.ErrorException;
import moba.server.utilities.logger.Loggable;

public abstract class AbstractLock implements Loggable {

    protected static final int APP_SERVER_ID = 1;

    public enum LockState {
        LOCKED_BY_OWN_APP,
        LOCKED_BY_OTHER_APP,
        UNLOCKED
    }

    public abstract void resetAll();

    public abstract void resetOwn(long appId);

    public abstract void tryLock(long appId, Object data)
    throws ErrorException;

    public abstract void unlock(long appId, Object data)
    throws ErrorException;

    public abstract boolean isLockedByApp(long appId, Object data)
    throws ErrorException;
}
