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
import moba.server.datatypes.collections.FunctionStateDataList;
import moba.server.datatypes.enumerations.Day;
import moba.server.datatypes.enumerations.FunctionState;
import moba.server.datatypes.objects.FunctionStateData;
import moba.server.datatypes.objects.GlobalPortAddressData;
import moba.server.datatypes.objects.PointInTime;
import moba.server.datatypes.objects.PortAddressData;
import moba.server.exceptions.ClientErrorException;
import moba.server.utilities.CheckedEnum;
import moba.server.utilities.database.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

final public class FunctionTimeTableRepository {
    private final Database database;

    public FunctionTimeTableRepository(Database database) {
        this.database = database;
    }

    public FunctionStateDataList getResult(PointInTime time, int multiplicator)
    throws SQLException, ClientErrorException {
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
                FunctionStateDataList list = new FunctionStateDataList();
                if(!rs.next()) {
                    return list;
                }
                do {
                    FunctionStateData data = new FunctionStateData(
                        new GlobalPortAddressData(
                            rs.getLong("DeviceId"),
                            new PortAddressData(
                                rs.getLong("Controller"),
                                rs.getLong("Port")
                            )
                        ),
                        CheckedEnum.getFromString(FunctionState.class, rs.getString("Action"))
                    );
                    list.add(data);
                } while(rs.next());
                return list;
            }
        }
    }

    private static String getQuery(boolean daySwitch) {
        if(daySwitch) {
            return /* language=SQL */
                "SELECT DeviceId, Controller, Port, Action " +
                "FROM FunctionCycleTimes " +
                "LEFT JOIN FunctionAddresses " +
                "ON FunctionCycleTimes.FunctionAddressId = FunctionAddresses.Id " +
                "WHERE ((Weekdays = ? AND Time >= ?) OR (Weekdays = ? AND Time < ?)) " +
                "AND ((AtRandom = 1 AND (FLOOR(RAND() * 10) % 2)) OR AtRandom = 0)";
        } else {
            return /* language=SQL */
                "SELECT DeviceId, Controller, Port, Action " +
                "FROM FunctionCycleTimes " +
                "LEFT JOIN FunctionAddresses " +
                "ON FunctionCycleTimes.FunctionAddressId = FunctionAddresses.Id " +
                "WHERE Weekdays = ? AND Time >= ? AND Time < ? " +
                "AND ((AtRandom = 1 AND (FLOOR(RAND() * 10) % 2)) OR AtRandom = 0)";
        }
    }
}
