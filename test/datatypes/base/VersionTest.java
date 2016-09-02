/*
 *  moba-appServer
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

public class VersionTest {
    @Test
    public void testCompareMajor() {
        Version v = null;
        Version instance = new Version();
        int expResult = 0;
        int result = instance.compareMajor(v);
        assertEquals(expResult, result);
        fail("The test case is a prototype.");
    }

    @Test
    public void testCompareMinor() {
        Version v = null;
        Version instance = new Version();
        int expResult = 0;
        int result = instance.compareMinor(v);
        assertEquals(expResult, result);
        fail("The test case is a prototype.");
    }

    @Test
    public void testCompareTo() {
        Object o = null;
        Version instance = new Version();
        int expResult = 0;
        int result = instance.compareTo(o);
        assertEquals(expResult, result);
        fail("The test case is a prototype.");
    }

    @Test
    public void testToString() {
        Version instance = new Version();
        String expResult = "";
        String result = instance.toString();
        assertEquals(expResult, result);
        fail("The test case is a prototype.");
    }

    @Test
    public void testToJsonString() {
        boolean formated = false;
        int indent = 0;
        Version instance = new Version();
        String expResult = "";
        String result = instance.toJsonString(formated, indent);
        assertEquals(expResult, result);
        fail("The test case is a prototype.");
    }
}
