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
 *  along with this program. If not, see <http://www.gnu.org/licenses/agpl.txt>.
 *
 */

package json;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import json.streamreader.JSONStreamReaderI;

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
        checkNext('{');
        return nextObject();
    }

    protected String nextKey()
    throws JSONException, IOException {
        char c;
        StringBuilder sb = new StringBuilder();

        for(int i = 0; i < JSONDecoder.MAX_STRING_LENGTH; ++i) {
            c = next();

            if(Character.isWhitespace(c) || !(Character.isLetterOrDigit(c) || c == '_' || c == '"')) {
                throw new JSONException("key contains invalide char!");
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

        for(int i = 0; i < JSONDecoder.MAX_STRING_LENGTH; ++i) {
            c = next(!strict);
            switch(c) {
                case '}':
                    if(!map.isEmpty())  {
                        throw new JSONException("expected new key");
                    }
                    return map;

                case '"':
                    key = nextKey();
                    break;

                default:
                    throw new JSONException("invalid key");
            }
            checkNext(':');

            if(map.containsKey(key)) {
                throw new JSONException("duplicate key <" + key + ">");
            }
            map.put(key, nextValue());

            switch(next(!strict)) {
                case ',':
                    break;

                case '}':
                    return map;

                default:
                    throw new JSONException("expected a ',' or '}'");
            }
        }
        throw new JSONException("maximum string-length of <" + JSONDecoder.MAX_STRING_LENGTH + "> reached!");
    }

    protected Object nextValue()
    throws JSONException, IOException {
        char c = next(!strict);
        switch(c) {
            case 'n':
                return nextNull();

            case 't':
                return nextTrue();

            case 'f':
                return nextFalse();

            case '"':
                return nextString();

            case '{':
                return nextObject();

            case '[':
                return nextArray();

            default:
                lastChar = c;
                return nextNumber();
        }
    }

    protected Object nextNull()
    throws JSONException, IOException {
        if(next() != 'u' || next() != 'l' || next() != 'l') {
            throw new JSONException("parsing error, value not 'null'");
        }
        return null;
    }

    protected Boolean nextTrue()
    throws JSONException, IOException {
        if(next() != 'r' || next() != 'u' || next() != 'e') {
            throw new JSONException("parsing error, value not 'true'");
        }
        return Boolean.TRUE;
    }

    protected Boolean nextFalse()
    throws JSONException, IOException {
        if(next() != 'a' || next() != 'l' || next() != 's' || next() != 'e') {
            throw new JSONException("parsing error, value not 'false'");
        }
        return Boolean.FALSE;
    }

    protected String nextString()
    throws JSONException, IOException {
        char c;
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < JSONDecoder.MAX_STRING_LENGTH; ++i) {
            c = next();
            switch(c) {
                case '\n':
                case '\r':
                    throw new JSONException("invalid char");

                case '\\':
                    c = next();
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
                            sb.append((char)Integer.parseInt(next(4), 16));
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
        throw new JSONException("maximum string-length of <" + JSONDecoder.MAX_STRING_LENGTH + "> reached!");
    }

    protected ArrayList nextArray()
    throws JSONException, IOException {
        ArrayList<Object> arrayList = new ArrayList<>();

        char c = next(!strict);

        if(c == ']') {
            return arrayList;
        }
        lastChar = c;
        arrayList.add(nextValue());

        while(true) {
            c = next(!strict);

            switch(c) {
                case ',':
                    arrayList.add(nextValue());
                    break;

                case ']':
                    return arrayList;

                default:
                    throw new JSONException("expected ',' or ']'");
            }
        }
    }

    protected Object nextNumber()
    throws JSONException, IOException {
        char c;
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < JSONDecoder.MAX_STRING_LENGTH; ++i) {
            c = next();

            if("\n\r ".indexOf(c) != -1) {
                continue;
            }

            if(",]}".indexOf(c) != -1) {
                return parseNumber(sb.toString());
            }

            if(Character.isDigit(c) || c == '-' || c == 'e' || c == 'E' || c == '.' || c == 'x' || c == 'X') {
                sb.append(c);
                continue;
            }
            throw new JSONException("parsing error");
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
            throw new JSONException("parsing error, number starts not with digit or -");
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
            throw new JSONException("parsing, error could not determine value: <" + s + ">", e);
        }
    }

    protected void checkNext(char x)
    throws IOException {
        char c = next(!strict);
        if(c != x) {
            throw new IOException("expected '" + x + "' found '" + c + "'!");
        }
    }

    protected char next()
    throws IOException {
        return next(false);
    }

    protected char next(boolean ignoreWhitespace)
    throws IOException {
        if(lastChar != 0) {
            char t = lastChar;
            lastChar = 0;
            return t;
        }
        int c;
        do {
            c = reader.read();
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
            char c = next();
            sb.append(c);
        }
        return sb.toString();
    }
}

