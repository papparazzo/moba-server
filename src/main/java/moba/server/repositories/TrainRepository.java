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
import moba.server.datatypes.enumerations.DrivingDirection;
import moba.server.datatypes.enumerations.TrainType;
import moba.server.datatypes.objects.Speed;
import moba.server.datatypes.objects.Train;
import moba.server.exceptions.ClientErrorException;
import moba.server.utilities.CheckedEnum;
import moba.server.utilities.database.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.NoSuchElementException;

public final class TrainRepository {

    private final Database database;
    private final TrainApiConnector trainApi;

    public TrainRepository(Database database, TrainApiConnector trainApi) {
        this.database = database;
        this.trainApi = trainApi;
    }

    public Train getTrainById(int trainId)
    throws SQLException, ClientErrorException, ApiConnectorException {
        String q = "SELECT TrainId, Address, DrivingDirection FROM Trains WHERE `Trains`.`id` = ? ";

        try(
            Connection con = database.getConnection();
            PreparedStatement stmt = con.prepareStatement(q)
        ) {
            stmt.setLong(1, trainId);

            ResultSet rs = stmt.executeQuery();

            if(!rs.next()) {
                throw new NoSuchElementException("No Train found for id <" + trainId + ">");
            }

            var x = trainApi.getTrain(trainId);

            return
                new Train(
                    rs.getInt("Id"),
                    rs.getInt("Address"),
                    new Speed(70),
                    CheckedEnum.getFromString(DrivingDirection.class, rs.getString("DrivingDirection")),
                    TrainType.FREIGHT_TRAIN,
                    true,
                    true
                );
        }

        /*

        return new Train(
            trainId,
            16410, //            int address,
            new Speed(100),   //            int speed,
            DrivingDirection.FORWARD,
            TrainType.FREIGHT_TRAIN,
            false,
            false
        );


int address,
    int speed,
    DrivingDirection drivingDirection,
    TrainType trainType,
    boolean hasPantograph,
    boolean noDirectionalControl
*/


    }
}
