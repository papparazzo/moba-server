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

package moba.server.routing;

import moba.server.com.Dispatcher;
import moba.server.datatypes.enumerations.ActionType;
import moba.server.datatypes.objects.ActionList;
import moba.server.datatypes.objects.ActionListCollection;
import moba.server.messages.Message;
import moba.server.messages.messageType.InterfaceMessage;

public class TrainRun {
    private final Dispatcher dispatcher;

    private final Router routing;

    public TrainRun(Dispatcher dispatcher, Router routing) {
        this.dispatcher = dispatcher;
        this.routing = routing;
    }

    public void fillUp() {
        var v = routing.getRoute(1, 2);
    }


    public void d() {
        ActionList actionList = new ActionList();
/*
                while(rs.next()) {
            int localId = rs.getInt("LocalId");
            if(rs.getBoolean("SwitchOn")) {
                actionList.addAction(ActionType.SWITCHING_GREEN, localId);
            } else {
                actionList.addAction(ActionType.SWITCHING_RED, localId);
            }
        }
*/
        ActionListCollection collection = new ActionListCollection();
        collection.addActionList(actionList);

        dispatcher.sendGroup(new Message(InterfaceMessage.SET_ACTION_LIST, collection));


    }



}
