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

import moba.server.utilities.database.Database;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

final public class FunctionTimeTableRepository extends AbstractTimeTableRepository {
    private final Database database;

    public FunctionTimeTableRepository(Database database) {
        this.database = database;
    }

    protected ResultSet getResultWithDaySwitch(String t1, String t2, String d1, String d2)
    throws SQLException {

        String q =
            "SELECT DeviceId, Controller, Port, Action " +
            "FROM FunctionCycleTimes " +
            "LEFT JOIN FunctionAddresses " +
            "ON FunctionCycleTimes.FunctionAddressId = FunctionAddresses.Id " +
            "WHERE ((Weekdays = ? AND Time >= ?) OR (Weekdays = ? AND Time < ?)) " +
            "AND ((AtRandom = 1 AND (FLOOR(RAND() * 10) % 2)) OR AtRandom = 0)";

        try(PreparedStatement stmt = database.getConnection().prepareStatement(q)) {
            stmt.setString(1, d1);
            stmt.setString(2, t1);

            stmt.setString(3, d2);
            stmt.setString(4, t2);

            return stmt.executeQuery();
        }
    }

    protected ResultSet getResultSameDay(String t1, String t2, String d1)
    throws SQLException {
        String q =
            "SELECT DeviceId, Controller, Port, Action " +
            "FROM FunctionCycleTimes " +
            "LEFT JOIN FunctionAddresses " +
            "ON FunctionCycleTimes.FunctionAddressId = FunctionAddresses.Id " +
            "WHERE Weekdays = ? AND Time >= ? AND Time < ? " +
            "AND ((AtRandom = 1 AND (FLOOR(RAND() * 10) % 2)) OR AtRandom = 0)";

        try(PreparedStatement stmt = database.getConnection().prepareStatement(q)) {
            stmt.setString(1, d1);
            stmt.setString(2, t1);
            stmt.setString(3, t2);

            return stmt.executeQuery();
        }
    }
}
