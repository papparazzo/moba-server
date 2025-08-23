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

import java.util.ArrayList;
import java.util.HashMap;

public class ActionList {

    private final ArrayList<HashMap<String, Object>> actions = new ArrayList<>();

    public ActionList() {}

    public ActionList(ActionType actionType) {
        addAction(actionType);
    }

    public <T> ActionList(ActionType actionType, T data) {
        addAction(actionType, data);
    }

    public ActionList addAction(ActionType actionType) {
        HashMap<String, Object> action = new HashMap<>();
        action.put("action", actionType);
        actions.add(action);
        return this;
    }

    public <T> ActionList addAction(ActionType actionType, T data) {
        HashMap<String, Object> action = new HashMap<>();
        action.put("action", actionType);
        action.put("data", data);
        actions.add(action);
        return this;
    }

    public ActionList addActionOnCondition(ActionType actionType, boolean condition) {
        if(!condition) {
            return this;
        }
        return addAction(actionType);
    }

    public ArrayList<HashMap<String, Object>> getActions() {
        return actions;
    }
}
