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
import moba.server.datatypes.enumerations.ActionType;
import moba.server.datatypes.objects.ActionList;
import moba.server.datatypes.objects.ActionListCollection;
import moba.server.datatypes.objects.PointInTime;
import moba.server.messages.Message;
import moba.server.messages.messageType.InterfaceMessage;
import moba.server.repositories.FunctionTimeTableRepository;

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
    throws SQLException {

        ResultSet rs = functionRepository.getResult(time, multiplicator);

        if (!rs.next() ) {
            // no records, no actions!
            return;
        }

        ActionList actionList = new ActionList();

        do {

            // FIXME Hier raspberry aufrufen
            int localId = rs.getInt("LocalId");
            if(rs.getBoolean("SwitchOn")) {
                actionList.addAction(ActionType.SWITCHING_GREEN, localId);
            } else {
                actionList.addAction(ActionType.SWITCHING_RED, localId);
            }
        } while (rs.next());

        ActionListCollection collection = new ActionListCollection();
        collection.addActionList(actionList);

        dispatcher.sendGroup(new Message(InterfaceMessage.SET_ACTION_LIST, collection));
    }
}
