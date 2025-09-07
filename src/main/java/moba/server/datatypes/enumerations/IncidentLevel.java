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

public enum IncidentLevel {
    CRITICAL,  // schwerwiegende Ausnahme, moba-server wird neu gestartet
    ERROR,     // Fehlerhafte Nachrichten vom Client: Ungültige Nachrichten-Id, falscher Nachrichtenaufbau
    WARNING,   // Nothalt ausgelöst, Probleme im Automatikbetrieb
    NOTICE     // Freigabe Nothalt
}
