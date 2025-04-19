/*
 *  Project:    moba-server
 *
 *  Copyright (C) 2016 Stefan Paproth <pappi-@gmx.de>
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

package moba.server.datatypes.enumerations;

import moba.server.datatypes.enumerations.helper.CheckedEnum;
import moba.server.utilities.exceptions.SystemErrorException;

public enum ThreeState {
    ON,
    OFF,
    AUTO,
    UNSET;

    public static ThreeState getValue(ThreeState t, ThreeState def) {
        if(t == ThreeState.UNSET) {
            return def;
        }
        return t;
    }

    public static ThreeState getValue(boolean val) {
        if(val) {
            return ThreeState.ON;
        }
        return ThreeState.OFF;
    }

    public static ThreeState getValue(String s, ThreeState def)
    throws SystemErrorException {
        ThreeState t = CheckedEnum.getFromString(ThreeState.class, s);
        return getValue(t, def);
    }
}
