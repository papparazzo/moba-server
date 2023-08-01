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

package moba.server.utilities.config;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import moba.server.json.JSONException;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.inspector.TagInspector;
import org.yaml.snakeyaml.nodes.Tag;

public class Config {
    protected String              fileName;
    protected Map<String, Object> content;

    public Config(String fileName)
    throws IOException, JSONException, ConfigException {
        InputStream is = new FileInputStream(fileName);
        this.fileName = fileName;

        // https://www.veracode.com/blog/research/resolving-cve-2022-1471-snakeyaml-20-release-0
        LoaderOptions options = new LoaderOptions();
        TagInspector allowedTags = (Tag tag) -> true;
        options.setTagInspector(allowedTags);
        
        Yaml yaml = new Yaml(options);
        content = (Map<String, Object>)yaml.load(is);
        if(content == null || content.isEmpty()) {
            throw new ConfigException("content is empty");
        }
    }

    public void writeFile()
    throws IOException {
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setIndent(4);

        Yaml yaml = new Yaml(options);
        yaml.dump(content, new FileWriter(this.fileName));
    }

    public Object getSection(String expr) {
        String[] tokens = expr.split("\\.");

        Object o = content;

        for(String s : tokens) {
            Map<String, Object> map = (Map<String, Object>)o;
            if(map == null) {
                return null;
            }
            o = map.get(s);
        }

        if(o != null && o.getClass() == Integer.class) {
            o = (long)Long.valueOf((Integer)o);
        }

        return o;
    }

    public void setSection(String section, Object val)
    throws ConfigException {
        if(content == null) {
            content = new HashMap<>();
        }
        content.put(section, val);
    }

    public boolean removeSection(String section)
    throws ConfigException {
        if(content == null) {
            throw new ConfigException("object is null");
        }
        return (content.remove(section) != null);
    }

    public boolean sectionExists(String section) {
        return (getSection(section) != null);
    }
}