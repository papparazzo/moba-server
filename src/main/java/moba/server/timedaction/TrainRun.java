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
import moba.server.datatypes.objects.PointInTime;
import moba.server.datatypes.objects.Train;
import moba.server.datatypes.objects.TrainJourney;
import moba.server.repositories.TrainRepository;
import moba.server.repositories.TrainTimeTableRepository;

import java.sql.ResultSet;
import java.sql.SQLException;

final public class TrainRun implements TimedActionInterface {

    private final TrainTimeTableRepository trainTimeTableRepository;
    private final TrainRunner trainRunner;

    private final TrainRepository trainRepository;

    public TrainRun(TrainTimeTableRepository trainTimeTableRepository, TrainRunner trainRunner, TrainRepository trainRepository) {
        this.trainTimeTableRepository = trainTimeTableRepository;
        this.trainRunner = trainRunner;
        this.trainRepository = trainRepository;
    }

    @Override
    public void trigger(PointInTime time, int multiplicator)
    throws SQLException {
        ResultSet rs = trainTimeTableRepository.getResult(time, multiplicator);

        if (!rs.next() ) {
            return;
        }

        do {
            Train train = trainRepository.getTrainById(rs.getLong("TrainId"));

            TrainJourney destination = new TrainJourney(
                train,
                rs.getLong("FromBlockId"),
                rs.getLong("ToBlockId")
            );
            trainRunner.pushTrain(destination);
            if(rs.getLong("NonRecurring") == 1) {
                trainTimeTableRepository.removeTrain(rs.getLong("Id"));
            }
        } while (rs.next());
    }
}
