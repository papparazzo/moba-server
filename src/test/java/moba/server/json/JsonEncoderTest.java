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

package moba.server.json;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import moba.server.json.streamwriter.JsonStreamWriterStringBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;

import java.io.IOException;
import java.net.InetAddress;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

import static org.junit.jupiter.api.Assertions.*;

class JsonEncoderTest {

    String encode(Object obj)
    throws IOException, JsonException {
        StringBuilder sb = new StringBuilder();
        JsonEncoder encoder = new JsonEncoder(new JsonStreamWriterStringBuilder(sb));
        encoder.encode(obj);
        return sb.toString();
    }

    @Test
    void constructor_withNullWriter_throwsIOException() {
        assertThrows(IOException.class, () -> new JsonEncoder(null));
    }

    @Test
    void testJsonInteger()
    throws IOException, JsonException {
        assertEquals("5", encode(5));
    }

    @Test
    void testJsonBoolean()
    throws IOException, JsonException {
        assertEquals("true", encode(true));
        assertEquals("false", encode(false));
    }

    @Test
    void testJsonString()
    throws IOException, JsonException {
        assertEquals("\"\"", encode(""));
        assertEquals("\"test\"", encode("test"));
    }

    @Test
    void testJsonNull()
    throws IOException, JsonException {
        assertNull(encode(null));
    }

    @Test
    void testJsonObject()
    throws IOException, JsonException {
        HashMap<String, Object> map = new HashMap<>();
        assertEquals("{}", encode(map));

        map.put("key", "value");
        assertEquals("{\"key\":\"value\"}", encode(map));

        map.put("e", 1);
        assertEquals("{\"e\":1,\"key\":\"value\"}", encode(map));

        map.put("t", true);
        assertEquals("{\"t\":true,\"e\":1,\"key\":\"value\"}", encode(map));
    }

    @Test
    void testJsonArray()
    throws IOException, JsonException {
        assertEquals("[]", encode(Collections.emptyList()));
    }
}