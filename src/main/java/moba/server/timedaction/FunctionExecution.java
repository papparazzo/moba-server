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

import moba.server.com.Dispatcher;
import moba.server.datatypes.collections.FunctionStateDataList;
import moba.server.datatypes.enumerations.FunctionState;
import moba.server.datatypes.objects.FunctionStateData;
import moba.server.datatypes.objects.GlobalPortAddressData;
import moba.server.datatypes.objects.PointInTime;
import moba.server.datatypes.objects.PortAddressData;
import moba.server.exceptions.ClientErrorException;
import moba.server.messages.Message;
import moba.server.messages.messagetypes.EnvironmentMessage;
import moba.server.repositories.FunctionTimeTableRepository;
import moba.server.utilities.CheckedEnum;

import java.sql.ResultSet;
import java.sql.SQLException;

final public class FunctionExecution implements TimedActionInterface {
    private final FunctionTimeTableRepository functionRepository;
    private final Dispatcher                 dispatcher;

    public FunctionExecution(FunctionTimeTableRepository functionRepository, Dispatcher dispatcher) {
        this.functionRepository = functionRepository;
        this.dispatcher = dispatcher;
    }

    @Override
    public void trigger(PointInTime time, int multiplicator)
    throws SQLException, ClientErrorException {

        FunctionStateDataList list = new FunctionStateDataList();
        ResultSet rs = functionRepository.getResult(time, multiplicator);

        if (!rs.next()) {
            return;
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
        dispatcher.sendGroup(new Message(EnvironmentMessage.SET_FUNCTIONS, list));
    }
}
