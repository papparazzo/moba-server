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

package moba.server.datatypes.objects.actiondata;

import moba.server.datatypes.enumerations.ActionType;
import moba.server.datatypes.enumerations.ControllableFunction;
import moba.server.datatypes.objects.ActionData;

import java.util.HashMap;

public final class LocoFunction extends ActionData {

    private final ControllableFunction controllableFunction;
    private final boolean active;

    public LocoFunction(ControllableFunction controllableFunction, boolean active) {
        super(ActionType.LOCO_FUNCTION);
        this.controllableFunction = controllableFunction;
        this.active = active;
    }

    @Override
    protected void appendData(HashMap<String, Object> action) {
        action.put("active", active);
        action.put("function", controllableFunction);
    }
}
