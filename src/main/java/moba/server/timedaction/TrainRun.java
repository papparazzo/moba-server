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

package moba.server.timedaction;

import moba.server.actionhandler.TrainRunner;
import moba.server.datatypes.enumerations.ActionType;
import moba.server.datatypes.objects.PointInTime;
import moba.server.datatypes.objects.TrainDestination;
import moba.server.repositories.TrainTimeTableRepository;
import moba.server.repositories.TrainlistRepository;

import java.sql.ResultSet;
import java.sql.SQLException;

final public class TrainRun implements TimedActionInterface {

    private final TrainTimeTableRepository trainTimeTableRepository;
    private final TrainRunner trainRunner;

    private final TrainlistRepository trainlistRepository;

    public TrainRun(TrainTimeTableRepository trainTimeTableRepository, TrainRunner trainRunner, TrainlistRepository trainlistRepository) {
        this.trainTimeTableRepository = trainTimeTableRepository;
        this.trainRunner = trainRunner;
        this.trainlistRepository = trainlistRepository;
    }

    @Override
    public void trigger(PointInTime time, int multiplicator)
    throws SQLException {
        ResultSet rs = trainTimeTableRepository.getResult(time, multiplicator);

        if (!rs.next() ) {
            // no records, no actions!
            return;
        }

        do {
//trainlistRepository.getTrainList();

//Train train = new Train();
/*
int address,
    int speed,
    DrivingDirection drivingDirection,
    TrainType trainType,
    boolean hasPantograph,
    boolean noDirectionalControl
*/

            int localId = rs.getInt("TrainId");

            //   "SELECT Id, TrainId, ToBlockId " +

// Train train, long departureBlockId, long destinationBlockId

            TrainDestination destination = new TrainDestination();

            if(rs.getBoolean("SwitchOn")) {
                actionList.addAction(ActionType.SWITCHING_GREEN, localId);
            } else {
                actionList.addAction(ActionType.SWITCHING_RED, localId);
            }
        } while (rs.next());

        trainRunner.pushTrain();


    }

}
