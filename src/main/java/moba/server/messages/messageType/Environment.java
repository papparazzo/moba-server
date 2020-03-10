/*
 *  Project:    moba-server
 *
 *  Copyright (C) 2020 Stefan Paproth <pappi-@gmx.de>
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

package moba.server.messages.messageType;


public class Environment {
    protected static final int GROUP_ID = 5;

    protected static final int GET_ENVIRONMENT   = 1;
    protected static final int SET_ENVIRONMENT   = 2;
    protected static final int GET_AMBIENCE      = 3;
    protected static final int SET_AMBIENCE      = 4;
    protected static final int GET_AMBIENT_LIGHT = 5;
    protected static final int SET_AMBIENT_LIGHT = 6;

}
