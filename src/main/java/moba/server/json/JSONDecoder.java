/*
 *  Project:    moba-server
 *
 *  Copyright (C) 2016 Stefan Paproth <pappi-@gmx.de>
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import moba.server.json.stringreader.JSONStringReader;

public class JSONDecoder {
    protected JSONStringReader reader;
    protected static final int  MAX_STRING_LENGTH = 1024;

    public JSONDecoder(JSONStringReader reader) {
        this.reader = reader;
    }

    public Object decode()
    throws JSONException, IOException {
        return nextValue();
    }

    protected String nextKey()
    throws JSONException, IOException {
        char c;
        StringBuilder sb = new StringBuilder();

        for(int i = 0; i < JSONDecoder.MAX_STRING_LENGTH; ++i) {
            c = reader.next();

            if(
                Character.isWhitespace(c) || 
                !(Character.isLetterOrDigit(c) || c == '_' || c == '"')
            ) {
                throw new JSONException("key contains invalid char!");
            }

            if(c == '"') {
                String s = sb.toString().trim();
                if(s.isEmpty()) {
                    throw new JSONException("key is empty");
                }
                return s;
            }
            sb.append(c);
        }
        throw new JSONException("maximum string-length of <" + JSONDecoder.MAX_STRING_LENGTH + "> reached!");
    }

    protected Map<String, Object> nextObject()
    throws JSONException, IOException {
        Map<String, Object> map = new HashMap<>();
        String key;
        char c;
        reader.checkNext('{');

        for(int i = 0; i < JSONDecoder.MAX_STRING_LENGTH; ++i) {
            c = reader.next();
            switch(c) {
                case '}' -> {
                    return map;
                }

                case '"' -> 
                    key = nextKey();

                default ->
                    throw new JSONException("invalid key: expected a '\"' or '}', got <" + c + "> instead!");
            }
            reader.checkNext(':');

            if(map.containsKey(key)) {
                throw new JSONException("duplicate key <" + key + ">");
            }
            map.put(key, nextValue());

            switch(reader.next()) {
                case ',' -> {
                }

                case '}' -> {
                    return map;
                }

                default -> 
                    throw new JSONException("expected a ',' or '}', got <" + c + "> instead!");
            }
        }
        throw new JSONException("maximum string-length of <" + JSONDecoder.MAX_STRING_LENGTH + "> reached!");
    }

    protected Object nextValue()
    throws JSONException, IOException {
        switch(reader.peek()) {
            case 'n' -> {
                return nextNull();
            }

            case 't' -> {
                return nextTrue();
            }

            case 'f' -> {
                return nextFalse();
            }

            case '"' -> {
                return nextString();
            }

            case '{' -> {
                return nextObject();
            }

            case '[' -> {
                return nextArray();
            }

            case 0 -> 
                throw new IOException("input stream corrupted!");

            default -> {
                return nextNumber();
            }
        }
    }

    protected Object nextNull()
    throws IOException {
        reader.checkNext("null");
        return null;
    }

    protected Boolean nextTrue()
    throws IOException {
        reader.checkNext("true");
        return Boolean.TRUE;
    }

    protected Boolean nextFalse()
    throws IOException {
        reader.checkNext("false");
        return Boolean.FALSE;
    }

    protected String nextString()
    throws JSONException, IOException {
        reader.checkNext('"');
        char c;
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < JSONDecoder.MAX_STRING_LENGTH; ++i) {
            c = reader.next();
            switch(c) {
                case '\n', '\r' -> 
                    throw new JSONException("invalid char");
                    
                case '\\' -> {
                    c = reader.next();
                    switch (c) {
                        case 'b' -> sb.append('\b');
                        case 't' -> sb.append('\t');
                        case 'n' -> sb.append('\n');
                        case 'f' -> sb.append('\f');
                        case 'r' -> sb.append('\r');
                        case 'u' -> sb.append((char)Integer.parseInt(reader.next(4), 16));

                        case '"', '\\', '/' -> sb.append(c);
                        default ->
                            throw new JSONException("invalid escape-sequence <"+ c +">");
                    }
                }

                case '"' -> {
                    return sb.toString();
                }

                default -> 
                    sb.append(c);
            }
        }
        throw new JSONException("maximum string-length of <" + JSONDecoder.MAX_STRING_LENGTH + "> reached!");
    }

    protected ArrayList<Object> nextArray()
    throws JSONException, IOException {
        ArrayList<Object> arrayList = new ArrayList<>();
        reader.checkNext('[');
        char c = reader.peek();

        if(c == ']') {
            reader.next();
            return arrayList;
        }
        arrayList.add(nextValue());

        while(true) {
            c = reader.next();

            switch(c) {
                case ',' -> arrayList.add(nextValue());

                case ']' -> {
                    return arrayList;
                }

                default ->
                    throw new JSONException("expected ',' or ']', got <" + c + "> instead!");
            }
        }
    }

    protected Object nextNumber()
    throws JSONException, IOException {
        char c;
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < JSONDecoder.MAX_STRING_LENGTH; ++i) {
            c = reader.peek();

            if(",]}".indexOf(c) != -1 || c == 0) {
                return parseNumber(sb.toString());
            }
            reader.next();

            if(Character.isDigit(c) || c == '-' || c == 'e' || c == 'E' || c == '.' || c == 'x' || c == 'X') {
                sb.append(c);
                continue;
            }
            throw new JSONException("expected digit, '-' or 'e', 'E', '.' or 'x', 'X' but found <" + c + ">!");
        }
        throw new JSONException("maximum string-length of <" + JSONDecoder.MAX_STRING_LENGTH + "> reached!");
    }

    protected Object parseNumber(String s)
    throws JSONException {
        s = s.trim();
        if(s.isEmpty()) {
            throw new JSONException("empty value");
        }

        char b = s.charAt(0);
        if(!Character.isDigit(b) && b != '-') {
            throw new JSONException("number starts not with digit or -");
        }

        try {
            if(b == '0' && s.length() > 2 && (s.charAt(1) == 'x' || s.charAt(1) == 'X')) {
                return Long.parseLong(s.substring(2), 16);
            }

            if(s.indexOf('.') > -1 || s.indexOf('e') > -1 || s.indexOf('E') > -1) {
                return Double.valueOf(s);
            }
            return Long.valueOf(s);
        } catch(NumberFormatException e) {
            throw new JSONException("could not determine value: <" + s + ">", e);
        }
    }
}
