/*
 *  Project:    moba-server
 *
 *  Copyright (C) 2020 Stefan Paproth <pappi-@gmx.de>
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

package moba.server.json.streamwriter;

import java.io.IOException;
import java.util.ArrayList;

public final class JSONMultiStreamWriter implements JSONStreamWriterI {

    protected ArrayList<JSONStreamWriterI> writers = new ArrayList<>();

    public JSONMultiStreamWriter() {

    }

    public JSONMultiStreamWriter(JSONStreamWriterI writer)
    throws IOException {
        addAdditionalWriter(writer);
    }

    public void addAdditionalWriter(JSONStreamWriterI writer)
    throws IOException {
        if(writer == null) {
            throw new IOException("stream-writer not set");
        }
        writers.add(writer);
    }

    @Override
    public void write(int i) throws IOException {
        for(JSONStreamWriterI writer : writers) {
            writer.write(i);
        }
    }

    @Override
    public void write(char c) throws IOException {
        for(JSONStreamWriterI writer : writers) {
            writer.write(c);
        }
    }

    @Override
    public void write(String s) throws IOException {
        for(JSONStreamWriterI writer : writers) {
            writer.write(s);
        }
    }

    @Override
    public void close() throws IOException {
        for(JSONStreamWriterI writer : writers) {
            writer.close();
        }
    }
}
