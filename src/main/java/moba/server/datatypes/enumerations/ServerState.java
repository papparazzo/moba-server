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
    HALT,                             // Alle Lokomotiven anhalten, Initialisierungszustand
    ERROR,                            // Keine Verbindung zur Hardware

    MANUAL_MODE,                      // Manueller Betrieb
    AUTOMATIC_MODE,                   // Anlage im Automatikbetrieb

    READY_FOR_AUTOMATIC_MODE,

    STANDBY_IN_MANUAL_MODE,                 // Energiesparmodus (manueller Modus)
    STANDBY_IN_AUTOMATIC_MODE,              // Energiesparmodus (automatischer Modus)

    EMERGENCY_STOP_IN_MANUAL_MODE,          // Nothalt (manueller Modus)
    EMERGENCY_STOP_IN_AUTOMATIC_MODE,       // Nothalt (automatischer Modus)
    EMERGENCY_STOP_IN_AUTOMATIC_HALT,
    EMERGENCY_STOP_IN_AUTOMATIC_HALT_FOR_SHUTDOWN,

    AUTOMATIC_HALT,                            // Anlage anhalten
    AUTOMATIC_HALT_FOR_SHUTDOWN,
    READY_FOR_SHUTDOWN;

    public SystemState toSystemState() {
        return switch(this) {
            case
                HALT,
                ERROR,
                EMERGENCY_STOP_IN_MANUAL_MODE,
                EMERGENCY_STOP_IN_AUTOMATIC_MODE,
                EMERGENCY_STOP_IN_AUTOMATIC_HALT,
                EMERGENCY_STOP_IN_AUTOMATIC_HALT_FOR_SHUTDOWN
                    -> SystemState.INCIDENT;

            case
                MANUAL_MODE,
                AUTOMATIC_HALT_FOR_SHUTDOWN
                    -> SystemState.MANUAL;

            case
                READY_FOR_AUTOMATIC_MODE
                    -> SystemState.READY;
            case
                AUTOMATIC_MODE,
                AUTOMATIC_HALT
                    -> SystemState.AUTOMATIC;

            case
                STANDBY_IN_MANUAL_MODE,
                STANDBY_IN_AUTOMATIC_MODE
                    -> SystemState.STANDBY;

            case
                READY_FOR_SHUTDOWN
                    -> SystemState.SHUTDOWN;
        };
    }
}
