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

package moba.server.repositories;

import moba.server.datatypes.collections.TrainList;
import moba.server.datatypes.enumerations.DrivingDirection;
import moba.server.datatypes.objects.Train;
import moba.server.utilities.CheckedEnum;
import moba.server.utilities.Database;
import moba.server.exceptions.ClientErrorException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class TrainlistRepository {
    protected final Database database;

    public TrainlistRepository(Database database) {
        this.database = database;
    }

    public TrainList getTrainList(long id)
    throws SQLException, ClientErrorException {

        // Stell den aktuellen IST-Zustand (wo befindet sich welcher Zug) da!
        Connection con = database.getConnection();
        String q =
            "SELECT Trains.Id, `BlockSections`.`Id` AS BlockId , Address, Speed, DrivingDirection " +
            "FROM Trains " +
            "LEFT JOIN BlockSections " +
            "ON BlockSections.TrainId = Trains.Id " +
            "LEFT JOIN `TrackLayoutSymbols` " +
            "ON `TrackLayoutSymbols`.`Id` = `BlockSections`.`Id` " +
            "WHERE `TrackLayoutSymbols`.`TrackLayoutId` = ? ";

        try (PreparedStatement pstmt = con.prepareStatement(q)) {
            pstmt.setLong(1, id);

            TrainList map = new TrainList();
            ResultSet rs = pstmt.executeQuery();

            while(rs.next()) {
                map.add(
                    new Train(
                        rs.getLong("Id"),
                        rs.getInt("BlockId"),
                        rs.getInt("Address"),
                        rs.getInt("Speed"),
                        CheckedEnum.getFromString(DrivingDirection.class, rs.getString("DrivingDirection"))
                    )
                );
            }
            return map;
        }
    }
}
