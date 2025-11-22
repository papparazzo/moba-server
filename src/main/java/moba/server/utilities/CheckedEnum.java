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

package moba.server.utilities;

import com.google.common.base.Enums;
import moba.server.datatypes.enumerations.ClientError;
import moba.server.exceptions.ClientErrorException;

public class CheckedEnum {

    public static <T extends Enum<T>> T getFromString(Class<T> enumClass, String value)
    throws ClientErrorException {
        if(value == null) {
            throw new ClientErrorException(
                ClientError.FAULTY_MESSAGE,
                "null-value for enum <" + enumClass.getName() + "> given."
            );
        }

        var e = Enums.getIfPresent(enumClass, value).orNull();

        if(e == null) {
            throw new ClientErrorException(
                ClientError.FAULTY_MESSAGE,
                "unknown value <" + value + "> of enum <" + enumClass.getName() + ">."
            );
        }
        return e;
    }
}
