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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import org.junit.Test;

public class PercentTest {
    @Test
    public void testSetValue() {
        int val = 0;
        Percent instance = new Percent();
        instance.setValue(val);
    }

    @Test
    public void testGetValue() {
        Percent instance = new Percent();
        int expResult = 0;
        int result = instance.getValue();
        assertEquals(expResult, result);
    }

    @Test
    public void testToJsonString()
    throws Exception {
        boolean formatted = false;
        int indent = 0;
        Percent instance = new Percent();
        String expResult = "0";
        String result = instance.toJsonString(formatted, indent);
        assertEquals(expResult, result);
    }
}
