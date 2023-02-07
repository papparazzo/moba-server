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

public enum ErrorId {
    UNKNOWN_ERROR,
    UNKNOWN_GROUP_ID,
    UNKNOWN_MESSAGE_ID,

    DATABASE_ERROR,

    DATASET_NOT_LOCKED,
    DATASET_LOCKED,
    DATASET_MISSING,

    FAULTY_MESSAGE,

    INVALID_APP_ID,
    INVALID_DATA_SEND,
    INVALID_STATUS_CHANGE,

    NO_DEFAULT_GIVEN,

    SAME_ORIGIN_NEEDED,

    SETTING_NOT_ALLOWED,
    INVALID_VALUE_GIVEN;
}
