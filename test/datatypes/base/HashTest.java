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

public class HashTest {
    @Test(expected = IllegalArgumentException.class)
    public void testSetValue() {
        String val = "";
        Hash instance = new Hash();
        instance.setValue(val);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetValueToShort() {
        String expResult = "ABC";
        new Hash(expResult);
    }

    @Test
    public void testGetValue() {
        String expResult = "ABA1234567890DEFDDEABA1234567890DEFDDEABA1234567890DEFDDEABA12FF";
        Hash instance = new Hash(expResult);
        String result = instance.getValue();
        assertEquals(expResult, result);
    }

    @Test
    public void testToJsonString()
    throws Exception {
        String value = "ABA1234567890DEFDDEABA1234567890DEFDDEABA1234567890DEFDDEABA12FF";
        Hash instance = new Hash(value);
        String expResult = "\"" + value + "\"";
        String result = instance.toJsonString(false, 0);
        assertEquals(expResult, result);
    }
}
