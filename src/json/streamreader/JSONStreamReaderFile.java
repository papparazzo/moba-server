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

package json.streamreader;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class JSONStreamReaderFile implements JSONStreamReaderI {
    private BufferedReader fh;

    public JSONStreamReaderFile(String fileName)
    throws FileNotFoundException {
        fh = new BufferedReader(new FileReader(fileName));
    }

    @Override
    public int read()
    throws IOException {
        return fh.read();
    }

    @Override
    public int peek()
    throws IOException {
        fh.mark(1);
        int ch = fh.read();
        fh.reset();
        return ch;
    }
}
