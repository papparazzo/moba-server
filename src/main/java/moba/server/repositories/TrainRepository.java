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

import moba.server.datatypes.enumerations.DrivingDirection;
import moba.server.datatypes.enumerations.TrainType;
import moba.server.datatypes.objects.Train;

public class TrainRepository {

    public TrainRepository() {

    }

    public Train getTrainById(long trainId) {
        // TODO get this from Repository and API
        return new Train(
    16410, //            int address,
            100, //            int speed,
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
