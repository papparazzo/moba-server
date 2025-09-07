/*
 *  Project:    moba-server
 *
 *  Copyright (C) 2025 Stefan Paproth <pappi-@gmx.de>
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

class VersionTest {

    @Test
    void testDefaultConstructor() {
        Version version = new Version();
        assertEquals("0.0.0-0000", version.toString());
    }

    @Test
    void testValidVersionStrings() {
        // Test verschiedene gültige Versionsformate
        assertDoesNotThrow(() -> new Version("1.0.0-1234"));
        assertDoesNotThrow(() -> new Version("2.1.0"));
        assertDoesNotThrow(() -> new Version("3"));
        assertDoesNotThrow(() -> new Version("4.5"));
    }

    @Test
    void testInvalidVersionStrings() {
        // Test ungültige Versionsformate
        assertThrows(IllegalArgumentException.class, () -> new Version(""));
        assertThrows(IllegalArgumentException.class, () -> new Version(null));
        assertThrows(IllegalArgumentException.class, () -> new Version("a.b.c"));
        assertThrows(IllegalArgumentException.class, () -> new Version("1.2.3-4-5"));
    }

    @Test
    void testVersionParsing() {
        Version version = new Version("1.2.3-456");
        assertEquals("1.2.3-0456", version.toString());
    }

    @Test
    void testPatchNumberLimit() {
        Version version = new Version("1.0.0-99999");
        assertEquals("1.0.0-9999", version.toString());
    }

    @Test
    void testCompareTo() {
        Version v1 = new Version("1.0.0-0");
        Version v2 = new Version("1.0.0-1");
        Version v3 = new Version("1.0.1-0");
        Version v4 = new Version("1.1.0-0");
        Version v5 = new Version("2.0.0-0");

        // Test verschiedene Vergleichsszenarien
        assertTrue(v1.compareTo(v2) < 0);
        assertTrue(v2.compareTo(v3) < 0);
        assertTrue(v3.compareTo(v4) < 0);
        assertTrue(v4.compareTo(v5) < 0);
        assertTrue(v5.compareTo(v1) > 0);
        assertEquals(0, v1.compareTo(new Version("1.0.0-0")));
    }

    @Test
    void testCompareMajor() {
        Version v1 = new Version("1.0.0-0");
        Version v2 = new Version("2.0.0-0");
        Version v3 = new Version("1.5.0-0");

        assertEquals(-1, v1.compareMajor(v2));
        assertEquals(1, v2.compareMajor(v1));
        assertEquals(0, v1.compareMajor(v3));
    }

    @Test
    void testCompareMinor() {
        Version v1 = new Version("1.0.0-0");
        Version v2 = new Version("1.1.0-0");
        Version v3 = new Version("1.0.5-0");

        assertEquals(-1, v1.compareMinor(v2));
        assertEquals(1, v2.compareMinor(v1));
        assertEquals(0, v1.compareMinor(v3));
    }

    @Test
    void testToString() {
        // Test verschiedene Formatierungen
        assertEquals("1.0.0-0000", new Version("1.0.0-0").toString());
        assertEquals("1.0.0-0001", new Version("1.0.0-1").toString());
        assertEquals("1.0.0-0012", new Version("1.0.0-12").toString());
        assertEquals("1.0.0-0123", new Version("1.0.0-123").toString());
        assertEquals("1.0.0-1234", new Version("1.0.0-1234").toString());
    }

    @Test
    void testPartialVersionString() {
        Version v1 = new Version("1");
        Version v2 = new Version("1.2");
        Version v3 = new Version("1.2.3");

        assertEquals("1.0.0-0000", v1.toString());
        assertEquals("1.2.0-0000", v2.toString());
        assertEquals("1.2.3-0000", v3.toString());
    }
}