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

public enum ServerState {
    HALT,
    INCIDENT,
    CONNECTION_LOST,

    MANUAL_MODE,
    READY_FOR_AUTOMATIC_MODE,
    AUTOMATIC_MODE,
    STANDBY,

    AUTOMATIC_HALT,
    AUTOMATIC_HALT_FOR_SHUTDOWN,
    READY_FOR_SHUTDOWN;

    public SystemState toSystemState() {
        return switch(this) {
            case
                INCIDENT
                    -> SystemState.INCIDENT;

            case
                HALT,
                CONNECTION_LOST
                    -> SystemState.NO_CONNECTION;

            case
                MANUAL_MODE
                    -> SystemState.MANUAL;

            case
                READY_FOR_AUTOMATIC_MODE
                    -> SystemState.READY;
            case
                AUTOMATIC_MODE,
                AUTOMATIC_HALT
                    -> SystemState.AUTOMATIC;

            case
                STANDBY
                    -> SystemState.STANDBY;

            case
                AUTOMATIC_HALT_FOR_SHUTDOWN,
                READY_FOR_SHUTDOWN
                    -> SystemState.SHUTDOWN;
        };
    }
}
