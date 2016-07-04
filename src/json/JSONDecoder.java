/*
 *  json
 *
 *  Copyright (C) 2013 Stefan Paproth <pappi-@gmx.de>
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
import java.util.*;

import json.streamreader.*;
import utilities.Switch;

public class JSONDecoder {
    private JSONStreamReaderI reader = null;
    private boolean           strict;
    private static final int  MAX_STRING_LENGTH = 1024;
    private char              lastChar = 0;

    public JSONDecoder(JSONStreamReaderI reader)
    throws JSONException {
        this(reader, true);
    }

    public JSONDecoder(JSONStreamReaderI reader, boolean strict)
    throws JSONException {
        this.reader = reader;
        this.strict = strict;
    }

    public Map<String, Object> decode()
    throws JSONException, IOException {
        this.checkNext('{');
        return this.nextObject();
    }

    protected String nextKey()
    throws JSONException, IOException {
        char c;
        StringBuilder sb = new StringBuilder();

        for(int i = 0; i < JSONDecoder.MAX_STRING_LENGTH; ++i) {
            c = this.next();

            if(
                Character.isWhitespace(c) ||
                !(Character.isLetterOrDigit(c) || c == '_' || c == '"')
            ) {
                throw new JSONException("key contains invalide char!");
            }

            if(c == '"') {
                String s = sb.toString().trim();
                if(s.isEmpty()) {
                    throw new JSONException("key is empty");
                } else {
                    return s;
                }
            }
            sb.append(c);
        }
        throw new JSONException(
            "maximum string-length of <" +
            JSONDecoder.MAX_STRING_LENGTH + "> reached!"
        );
    }

    protected Map<String, Object> nextObject()
    throws JSONException, IOException {
        Map<String, Object> map = new HashMap<>();
        String key;
        char c;

        for(int i = 0; i < JSONDecoder.MAX_STRING_LENGTH; ++i) {
            c = this.next(!this.strict);
            switch(c) {
                case '}':
                    return map;

                case '"':
                    key = this.nextKey();
                    break;

                default:
                    throw new JSONException("invalid key");
            }
            this.checkNext(':');

            if(map.containsKey(key)) {
                throw new JSONException("duplicate key <" + key + ">");
            }
            map.put(key, this.nextValue());

            switch(this.next(!this.strict)) {
                case ',':
                    c = this.next(!this.strict);
                    if(c == '}') {
                        throw new JSONException("expected new key");
                    }
                    this.lastChar = c;
                    break;

                case '}':
                    return map;

                default:
                    throw new JSONException("expected a ',' or '}'");
            }
        }
        throw new JSONException(
            "maximum string-length of <" +
            JSONDecoder.MAX_STRING_LENGTH + "> reached!"
        );
    }

    protected Object nextValue()
    throws JSONException, IOException {
        char c = this.next(!this.strict);
        switch(c) {
            case '"':
                return this.nextString();
            case '{':
                return this.nextObject();
            case '[':
                return this.nextArray();
            default:
                this.lastChar = c;
                return this.nextJValue();
        }
    }

    protected String nextString()
    throws JSONException, IOException {
        char c;
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < JSONDecoder.MAX_STRING_LENGTH; ++i) {
            c = this.next();
            switch(c) {
                case '\n':
                case '\r':
                    throw new JSONException("invalid char");

                case '\\':
                    c = this.next();
                    switch (c) {
                        case 'b':
                            sb.append('\b');
                            break;

                        case 't':
                            sb.append('\t');
                            break;

                        case 'n':
                            sb.append('\n');
                            break;

                        case 'f':
                            sb.append('\f');
                            break;

                        case 'r':
                            sb.append('\r');
                            break;

                        case 'u':
                            sb.append((char)Integer.parseInt(this.next(4), 16));
                            break;

                        case '"':
                        case '\\':
                        case '/':
                            sb.append(c);
                            break;

                        default:
                            throw new JSONException("invalid escape-sequence");
                    }
                    break;

                case '"':
                    return sb.toString();

                default:
                    sb.append(c);
                    break;
            }
        }
        throw new JSONException(
            "maximum string-length of <" +
            JSONDecoder.MAX_STRING_LENGTH + "> reached!"
        );
    }

    protected ArrayList nextArray()
    throws JSONException, IOException {
        ArrayList<Object> arrayList = new ArrayList<>();

        char c = this.next();

        if(c == ']') {
            return arrayList;
        }
        this.lastChar = c;
        arrayList.add(this.nextValue());

        while(true) {
            c = this.next();

            switch(c) {
                case ',':
                    arrayList.add(this.nextValue());
                    break;

                case ']':
                    return arrayList;

                default:
                    throw new JSONException("expected ',' or ']'");
            }
        }
    }

    protected Object nextJValue() throws JSONException, IOException {
        char c;
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < JSONDecoder.MAX_STRING_LENGTH; ++i) {
            c = this.next();

            if("\n\r ".indexOf(c) != -1) {
                continue;
            }

            if(",]}".indexOf(c) != -1) {
                this.lastChar = c;
                return this.parseValue(sb.toString());
            }

            if(
                (c >= 'A' && c <= 'Z') ||
                (c >= 'a' && c <= 'z') ||
                (c >= '0' && c <= '9') ||
                c == '-'
            ) {
                sb.append(c);
                continue;
            }
            throw new JSONException("parsing error");
        }
        throw new JSONException(
            "maximum string-length of <" +
            JSONDecoder.MAX_STRING_LENGTH + "> reached!"
        );
    }

    protected Object parseValue(String s)
    throws JSONException {
        s = s.trim();
        if(s.isEmpty()) {
            throw new JSONException("empty value");
        }
        if(s.equalsIgnoreCase("true")) {
            return Boolean.TRUE;
        }
        if(s.equalsIgnoreCase("false")) {
            return Boolean.FALSE;
        }
        if(s.equalsIgnoreCase("null")) {
            return null;
        }

        if(s.equalsIgnoreCase("auto")) {
            return Switch.AUTO;
        }

        if(s.equalsIgnoreCase("on")) {
            return Switch.ON;
        }

        if(s.equalsIgnoreCase("off")) {
            return Switch.OFF;
        }

        if(s.equalsIgnoreCase("unset")) {
            return Switch.UNSET;
        }

        if(s.equalsIgnoreCase("trigger")) {
            return Switch.TRIGGER;
        }

        char b = s.charAt(0);
        try {
            if((b >= '0' && b <= '9') || b == '-') {
                if(
                    b == '0' &&
                    s.length() > 2 &&
                    (s.charAt(1) == 'x' || s.charAt(1) == 'X')
                ) {
                    return Long.parseLong(s.substring(2), 16);
                }

                if(
                    s.indexOf('.') > -1 ||
                    s.indexOf('e') > -1 ||
                    s.indexOf('E') > -1
                ) {
                    return Double.valueOf(s);
                } else {
                    return Long.valueOf(s);
                }
            }
        } catch(Exception e) {
            throw new JSONException(
                "parsing, error could not determine value: <" +
                e.toString() + ">"
            );
        }
        throw new JSONException("parsing error, could not determine value" );
    }

    protected void checkNext(char x)
    throws IOException {
        char c = this.next(!this.strict);
        if(c != x) {
            throw new IOException("expected '" + x + "' found '" + c + "'!");
        }
    }

    protected char next() throws IOException {
        return this.next(false);
    }

    protected char next(boolean ignoreWhitespace)
    throws IOException {
        if(this.lastChar != 0) {
            char t = this.lastChar;
            this.lastChar = 0;
            return t;
        }
        int c;
        do {
            c = this.reader.read();
        } while(Character.isWhitespace(c) && ignoreWhitespace);
//System.err.print((char)c);
        if(c == -1 || c == 0) {
            throw new IOException("input stream corrupted!");
        }
        return (char)c;
    }

    protected String next(int n)
    throws IOException {
        StringBuilder sb = new StringBuilder();
        if(n == 0) {
            return "";
        }

        for(int i = 0; i < n; ++i) {
            char c = this.next();
            sb.append(c);
        }
        return sb.toString();
    }
}

