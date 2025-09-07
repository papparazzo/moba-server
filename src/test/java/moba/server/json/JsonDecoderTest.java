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

import moba.server.json.streamreader.JsonStreamReaderBytes;
import moba.server.json.stringreader.JsonStringReader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;

import java.io.IOException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class JsonDecoderTest {

    JsonDecoder getDecoder(String inputString) {
        byte[] byteArrray = inputString.getBytes();
        return new JsonDecoder(new JsonStringReader(new JsonStreamReaderBytes(byteArrray)));
    }

    @Nested
    class PrimitiveValueTests {
        @Test
        void decode_null_returnsNull() throws JsonException, IOException {
            Object result = getDecoder("null").decode();
            assertNull(result);
        }

        @Test
        void decode_boolean_returnsBoolean() throws JsonException, IOException {
            assertTrue((Boolean) getDecoder("true").decode());
            assertFalse((Boolean) getDecoder("false").decode());
        }

        @Test
        void decode_integer_returnsLong() throws JsonException, IOException {
            assertEquals(42L, getDecoder("42").decode());
            assertEquals(-17L, getDecoder("-17").decode());
        }

        @Test
        void decode_decimal_returnsDouble() throws JsonException, IOException {
            assertEquals(3.14, getDecoder("3.14").decode());
            assertEquals(-0.001, getDecoder("-0.001").decode());
        }

        @Test
        void decode_string_returnsString() throws JsonException, IOException {
            assertEquals("Hello World", getDecoder("\"Hello World\"").decode());
            assertEquals("", getDecoder("\"\"").decode());
        }
    }

    @Nested
    class StringTests {
        @Test
        void decode_stringWithEscapeSequences_returnsDecodedString() throws JsonException, IOException {
            assertEquals("Line 1\nLine 2", getDecoder("\"Line 1\\nLine 2\"").decode());
            assertEquals("Tab\there", getDecoder("\"Tab\\there\"").decode());
            assertEquals("Quote\"inside", getDecoder("\"Quote\\\"inside\"").decode());
            assertEquals("Back\\slash", getDecoder("\"Back\\\\slash\"").decode());
        }

        @Test
        void decode_unicodeSequences_returnsDecodedString() throws JsonException, IOException {
            assertEquals("Hello ♥", getDecoder("\"Hello \\u2665\"").decode());
            assertEquals("←→", getDecoder("\"\\u2190\\u2192\"").decode());
        }
    }

    @Nested
    class ArrayTests {
        @Test
        void decode_emptyArray_returnsEmptyList() throws JsonException, IOException {
            Object result = getDecoder("[]").decode();
            assertInstanceOf(List.class, result);
            assertEquals(0, ((List<?>) result).size());
        }

        @Test
        void decode_arrayWithPrimitives_returnsList() throws JsonException, IOException {
            Object result = getDecoder("[1, true, \"text\", null]").decode();
            assertInstanceOf(List.class, result);
            List<?> list = (List<?>) result;
            assertEquals(4, list.size());
            assertEquals(1L, list.get(0));
            assertEquals(true, list.get(1));
            assertEquals("text", list.get(2));
            assertNull(list.get(3));
        }

        @Test
        void decode_nestedArrays_returnsNestedLists() throws JsonException, IOException {
            Object result = getDecoder("[[1,2],[3,4]]").decode();
            assertInstanceOf(List.class, result);
            List<?> outer = (List<?>) result;
            assertEquals(2, outer.size());
            assertInstanceOf(List.class, outer.get(0));
            assertInstanceOf(List.class, outer.get(1));
        }
    }

    @Nested
    class ObjectTests {
        @Test
        void decode_emptyObject_returnsEmptyMap() throws JsonException, IOException {
            Object result = getDecoder("{}").decode();
            assertInstanceOf(Map.class, result);
            assertEquals(0, ((Map<?, ?>) result).size());
        }

        @Test
        void decode_simpleObject_returnsMap() throws JsonException, IOException {
            Object result = getDecoder("{\"name\":\"John\",\"age\":30}").decode();
            assertInstanceOf(Map.class, result);
            Map<?, ?> map = (Map<?, ?>) result;
            assertEquals("John", map.get("name"));
            assertEquals(30L, map.get("age"));
        }

        @Test
        void decode_nestedObject_returnsNestedMap() throws JsonException, IOException {
            Object result = getDecoder("{\"person\":{\"name\":\"John\",\"age\":30}}").decode();
            assertInstanceOf(Map.class, result);
            Map<?, ?> outer = (Map<?, ?>) result;
            assertInstanceOf(Map.class, outer.get("person"));
            Map<?, ?> person = (Map<?, ?>) outer.get("person");
            assertEquals("John", person.get("name"));
            assertEquals(30L, person.get("age"));
        }

        @Test
        void decode_objectWithArray_returnsMapWithList() throws JsonException, IOException {
            Object result = getDecoder("{\"numbers\":[1,2,3]}").decode();
            assertInstanceOf(Map.class, result);
            Map<?, ?> map = (Map<?, ?>) result;
            assertInstanceOf(List.class, map.get("numbers"));
            List<?> numbers = (List<?>) map.get("numbers");
            assertEquals(Arrays.asList(1L, 2L, 3L), numbers);
        }
    }

    @Nested
    class ErrorTests {
        @Test
        void decode_invalidJson_throwsJsonException() {
            assertThrows(JsonException.class, () -> getDecoder("{invalid}").decode());
            assertThrows(JsonException.class, () -> getDecoder("[1,2,]").decode());
            assertThrows(JsonException.class, () -> getDecoder("{'single':quotes}").decode());
        }

        @Test
        void decode_incompleteJson_throwsJsonException() {
            assertThrows(JsonException.class, () -> getDecoder("{\"name\":").decode());
            assertThrows(JsonException.class, () -> getDecoder("[1,2").decode());
            assertThrows(JsonException.class, () -> getDecoder("\"unclosed").decode());
        }

        @Test
        void decode_emptyInput_throwsJsonException() {
            assertThrows(JsonException.class, () -> getDecoder("").decode());
            assertThrows(JsonException.class, () -> getDecoder("  ").decode());
        }
    }

    @Nested
    class WhitespaceTests {
        @Test
        void decode_jsonWithWhitespace_parsesCorrectly() throws JsonException, IOException {
            String json = """
                {
                    "name": "John",
                    "age": 30,
                    "hobbies": [
                        "reading",
                        "gaming"
                    ]
                }
                """;
            Object result = getDecoder(json).decode();
            assertInstanceOf(Map.class, result);
            Map<?, ?> map = (Map<?, ?>) result;
            assertEquals("John", map.get("name"));
            assertEquals(30L, map.get("age"));
            assertInstanceOf(List.class, map.get("hobbies"));
            List<?> hobbies = (List<?>) map.get("hobbies");
            assertEquals(Arrays.asList("reading", "gaming"), hobbies);
        }
    }

    @Nested
    class SpecialNumberTests {
        @Test
        void decode_scientificNotation_returnsDouble() throws JsonException, IOException {
            assertEquals(1.23e-4, getDecoder("1.23e-4").decode());
            assertEquals(1.23E+4, getDecoder("1.23E+4").decode());
        }

        @Test
        void decode_largeNumbers_handlesCorrectly() throws JsonException, IOException {
            assertEquals(Long.MAX_VALUE, getDecoder(String.valueOf(Long.MAX_VALUE)).decode());
            assertEquals(Double.MAX_VALUE, getDecoder(String.valueOf(Double.MAX_VALUE)).decode());
        }
    }
}