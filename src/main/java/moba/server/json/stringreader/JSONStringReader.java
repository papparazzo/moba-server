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

package moba.server.json.stringreader;

import java.io.IOException;
import moba.server.json.streamreader.JSONStreamReaderI;


public class JSONStringReader {
    private JSONStreamReaderI reader = null;
    private char lastChar = 0;

    public JSONStringReader(JSONStreamReaderI reader) {
        this.reader = reader;
    }

    public char peek() {
        return peek(false);
    }

    public char peek(boolean ignoreWhitespace) {
        try {
            char c = next(ignoreWhitespace);
            lastChar = c;
            return c;
        } catch(IOException e) {
            return 0;
        }
    }

    public void checkNext(String s)
    throws IOException {
        checkNext(s, false);
    }

    public void checkNext(String s, boolean ignoreWhitespace)
    throws IOException {
        for (int i = 0; i < s.length(); i++){
            checkNext(s.charAt(i), ignoreWhitespace);
        }
    }

    public void checkNext(char x)
    throws IOException {
        checkNext(x, false);
    }

    public void checkNext(char x, boolean ignoreWhitespace)
    throws IOException {
        char c = next(ignoreWhitespace);
        if(c != x) {
            throw new IOException("expected '" + x + "' found '" + c + "'!");
        }
    }

    public char next()
    throws IOException {
        return next(false);
    }

    public char next(boolean ignoreWhitespace)
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

    public String next(int n)
    throws IOException {
        return next(n, false);
    }

    public String next(int n, boolean ignoreWhitespace)
    throws IOException {
        StringBuilder sb = new StringBuilder();
        if(n == 0) {
            return "";
        }

        for(int i = 0; i < n; ++i) {
            char c = next(ignoreWhitespace);
            sb.append(c);
        }
        return sb.toString();
    }
}
