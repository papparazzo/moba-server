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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class HashTest {

    @Test
    public void testSetValue() {
        String val = "";
        Hash instance = new Hash();
        assertThrows(IllegalArgumentException.class, () -> instance.setValue(val));
    }

    @Test
    public void testGetValueToShort() {
        assertThrows(IllegalArgumentException.class, () -> new Hash("ABC"));
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
