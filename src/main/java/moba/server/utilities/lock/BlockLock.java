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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;
import moba.server.utilities.Database;
import moba.server.datatypes.enumerations.ClientError;
import moba.server.utilities.exceptions.ClientErrorException;

public final class BlockLock extends AbstractLock {

    private final Database database;

    public BlockLock(Database database) {
        this.database = database;
    }

    @Override
    public void resetAll()
    throws SQLException {
        Connection con = database.getConnection();

        try(PreparedStatement stmt = con.prepareStatement("UPDATE `BlockSections` SET `Locked` = NULL")) {
            stmt.executeUpdate();
            getLogger().log(Level.INFO, stmt.toString());
        }
    }

    @Override
    public void resetOwn(long appId)
    throws SQLException {
        Connection con = database.getConnection();

        try(PreparedStatement stmt = con.prepareStatement("UPDATE `BlockSections` SET `Locked` = NULL WHERE `Locked` = ?")) {
            stmt.setLong(1, appId);
            stmt.executeUpdate();
            getLogger().log(Level.INFO, stmt.toString());
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void tryLock(long appId, Object data)
    throws ClientErrorException, SQLException {

        ArrayList<Long> list = (ArrayList<Long>)data;

        if(isLockedByApp(appId, data)) {
            throw new ClientErrorException(ClientError.DATASET_LOCKED, "object is already locked");
        }

        Connection con = database.getConnection();
        con.setAutoCommit(false);

        String q =
            "UPDATE `BlockSections` " +
            "SET `locked` = ? " +
            "WHERE `locked` IS NULL AND `id` IN (" + getPlaceHolderString(list.size()) + ")";

        try(PreparedStatement stmt = con.prepareStatement(q)) {
            stmt.setLong(1, appId);
            int i = 1;
            for(Long v : list) {
                stmt.setLong(++i, v);
            }

            getLogger().log(Level.INFO, stmt.toString());

            if(stmt.executeUpdate() != list.size()) {
                con.rollback();
                throw new ClientErrorException(ClientError.DATASET_LOCKED, "object is already locked");
            }
            con.commit();
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void unlock(long appId, Object data)
    throws ClientErrorException, SQLException {
        ArrayList<Long> list = (ArrayList<Long>)data;

        if(!isLockedByApp(appId, data)) {
            return;
        }

        Connection con = database.getConnection();
        con.setAutoCommit(false);

        String q =
            "UPDATE `BlockSections` " +
            "SET `locked` = NULL " +
            "WHERE `locked` = ? AND `id` IN (" + getPlaceHolderString(list.size()) + ")";

        try(PreparedStatement stmt = con.prepareStatement(q)) {
            stmt.setLong(1, appId);
            int i = 1;
            for(Long v : list) {
                stmt.setLong(++i, v);
            }

            getLogger().log(Level.INFO, stmt.toString());

            if(stmt.executeUpdate() != list.size()) {
                con.rollback();
                throw new ClientErrorException(ClientError.DATASET_MISSING, "no blocks found");
            }
            con.commit();
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean isLockedByApp(long appId, Object data)
    throws ClientErrorException, SQLException {
        ArrayList<Long> list = (ArrayList<Long>)data;

        Connection con = database.getConnection();

        String q =
            "SELECT `locked`, COUNT(*) AS `cnt` " +
            "FROM `BlockSections` " +
            "WHERE `Id` IN (" + getPlaceHolderString(list.size()) + ") GROUP BY `locked`";

        try(PreparedStatement stmt = con.prepareStatement(q)) {
            int i = 0;
            for(Long v : list) {
                stmt.setLong(++i, v);
            }

            getLogger().log(Level.INFO, stmt.toString());

            ResultSet rs = stmt.executeQuery();

            if(!rs.next()) {
                throw new ClientErrorException(ClientError.DATASET_MISSING, "no record set found");
            }
            long lockedBy = rs.getLong("locked");
            long cnt = rs.getLong("cnt");

            // more than 1 found: parts are locked, and parts are unlocked
            if(rs.next()) {
                throw new ClientErrorException(ClientError.DATASET_LOCKED, "object is locked");
            }
            if(cnt != list.size()) {
                throw new ClientErrorException(ClientError.DATASET_MISSING, "no record set found");
            }
            if(lockedBy == 0) {
                return false;
            }
            if(lockedBy == appId) {
                return true;
            }
            throw new ClientErrorException(ClientError.DATASET_LOCKED, "object is locked");
        }
    }

    private String getPlaceHolderString(int size) {
        if(size < 1) {
            throw new IllegalArgumentException("size < 1");
        }

        StringBuilder builder = new StringBuilder();

        builder.append("?,".repeat(size));
        return builder.deleteCharAt(builder.length() - 1).toString();
    }
}
