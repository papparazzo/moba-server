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
 *  along with this program. If not, see <https://www.gnu.org/licenses/agpl.txt>.
 *
 */

package moba.server.json.stringreader;

import java.io.IOException;
import moba.server.json.streamreader.JSONStreamReaderI;

public class JSONStringReader {
    private final JSONStreamReaderI reader;
    private char lastChar = 0;

    public JSONStringReader(JSONStreamReaderI reader) {
        this.reader = reader;
    }

    public char peek() {
        try {
            char c = next();
            lastChar = c;
            return c;
        } catch(IOException e) {
            return 0;
        }
    }

    public void checkNext(String s)
    throws IOException {
        for(int i = 0; i < s.length(); i++) {
            checkNext(s.charAt(i));
        }
    }

    public void checkNext(char x)
    throws IOException {
        char c = next();
        if(c != x) {
            throw new IOException("expected '" + x + "' found '" + c + "'!");
        }
    }

    public char next()
    throws IOException {
        if(lastChar != 0) {
            char t = lastChar;
            lastChar = 0;
            return t;
        }
        int c = reader.read();
        if(c == -1 || c == 0) {
            throw new IOException("input stream corrupted!");
        }
        return (char)c;
    }

    public String next(int n)
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
