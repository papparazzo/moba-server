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
 *  along with this program. If not, see <https://www.gnu.org/licenses/agpl.txt>.
 *
 */

package moba.server.datatypes.base;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

public class VersionTest {
    @Test
    public void testCompareMajor() {
        Version v = new Version();
        Version instance = new Version();
        int expResult = 0;
        int result = instance.compareMajor(v);
        assertEquals(expResult, result);
    }

    @Test
    public void testCompareMinor() {
        Version v = new Version();
        Version instance = new Version();
        int expResult = 0;
        int result = instance.compareMinor(v);
        assertEquals(expResult, result);
    }

    @Test
    public void testCompareTo() {
        Object o = new Version();
        Version instance = new Version();
        int expResult = 0;
        int result = instance.compareTo(o);
        assertEquals(expResult, result);
    }

    @Test
    public void testToString() {
        Version instance = new Version();
        String expResult = "0.0.0-0000";
        String result = instance.toString();
        assertEquals(expResult, result);
    }

    @Test
    public void testToJsonString() {
        boolean formatted = false;
        int indent = 0;
        Version instance = new Version();
        String expResult = "\"0.0.0.0\"";
        String result = instance.toJsonString(formatted, indent);
        assertEquals(expResult, result);
    }
}
