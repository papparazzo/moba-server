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
import moba.server.datatypes.objects.Position;
import moba.server.datatypes.objects.SwitchStateData;
import moba.server.routing.typedefs.SwitchStandMap;
import moba.server.utilities.CheckedEnum;
import moba.server.utilities.Database;
import moba.server.utilities.exceptions.ClientErrorException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SwitchStateList {
    protected final Database database;

    public SwitchStateList(Database database) {
        this.database = database;
    }

    public SwitchStandMap getSwitchStateList(long id)
    throws SQLException, ClientErrorException {

        Connection con = database.getConnection();

        String q =
            "SELECT `SwitchDrives`.`Id`, `SwitchDrives`.`SwitchStand`, " +
            "`TrackLayoutSymbols`.`XPos`, `TrackLayoutSymbols`.`YPos` " +
            "FROM SwitchDrives " +
            "LEFT JOIN `TrackLayoutSymbols` " +
            "ON `TrackLayoutSymbols`.`Id` = `SwitchDrives`.`Id` " +
            "WHERE `TrackLayoutSymbols`.`TrackLayoutId` = ? ";

        try (PreparedStatement pstmt = con.prepareStatement(q)) {
            pstmt.setLong(1, id);

            SwitchStandMap map = new SwitchStandMap();

            ResultSet rs = pstmt.executeQuery();
            while(rs.next()) {
                map.put(
                    new Position(
                        rs.getInt("XPos"),
                        rs.getInt("YPos")
                    ),
                    new SwitchStateData(
                        rs.getInt("Id"),
                        CheckedEnum.getFromString(SwitchStand.class, rs.getString("SwitchStand"))
                ));
            }
            return map;
        }
    }
}
