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

import moba.server.datatypes.collections.FunctionStateDataList;
import moba.server.datatypes.enumerations.FunctionState;
import moba.server.datatypes.enumerations.ServerState;
import moba.server.datatypes.objects.FunctionStateData;
import moba.server.datatypes.objects.GlobalPortAddressData;
import moba.server.datatypes.objects.PortAddressData;
import moba.server.exceptions.ClientErrorException;
import moba.server.utilities.CheckedEnum;
import moba.server.utilities.database.Database;

import java.sql.ResultSet;
import java.sql.SQLException;

final public class FunctionAddressesRepository {

    private final Database database;

    public FunctionAddressesRepository(Database database) {
        this.database = database;
    }

    public FunctionStateDataList changeState(ServerState state)
    throws SQLException, ClientErrorException {
        /*
         * Je nach SystemState unterschiedliche Actions
         *       INCIDENT:    Hauptlicht an.
         *       AUTOMATIC:   Rollos runter
         */
        FunctionStateDataList list = new FunctionStateDataList();

        String sql =
            "SELECT DeviceId, Controller, Port, Action " +
            "FROM FunctionStateChange " +
            "LEFT JOIN FunctionAddresses " +
            "ON FunctionStateChange.FunctionAddressId = FunctionAddresses.Id " +
            "WHERE OnState = ?";

        try(java.sql.PreparedStatement stmt = database.getConnection().prepareStatement(sql)) {
            stmt.setString(1, state.toSystemState().toString());

            ResultSet rs = stmt.executeQuery();

            while(rs.next()) {
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
            }
        }
        return list;
    }

    /* TODO implement...

    public FunctionStateDataList getFunctionList()
    throws SQLException, ClientErrorException {
        FunctionStateDataList list = new FunctionStateDataList();


        String sql =
            "SELECT DeviceId, Controller, Port, Action " +
            "FROM FunctionStateChange " +
            "LEFT JOIN FunctionAddresses " +
            "ON FunctionStateChange.FunctionAddressId = FunctionAddresses.Id " +
            "WHERE OnState = ?";


        sql = "SELECT Time, Weekdays, Action, AtRandom FROM FunctionCycleTimes";

        sql = "SELECT DeviceId, Controller, Port, Time, Weekdays, Action, AtRandom ";

        try(java.sql.PreparedStatement stmt = database.getConnection().prepareStatement(sql)) {

        }

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



    }

    public FunctionStateDataList storeFunctionList()
    throws SQLException, ClientErrorException {
        FunctionStateDataList list = new FunctionStateDataList();
    }
*/
}
