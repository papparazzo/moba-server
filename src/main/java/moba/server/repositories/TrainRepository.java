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

import moba.server.apiconnector.ApiConnectorException;
import moba.server.apiconnector.TrainApiConnector;
import moba.server.datatypes.collections.TrainList;
import moba.server.datatypes.enumerations.DrivingDirection;
import moba.server.datatypes.enumerations.TrainType;
import moba.server.datatypes.objects.Train;
import moba.server.exceptions.ClientErrorException;
import moba.server.utilities.CheckedEnum;
import moba.server.utilities.database.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public final class TrainRepository {

    private final Database database;
    private final TrainApiConnector trainApi;

    public TrainRepository(Database database, TrainApiConnector trainApi) {
        this.database = database;
        this.trainApi = trainApi;
    }

    public TrainList getTrainList(long trackLayoutId)
    throws SQLException, ClientErrorException, ApiConnectorException {

        // Stell den aktuellen IST-Zustand (wo befindet sich welcher Zug) dar!
        String q =
            "SELECT " +
                "Trains.Id, " +             // Interne Id
                "Trains.TrainId, " +        // fünfstellige "Zugnummer" für die Verknüpfung mit der API (Inventory)
                "Address, " +               // Lokadresse des Zugs
                "Speed, " +                 // FIXME: Brauchen wir das? Kommt aus der API!
                "DrivingDirection " +       // FIXME: Brauchen wir das? Kommt aus der CS2!
            "FROM Trains " +
            "LEFT JOIN BlockSections " +
            "ON BlockSections.TrainId = Trains.Id " +
            "LEFT JOIN `TrackLayoutSymbols` " +
            "ON `TrackLayoutSymbols`.`Id` = `BlockSections`.`Id` " +
            "WHERE `TrackLayoutSymbols`.`TrackLayoutId` = ? ";

        try(
            Connection con = database.getConnection();
            PreparedStatement stmt = con.prepareStatement(q)
        ) {
            stmt.setLong(1, trackLayoutId);

            TrainList map = new TrainList();
            ResultSet rs = stmt.executeQuery();

            while(rs.next()) {
                int trainId = rs.getInt("Id");

                var x = trainApi.getTrain(trainId);

                map.add(
                    new Train(
                        rs.getInt("Id"),
                        rs.getInt("Address"),
                        rs.getInt("Speed"),
                        CheckedEnum.getFromString(DrivingDirection.class, rs.getString("DrivingDirection")),
                        // TODO: Get this information from the api-endpoint!
                        TrainType.FREIGHT_TRAIN,
                        true,
                        true
                    )
                );
            }
            return map;
        } catch(Throwable e) {
            throw new RuntimeException(e);
        }
    }


    public Train getTrainById(long trainId) {
        // TODO get this from Repository and API
        return new Train(
            4,
            16410, //            int address,
            100,   //            int speed,
            DrivingDirection.FORWARD,
            TrainType.FREIGHT_TRAIN,
            false,
            false
        );

        /*
int address,
    int speed,
    DrivingDirection drivingDirection,
    TrainType trainType,
    boolean hasPantograph,
    boolean noDirectionalControl
*/


    }
}
