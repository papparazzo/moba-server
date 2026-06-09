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

import moba.server.datatypes.base.Time;
import moba.server.datatypes.enumerations.Day;
import moba.server.datatypes.objects.PointInTime;
import moba.server.repositories.datatypes.TrainTimeTableEntry;
import moba.server.utilities.database.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

final public class TrainTimeTableRepository {
    private final Database database;

    private final Logger logger;

    public TrainTimeTableRepository(Database database, Logger logger) {
        this.database = database;
        this.logger = logger;
    }

    public List<TrainTimeTableEntry> getTrainTableEntries(PointInTime time, int multiplicator)
    throws SQLException {
        Time t1 = time.getTime();
        Time t2 = time.getTime();

        Day d1 = time.getDay();

        boolean daySwitch = t2.hasDayChange(multiplicator);

        try(
            Connection con = database.getConnection();
            PreparedStatement stmt = con.prepareStatement(getQuery(daySwitch))
        ) {
            stmt.setString(1, d1.toString());
            stmt.setString(2, t1.getTime());
            stmt.setString(3, t2.getTime(multiplicator));

            if(daySwitch) {
                stmt.setString(4, d1.next().toString());
            }

            try(ResultSet rs = stmt.executeQuery()) {
                List<TrainTimeTableEntry> entries = new ArrayList<>();

                while(rs.next()) {
                    entries.add(new TrainTimeTableEntry(
                        rs.getLong("Id"),
                        rs.getLong("TrainId"),
                        rs.getLong("FromBlockId"),
                        rs.getLong("ToBlockId")
                    ));

                    if(rs.getBoolean("Recurring")) {
                        removeTrain(rs.getLong("Id"));
                    }
                }
                return entries;
            }
        }
    }

    private static String getQuery(boolean daySwitch) {
        if(daySwitch) {
            return /* language=SQL */
                "SELECT TTT.Id, TTT.TrainId, `BS`.`Id` AS FromBlockId , ToBlockId, Recurring " +
                "FROM TrainTimeTable TTT " +
                "LEFT JOIN BlockSections BS " +
                "ON BS.TrainId = TTT.TrainId " +
                "WHERE ((Weekdays = ? AND Time >= ?) OR (Time < ? AND Weekdays = ?)) ";
        } else {
            return /* language=SQL */
                "SELECT TTT.id, TTT.TrainId, `BS`.`Id` AS FromBlockId , ToBlockId, Recurring " +
                "FROM TrainTimeTable TTT " +
                "LEFT JOIN BlockSections BS " +
                "ON BS.TrainId = TTT.TrainId " +
                "WHERE Weekdays = ? AND Time >= ? AND Time < ? ";
        }
    }

    private void removeTrain(long trainTableId)
    throws SQLException {
        String q = "DELETE FROM TrainTimeTable WHERE TrainId = ? AND Recurring = 1";
        try(
            Connection con = database.getConnection();
            PreparedStatement stmt = con.prepareStatement(q)
        ) {
            stmt.setLong(1, trainTableId);
            stmt.execute();
        }
        logger.log(Level.WARNING, "train <{0}> already deleted from train-table!", new Object[]{trainTableId});
    }

    public void addTrain(long trainId, long toBlock, Day day, Time time, boolean recurring)
    throws SQLException {
        String q =
            "INSERT INTO TrainTimeTable (TrainId, ToBlockId, Weekdays, Time, Recurring) " +
            "VALUES (?, ?, ?, ?, ?)";

        try(
            Connection con = database.getConnection();
            PreparedStatement stmt = con.prepareStatement(q)
        ) {
            stmt.setLong(1, trainId);
            stmt.setLong(2, toBlock);
            stmt.setString(3, day.toString());
            stmt.setString(4, time.toString());
            stmt.setBoolean(5, recurring);
            stmt.execute();
        }
    }
}
