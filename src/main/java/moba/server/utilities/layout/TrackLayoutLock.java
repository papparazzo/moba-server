/*
 *  Project:    moba-server
 *
 *  Copyright (C) 2018 Stefan Paproth <pappi-@gmx.de>
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

package moba.server.utilities.layout;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import moba.server.utilities.database.Database;
import moba.server.datatypes.enumerations.ClientError;
import moba.server.exceptions.ClientErrorException;

public final class TrackLayoutLock {

    private final Database database;
    private final Logger logger;

    public TrackLayoutLock(Database database, Logger logger) {
        this.database = database;
        this.logger = logger;
    }

    public void resetAll()
    throws SQLException {
        // noinspection SqlWithoutWhere
        try(
            Connection con = database.getConnection();
            PreparedStatement stmt = con.prepareStatement("UPDATE `TrackLayouts` SET `Locked` = NULL")
        ) {
            stmt.executeUpdate();
            logger.log(Level.INFO, stmt.toString());
        }
    }

    public void resetOwn(long appId)
    throws SQLException {
        String q = "UPDATE `TrackLayouts` SET `Locked` = NULL WHERE `Locked` = ?";
        try(
            Connection con = database.getConnection();
            PreparedStatement stmt = con.prepareStatement(q)
        ) {
            stmt.setLong(1, appId);
            stmt.executeUpdate();
            logger.log(Level.INFO, stmt.toString());
        }
    }

    public void tryLock(long appId, long id)
    throws ClientErrorException, SQLException {
        if(isLockedByApp(appId, id)) {
            return;
        }

        String q = "UPDATE `TrackLayouts` SET `locked` = ? WHERE `locked` IS NULL AND `id` = ? ";

        try(
            Connection con = database.getConnection();
            PreparedStatement stmt = con.prepareStatement(q)
        ) {
            stmt.setLong(1, appId);
            stmt.setLong(2, id);

            logger.log(Level.INFO, stmt.toString());

            if(stmt.executeUpdate() == 0) {
                throw new ClientErrorException(ClientError.DATASET_LOCKED, "object is already locked");
            }
        }
    }

    public void unlock(long appId, long id)
    throws ClientErrorException, SQLException {
        if(!isLockedByApp(appId, id)) {
            return;
        }

        String q = "UPDATE `TrackLayouts` SET `locked` = NULL WHERE `locked` = ? AND `id` = ? ";

        try(
            Connection con = database.getConnection();
            PreparedStatement stmt = con.prepareStatement(q)
        ) {
            stmt.setLong(1, appId);
            stmt.setLong(2, id);

            logger.log(Level.INFO, stmt.toString());

            if(stmt.executeUpdate() == 0) {
                throw new ClientErrorException(ClientError.DATASET_MISSING, "no layout found with id <" + id + ">");
            }
        }
    }

    public boolean isLockedByApp(long appId, long id)
    throws ClientErrorException, SQLException {
        var lockedBy = getIdOfLockingApp(id);

        logger.log(Level.INFO, "object is locked by <{1}>", new Object[]{lockedBy});

        if(lockedBy == null) {
            return false;
        }
        if(lockedBy == appId) {
            return true;
        }
        throw new ClientErrorException(ClientError.DATASET_LOCKED, "object is already locked by <" + lockedBy + ">");
    }

    private Long getIdOfLockingApp(long id)
    throws ClientErrorException, SQLException {

        try(
            Connection con = database.getConnection();
            PreparedStatement stmt = con.prepareStatement("SELECT `locked` FROM `TrackLayouts` WHERE `Id` = ?")
        ) {
            stmt.setLong(1, id);
            logger.log(Level.INFO, stmt.toString());
            ResultSet rs = stmt.executeQuery();
            if(!rs.next()) {
                throw new ClientErrorException(
                    ClientError.DATASET_MISSING,
                    "No layout found with id <" + id + "> for determining lock-state"
                );
            }
            var val = rs.getLong("locked");
            return rs.wasNull() ? null : val;
        }
    }
}
