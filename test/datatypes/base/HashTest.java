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

public class HashTest {

    @Test
    public void testSetValue() {
        String val = "";
        Hash instance = new Hash();
        instance.setValue(val);
        fail("The test case is a prototype.");
    }

    @Test
    public void testGetValue() {
        Hash instance = new Hash();
        String expResult = "";
        String result = instance.getValue();
        assertEquals(expResult, result);
        fail("The test case is a prototype.");
    }

    @Test
    public void testToJsonString() throws Exception {
        boolean formated = false;
        int indent = 0;
        Hash instance = new Hash();
        String expResult = "";
        String result = instance.toJsonString(formated, indent);
        assertEquals(expResult, result);
        fail("The test case is a prototype.");
    }
}
