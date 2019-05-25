/*
 *  Project:    moba-server
 *
 *  Copyright (C) 2019 Stefan Paproth <pappi-@gmx.de>
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
package json;

import java.io.IOException;
import java.util.Map;
import json.streamreader.JSONStreamReaderString;
import json.stringreader.JSONStringReader;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author stefan
 */
public class JSONDecoderTest {

    @Test
    public void testConstructor() {

    }




    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testDecode() throws Exception {
        JSONDecoder instance = new JSONDecoder(new JSONStringReader(new JSONStreamReaderString("{}")));
        Map<String, Object> result = instance.decode();
        assertTrue(result.isEmpty());
    }

    @Test(expected = IOException.class)
    public void testDecodeOnInvalidJsonStrings() throws Exception {
        JSONDecoder instance = new JSONDecoder(new JSONStringReader(new JSONStreamReaderString("{")));
        instance.decode();
    }


    /**
     * Test of nextKey method, of class JSONDecoder.
     * /
    @Test
    public void testNextKey() throws Exception {
        System.out.println("nextKey");
        JSONDecoder instance = null;
        String expResult = "";
        String result = instance.nextKey();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of nextObject method, of class JSONDecoder.
     * /
    @Test
    public void testNextObject() throws Exception {
        System.out.println("nextObject");
        JSONDecoder instance = null;
        Map<String, Object> expResult = null;
        Map<String, Object> result = instance.nextObject();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of nextValue method, of class JSONDecoder.
     * /
    @Test
    public void testNextValue() throws Exception {
        System.out.println("nextValue");
        JSONDecoder instance = null;
        Object expResult = null;
        Object result = instance.nextValue();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of nextString method, of class JSONDecoder.
     * /
    @Test
    public void testNextString() throws Exception {
        System.out.println("nextString");
        JSONDecoder instance = null;
        String expResult = "";
        String result = instance.nextString();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of nextArray method, of class JSONDecoder.
     * /
    @Test
    public void testNextArray() throws Exception {
        System.out.println("nextArray");
        JSONDecoder instance = null;
        ArrayList expResult = null;
        ArrayList result = instance.nextArray();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of nextJValue method, of class JSONDecoder.
     * /
    @Test
    public void testNextJValue() throws Exception {
        System.out.println("nextJValue");
        JSONDecoder instance = null;
        Object expResult = null;
        Object result = instance.nextJValue();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of parseValue method, of class JSONDecoder.
     * /
    @Test
    public void testParseValue() throws Exception {
        System.out.println("parseValue");
        String s = "";
        JSONDecoder instance = null;
        Object expResult = null;
        Object result = instance.parseValue(s);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of checkNext method, of class JSONDecoder.
     * /
    @Test
    public void testCheckNext() throws Exception {
        System.out.println("checkNext");
        char x = ' ';
        JSONDecoder instance = null;
        instance.checkNext(x);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of next method, of class JSONDecoder.
     * /
    @Test
    public void testNext_0args() throws Exception {
        System.out.println("next");
        JSONDecoder instance = null;
        char expResult = ' ';
        char result = instance.next();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of next method, of class JSONDecoder.
     * /
    @Test
    public void testNext_boolean() throws Exception {
        System.out.println("next");
        boolean ignoreWhitespace = false;
        JSONDecoder instance = null;
        char expResult = ' ';
        char result = instance.next(ignoreWhitespace);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of next method, of class JSONDecoder.
     * /
    @Test
    public void testNext_int() throws Exception {
        System.out.println("next");
        int n = 0;
        JSONDecoder instance = null;
        String expResult = "";
        String result = instance.next(n);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }*/

}
