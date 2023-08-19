/*
 *  Project:    moba-server
 *
 *  Copyright (C) 2016 stefan
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

package moba.server.datatypes.base;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

public class TimeTest {
    @Test
    public void testSetValue_int() {
        int val = 0;
        Time instance = new Time();
        instance.setTime(val);
    }


    @Test
    public void testGetValue() {
        Time instance = new Time();
        int expResult = 0;
        int result = instance.getTime();
        assertEquals(expResult, result);
    }
}
