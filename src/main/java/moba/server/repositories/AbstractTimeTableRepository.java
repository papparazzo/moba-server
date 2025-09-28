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

package moba.server.repositories;

import moba.server.datatypes.base.Time;
import moba.server.datatypes.enumerations.Day;
import moba.server.datatypes.objects.PointInTime;

import java.sql.ResultSet;
import java.sql.SQLException;

public abstract class AbstractTimeTableRepository {

    public ResultSet getResult(PointInTime time, int multiplicator)
    throws SQLException {
        Time t1 = time.getTime();
        Time t2 = time.getTime();

        Day d1 = time.getDay();

        if(t2.hasDayChange(multiplicator)) {
            return getResultWithDaySwitch(t1.getTime(), t2.getTime(multiplicator), d1.toString(), d1.next().toString());
        } else {
            return getResultSameDay(t1.getTime(), t2.getTime(multiplicator), d1.toString());
        }
    }

    abstract protected ResultSet getResultWithDaySwitch(String t1, String t2, String d1, String d2)
    throws SQLException;

    abstract protected ResultSet getResultSameDay(String t1, String t2, String d1)
    throws SQLException;
}
