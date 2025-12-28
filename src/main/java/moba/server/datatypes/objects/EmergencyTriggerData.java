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

package moba.server.datatypes.objects;

import moba.server.datatypes.enumerations.EmergencyTriggerReason;
import moba.server.exceptions.ClientErrorException;
import moba.server.messages.Message;
import moba.server.utilities.CheckedEnum;

import java.util.Map;

public record EmergencyTriggerData(
    EmergencyTriggerReason reason,
    String message
) {

    @SuppressWarnings("unchecked")
    public static EmergencyTriggerData fromMessage(Message msg)
    throws ClientErrorException {
        Map<String, String> map = (Map<String, String>)msg.getData();

        return new EmergencyTriggerData(
            CheckedEnum.getFromString(EmergencyTriggerReason.class, map.get("reason")),
            map.get("message")
        );
    }
}
