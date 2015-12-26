/*
 *  AppServer
 *
 *  Copyright (C) 2015 stefan
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
 *  along with this program. If not, see <http://www.gnu.org/licenses/agpl.txt>.
 *
 */
package utilities;

import java.io.IOException;
import json.JSONException;
import json.JSONToStringI;

public enum ThreeState implements JSONToStringI {
    ON,
    AUTO,
    OFF;

    protected final int value;

    private ThreeState() {
        this.value = ordinal();
    }

    @Override
    public String toJsonString(boolean formated, int indent)
    throws JSONException, IOException {
        return ThreeState.values()[this.value].toString();
    }
}
