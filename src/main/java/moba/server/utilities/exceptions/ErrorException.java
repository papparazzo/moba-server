/*
 *  Project:    moba-server
 *
 *  Copyright (C) 2019 Stefan Paproth <pappi-@gmx.de>
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

package moba.server.utilities.exceptions;

import moba.server.datatypes.enumerations.ErrorId;

public class ErrorException extends Exception {

    private Throwable cause;

    private ErrorId errorId;

    public ErrorException(ErrorId errorId, String message) {
        super(message);
        this.errorId = errorId;
    }

    public ErrorException(Throwable t) {
        super(t.getMessage());
        cause = t;
    }

    public ErrorException(String message, Throwable t) {
        super(message);
        cause = t;
    }

    @Override
    public Throwable getCause() {
        return cause;
    }

    public ErrorId getErrorId() {
        return errorId;
    }
}