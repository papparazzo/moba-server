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

package moba.server.datatypes.objects;

import moba.server.datatypes.enumerations.ActionType;
import moba.server.json.JsonSerializerInterface;

import java.util.HashMap;

final public class ActionData<T> implements JsonSerializerInterface<HashMap<String, Object>> {

    private final ActionType actionType;

    private T data = null;

    public ActionData(ActionType actionType) {
        this.actionType = actionType;
    }

    public ActionData(ActionType actionType, T data) {
        this.actionType = actionType;
        this.data = data;
    }

    @Override
    public HashMap<String, Object> toJson() {
        HashMap<String, Object> action = new HashMap<>();
        action.put("action", actionType);
        if(data != null) {
            action.put("data", data);
        }
        return action;
    }
}
