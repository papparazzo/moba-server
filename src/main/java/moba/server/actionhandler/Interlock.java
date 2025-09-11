/*
 *  Project:    moba-server
 *
 *  Copyright (C) 2025 Stefan Paproth <pappi-@gmx.de>
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

package moba.server.actionhandler;

import moba.server.datatypes.enumerations.ClientError;
import moba.server.utilities.Database;
import moba.server.exceptions.ClientErrorException;
import moba.server.utilities.logger.Loggable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Vector;
import java.util.logging.Level;

final public class Interlock implements Loggable {

    private final Database database;

    private final HashMap<Long, Boolean> routeStatusList = new HashMap<>();

    public Interlock(Database database) {
        this.database = database;
    }

    public boolean setBlock(long trainId, long blockId) throws SQLException {
        Connection con = database.getConnection();

        String q =
            "UPDATE `BlockSections` " +
            "SET `TrainId` = ? " +
            "WHERE (`TrainId` IS NULL OR `TrainId` = ?) AND `id` = ?";

        try(PreparedStatement stmt = con.prepareStatement(q)) {
            stmt.setLong(1, trainId);
            stmt.setLong(2, trainId);
            stmt.setLong(3, blockId);

            return stmt.executeUpdate() == 1;
        }
    }

    public void releaseBlock(long trainId, long blockId)
    throws SQLException, ClientErrorException {
        Connection con = database.getConnection();

        String q = "UPDATE `BlockSections` SET `TrainId` = NULL WHERE `TrainId` = ? AND `id` = ?";

        try(PreparedStatement stmt = con.prepareStatement(q)) {
            stmt.setLong(1, trainId);
            stmt.setLong(2, blockId);

            if(stmt.executeUpdate() == 1) {
                throw new ClientErrorException(
                    ClientError.OPERATION_NOT_ALLOWED,
                    "block not set for train <" + trainId + ">"
                );
            }
        }
    }

    public boolean setRoute(long trainId, Vector<Long> switches)
        throws SQLException {

        Boolean routeStatus = routeStatusList.get(trainId);

        if(routeStatus != null && routeStatus) {
            // Fahrstraße gesetzt und Weichen geschaltet
            return true;
        }

        if(routeStatus != null) {
            // Fahrstraße gesetzt und Weichen noch nicht geschaltet
            return false;
        }

        Connection con = database.getConnection();
        con.setAutoCommit(false);

        String q =
            "UPDATE `SwitchDrives` " +
            "SET `TrainId` = ? " +
            "WHERE `TrainId` IS NULL AND `id` IN (" + getPlaceHolderString(switches.size()) + ")";

        try(PreparedStatement stmt = con.prepareStatement(q)) {
            stmt.setLong(1, trainId);
            int i = 1;
            for(Long v : switches) {
                stmt.setLong(++i, v);
            }

            getLogger().log(Level.INFO, stmt.toString());

            if(stmt.executeUpdate() != switches.size()) {
                con.rollback();
                // Weichen bereits anderweitig geschaltet.
                return false;
            }
            con.commit();
        }
        routeStatusList.put(trainId, false);
        return false;
    }

    public void routeSet(long trainId) {
        routeStatusList.put(trainId, false);
    }

    public void releaseRoute(long trainId)
    throws SQLException, ClientErrorException {

        Connection con = database.getConnection();

        String q = "UPDATE `SwitchDrives` SET `TrainId` = NULL WHERE `TrainId` = ? ";

        try(PreparedStatement stmt = con.prepareStatement(q)) {
            stmt.setLong(1, trainId);

            getLogger().log(Level.INFO, stmt.toString());

            stmt.executeUpdate();
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

