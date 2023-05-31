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
 *  along with this program. If not, see <http://www.gnu.org/licenses/agpl.txt>.
 *
 */

package moba.server.datatypes.enumerations;

public enum ToggleState {
    ON,
    UNSET,
    OFF;

    public static boolean getValue(ToggleState t, boolean def) {
        switch(t) {
            case UNSET -> {
                return def;
            }

            case ON -> {
                return true;
            }

            case OFF -> {
                return false;
            }

            default -> throw new UnsupportedOperationException("Not supported yet.");
        }
    }

    public static boolean getValue(Switch s, boolean def) {
        switch(s) {
            case UNSET -> {
                return def;
            }

            case ON -> {
                return true;
            }

            case OFF -> {
                return false;
            }

            default -> throw new UnsupportedOperationException("Not supported yet.");
        }
    }

    public static boolean getValue(String s, boolean def) {
        ToggleState t = ToggleState.valueOf(s);
        switch(t) {
            case UNSET -> {
                return def;
            }

            case ON -> {
                return true;
            }

            case OFF -> {
                return false;
            }

            default -> throw new UnsupportedOperationException("Not supported yet.");
        }
    }

    public static ToggleState getValue(String s, ToggleState def) {
        ToggleState t = ToggleState.valueOf(s);
        if(t == ToggleState.UNSET) {
            return def;
        }
        return t;
    }

    public static ToggleState getValue(ToggleState t, ToggleState def) {
        if(t == ToggleState.UNSET) {
            return def;
        }
        return t;
    }

    public static ToggleState getValue(Switch s, ToggleState def) {
        switch(s) {
            case OFF -> {
                return ToggleState.OFF;
            }

            case ON -> {
                return ToggleState.ON;
            }

            case UNSET -> {
                return def;
            }

            default -> throw new UnsupportedOperationException("Not supported yet.");
        }
    }

    public static ToggleState getValue(boolean val) {
        if(val) {
            return ToggleState.ON;
        }
        return ToggleState.OFF;
    }
}
