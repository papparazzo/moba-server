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

package datatypes.base;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import org.junit.Test;

public class TimeTest {
    @Test
    public void testSetValue_int() {
        int val = 0;
        Time instance = new Time();
        instance.setValue(val);
        fail("The test case is a prototype.");
    }

    @Test
    public void testSetValue_String() {
        String val = "";
        Time instance = new Time();
        instance.setValue(val);
        fail("The test case is a prototype.");
    }

    @Test
    public void testGetValue() {
        Time instance = new Time();
        int expResult = 0;
        int result = instance.getValue();
        assertEquals(expResult, result);
        fail("The test case is a prototype.");
    }

    @Test
    public void testToString() {
        Time instance = new Time();
        String expResult = "";
        String result = instance.toString();
        assertEquals(expResult, result);
        fail("The test case is a prototype.");
    }

    @Test
    public void testToJsonString() throws Exception {
        boolean formated = false;
        int indent = 0;
        Time instance = new Time();
        String expResult = "";
        String result = instance.toJsonString(formated, indent);
        assertEquals(expResult, result);
        fail("The test case is a prototype.");
    }
}
