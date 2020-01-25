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

public class DayTimeTest {
    @Test
    public void testConstructor() {
        DayTime instance = new DayTime();
        assertEquals(0, instance.getValue());
        assertEquals("So 00:00", instance.toString());
        assertEquals(true, instance.isFullHour());

        instance = new DayTime(22);
        assertEquals(22, instance.getValue());
        assertEquals("So 00:00", instance.toString());
        assertEquals(true, instance.isFullHour());

        instance = new DayTime(2222);
        assertEquals(2222, instance.getValue());
        assertEquals("So 00:37", instance.toString());
        assertEquals(false, instance.isFullHour());

        instance = new DayTime(36300);
        assertEquals(36300, instance.getValue());
        assertEquals("So 10:05", instance.toString());
        assertEquals(false, instance.isFullHour());

        instance = new DayTime(86400);
        assertEquals(86400, instance.getValue());
        assertEquals("Mo 00:00", instance.toString());
        assertEquals(true, instance.isFullHour());

        instance = new DayTime(86340);
        assertEquals(86340, instance.getValue());
        assertEquals("So 23:59", instance.toString());
        assertEquals(false, instance.isFullHour());

        instance = new DayTime(87000);
        assertEquals(87000, instance.getValue());
        assertEquals("Mo 00:10", instance.toString());
        assertEquals(false, instance.isFullHour());


        //instance = new DayTime("Sa", 10, 22);
        //instance = new DayTime("So 10:00");
    }


    @Test
    public void testSetValue_long() {
        int val = 0;
        DayTime instance = new DayTime();
        instance.setValue(val);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetValue_String() {
        String val = "";
        DayTime instance = new DayTime();
        instance.setValue(val);
    }

    @Test
    public void testGetValOfDay() {
        String val = "";
        DayTime instance = new DayTime();
//        long expResult = 0L;
//        long result = instance.getValOfDay(val);
      //  assertEquals(expResult, result);
    }

    @Test
    public void testGetValue() {
        DayTime instance = new DayTime();
        long expResult = 0L;
        long result = instance.getValue();
        assertEquals(expResult, result);
    }

    @Test
    public void testToString() {
        DayTime instance = new DayTime();
        String expResult = "So 00:00";
        String result = instance.toString();
        assertEquals(expResult, result);
    }

    @Test
    public void testToJsonString()
    throws Exception {
        boolean formated = false;
        int indent = 0;
        DayTime instance = new DayTime();
        String expResult = "\"So 00:00\"";
        String result = instance.toJsonString(formated, indent);
        assertEquals(expResult, result);
    }
}
