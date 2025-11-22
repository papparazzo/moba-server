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

import moba.server.datatypes.collections.ActionDataList;

import java.util.ArrayList;

public class ActionDataByLocalIdCollection {

    private Integer localId = null;

    private ActionDataList actionList = null;

    private final ArrayList<ActionDataByContactCollection> triggerList = new ArrayList<>();

    public ActionDataByLocalIdCollection(int localId) {
        this.localId = localId;
    }

    public ActionDataByLocalIdCollection() {
    }

    public ActionDataByLocalIdCollection addActionList(ActionDataList list) {
        actionList = list;
        return this;
    }

    public ActionDataByLocalIdCollection addTriggerList(ActionDataByContactCollection list) {
        triggerList.add(list);
        return this;
    }

    public Integer getLocalId() {
        return localId;
    }

    public ActionDataList getActionList() {
        // FIXME: Null oder lieber Leere ActionDataList??
        return actionList;
    }

    public ArrayList<ActionDataByContactCollection> getTriggerList() {
        return triggerList;
    }
}
