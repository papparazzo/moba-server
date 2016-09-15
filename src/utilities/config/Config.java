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

package utilities.config;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import json.JSONDecoder;
import json.JSONEncoder;
import json.JSONException;
import json.streamreader.JSONStreamReaderFile;
import json.streamwriter.JSONStreamWriterFile;

public class Config {
    protected String              fileName;
    protected Map<String, Object> content;

    public Config(String fileName)
    throws IOException, JSONException {
        JSONDecoder decoder = new JSONDecoder(
            new JSONStreamReaderFile(fileName),
            false
        );
        this.fileName = fileName;
        this.content = decoder.decode();
    }

    public void writeFile()
    throws IOException, JSONException {
        JSONEncoder encoder = new JSONEncoder(
            new JSONStreamWriterFile(this.fileName),
            true
        );
        encoder.encode(this.content);
    }

    public Object getSection(String expr) {
        if(this.content == null || this.content.isEmpty()) {
            return null;
        }

        String tokens[] = expr.split("\\.");

        Object o = this.content;

        for(String s : tokens) {
            Map<String, Object> map = (Map<String, Object>)o;
            if(map == null) {
                return null;
            }
            o = map.get(s);
        }

        return o;
    }

    public void setSection(String section, Object val) throws ConfigException {
        if(this.content == null) {
            this.content = new HashMap<>();
        }
        this.content.put(section, val);
    }

    public boolean removeSection(String section) throws ConfigException {
        if(this.content == null) {
            throw new ConfigException("object is null");
        }
        return (this.content.remove(section) != null);
    }

    public boolean sectionExists(String section) {
        return (this.getSection(section) != null);
    }
}