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

package moba.server.datatypes.collections;

import moba.server.datatypes.enumerations.ActionType;
import moba.server.datatypes.objects.ActionData;

import java.util.ArrayList;

public class ActionDataList extends ArrayList<ActionData<?>> {

    public ActionDataList addAction(ActionType actionType) {
        this.add(new ActionData<>(actionType));
        return this;
    }

    public <T> ActionDataList addAction(ActionType actionType, T data) {
        this.add(new ActionData<>(actionType, data));
        return this;
    }
}
