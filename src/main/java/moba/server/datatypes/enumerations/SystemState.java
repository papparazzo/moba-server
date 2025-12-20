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

public enum SystemState {
    HALT,                             // Alle Lokomotiven anhalten, Initialisierungszustand

    //ERROR,                            // Keine Verbindung zur Hardware

    MANUEL_MODE,                      // Manueller Betrieb
    AUTOMATIC_MODE,                   // Anlage im Automatikbetrieb
    STANDBY_IN_MANUAL_MODE,           // Energiesparmodus (manueller Modus)
    STANDBY_IN_AUTOMATIC_MODE,        // Energiesparmodus (automatischer Modus)
    EMERGENCY_STOP_IN_MANUAL_MODE,    // Nothalt (manueller Modus)
    EMERGENCY_STOP_IN_AUTOMATIC_MODE, // Nothalt (automatischer Modus)

    AUTOMATIC_HALT,                   // Anlage anhalten





    AUTOMATIC_HALT_FOR_SHUTDOWN,
    //READY_FOR_SHUTDOWN
}
