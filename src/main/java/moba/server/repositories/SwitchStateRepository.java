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

import moba.server.datatypes.enumerations.SwitchStand;
import moba.server.datatypes.collections.SwitchStateMap;
import moba.server.datatypes.objects.SwitchStandData;
import moba.server.utilities.CheckedEnum;
import moba.server.utilities.Database;
import moba.server.exceptions.ClientErrorException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

// TODO: Anstelle von `SwitchDrives`.`Address` hier eine Referenz auf FunctionAddresses!!
public final class SwitchStateRepository {
    private final Database database;

    public SwitchStateRepository(Database database) {
        this.database = database;
    }

    public SwitchStateMap getSwitchStateListForTracklayout(long id)
    throws SQLException, ClientErrorException {
        String q =
             /* language=SQL */
            "SELECT `SwitchDrives`.`Id`, `SwitchDrives`.`SwitchStand`, `SwitchDrives`.`Address` " +
            "FROM SwitchDrives " +
            "LEFT JOIN `TrackLayoutSymbols` " +
            "ON `TrackLayoutSymbols`.`Id` = `SwitchDrives`.`Id` " +
            "WHERE `TrackLayoutSymbols`.`TrackLayoutId` = ? ";
        return getSwitchStateListForRoute(q, id);
    }

    public SwitchStateMap getSwitchStateListForRoute(long routeId)
    throws SQLException, ClientErrorException {
        String q =
            /* language=SQL */
            "SELECT `SwitchDrives`.`Id`, `SwitchDrives`.`SwitchStand`, `SwitchDrives`.`Address` " +
            "FROM SwitchDrives " +
            "LEFT JOIN `TrackLayoutSymbols` " +
            "ON `TrackLayoutSymbols`.`Id` = `SwitchDrives`.`Id` " +
            "WHERE `TrackLayoutSymbols`.`TrackLayoutId` = ? ";

        return getSwitchStateListForRoute(q, routeId);
    }

    private SwitchStateMap getSwitchStateListForRoute(String stmt, long id)
    throws SQLException, ClientErrorException {
        Connection con = database.getConnection();

        try (PreparedStatement pstmt = con.prepareStatement(stmt)) {
            pstmt.setLong(1, id);

            SwitchStateMap map = new SwitchStateMap();

            ResultSet rs = pstmt.executeQuery();
            while(rs.next()) {
                map.put(
                    rs.getLong("Id"),
                    new SwitchStandData(
                        rs.getLong("Address"),
                        CheckedEnum.getFromString(SwitchStand.class, rs.getString("SwitchStand"))
                    )
                );
            }
            return map;
        }
    }

   // public void saveSwitchStateList(long id, SwitchStateMap container)
}
