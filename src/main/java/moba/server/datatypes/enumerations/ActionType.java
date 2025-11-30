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

package moba.server.datatypes.enumerations;

public enum ActionType {
    DELAY,

    LOCO_HALT,
    LOCO_SPEED,
    LOCO_DIRECTION_BACKWARD,
    LOCO_DIRECTION_FORWARD,

    LOCO_FUNCTION_ON,
    LOCO_FUNCTION_OFF,
    LOCO_FUNCTION_TRIGGER,
    SWITCHING_RED,              // Weiche rund / Signal rot schalten
    SWITCHING_GREEN,            // Weiche gerade / Signal grün schalten

 // FIXME Werden diese Dinge auch durch Kontakte ausgelöst? Ja! Knopfdruckaktionen!

 //   FUNCTION_ON,
 //   FUNCTION_OFF,
 //   FUNCTION_TRIGGER,

    // SEND_PUSH_TRAIN,
    SEND_ROUTE_SWITCHED,
    SEND_ROUTE_RELEASED,
    SEND_BLOCK_RELEASED
};