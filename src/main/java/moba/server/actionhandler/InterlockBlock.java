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
import moba.server.utilities.database.Database;
import moba.server.exceptions.ClientErrorException;
import moba.server.utilities.logger.Loggable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

// TODO: Dies ist wohl eher ein Repository als ein ActionHandler.
final public class InterlockBlock implements Loggable {

    private final Database database;

    public InterlockBlock(Database database) {
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

            if(stmt.executeUpdate() != 1) {
                throw new ClientErrorException(
                    ClientError.OPERATION_NOT_ALLOWED,
                    "block not set for train <" + trainId + ">"
                );
            }
        }
    }
}

