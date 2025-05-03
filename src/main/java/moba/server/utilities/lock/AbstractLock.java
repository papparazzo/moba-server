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
 *  along with this program. If not, see <https://www.gnu.org/licenses/agpl.txt>.
 *
 */

package moba.server.utilities.lock;

import moba.server.utilities.exceptions.ClientErrorException;
import moba.server.utilities.logger.Loggable;

import java.sql.SQLException;

public abstract class AbstractLock implements Loggable {

    public abstract void resetAll()
    throws SQLException;

    public abstract void resetOwn(long appId)
    throws SQLException;

    public abstract void tryLock(long appId, Object data)
    throws ClientErrorException, SQLException;

    public abstract void unlock(long appId, Object data)
    throws ClientErrorException, SQLException;

    public abstract boolean isLockedByApp(long appId, Object data)
    throws ClientErrorException, SQLException;
}
