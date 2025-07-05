/*
 *  Project:    moba-server
 *
 *  Copyright (C) 2020 Stefan Paproth <pappi-@gmx.de>
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

package moba.server.datatypes.objects;

import moba.server.datatypes.enumerations.ActionType;
import moba.server.json.JSONException;
import moba.server.json.JSONToStringI;
import moba.server.messages.Message;
import moba.server.messages.messageType.InterfaceMessage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class ActionListCollection implements JSONToStringI {

    private ArrayList<ActionList> actionslist = new ArrayList<>();

    public ActionListCollection addactionList(ActionList list) {
        actionslist.add(list);
        return this;
    }


    @Override
    public String toJsonString(boolean formatted, int indent) throws JSONException, IOException {
                HashMap<String, Object> data = new HashMap<>();
        data.put("localId",   16410);
        data.put("trigger",   null);
        data.put("actionLists", actionslist);


        var msg = new Message(InterfaceMessage.SET_ACTION_LIST, data);



        return "";
    }
}
