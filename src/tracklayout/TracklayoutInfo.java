/*
 *  appserver2
 *
 *  Copyright (C) 2014 Stefan Paproth <pappi-@gmx.de>
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

package tracklayout;

import json.streamwriter.*;
import java.io.*;
import java.text.*;
import java.util.*;
import json.*;

public class TracklayoutInfo implements JSONToStringI {
    protected long    id;
    protected String  name;
    protected String  description;
    protected Date    created;
    protected Date    modified;
    protected int     width;
    protected int     height;
    protected int     locked;

    public TracklayoutInfo(
        long id, String name, String description, int width, int height, int locked, Date modified, Date created
    ) {
        if(width < 1 || height < 1 || name == null || name.isEmpty()) {
            // FIXME Hier eine geeignete Exception werfen
            return;
        }
        if(modified == null) {
            modified = created;
        }

        this.id          = id;
        this.locked      = locked;
        this.name        = name;
        this.description = description;
        this.width       = width;
        this.height      = height;
        this.modified    = modified;
        this.created     = created;
    }

    public TracklayoutInfo(String name, String description, int width, int height) {
        this(-1, name, description, width, height, 0, new Date(), new Date());
    }

    public void setId(int id) {
        this.id = id;
    }

    public long getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public String getDescription() {
        return this.description;
    }

    public Date getModificationDate() {
        return this.modified;
    }

    public Date getCreationDate() {
        return this.created;
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    @Override
    public String toJsonString(boolean formated, int indent)
    throws IOException, JSONException {
        HashMap<String, Object> map = new HashMap<>();

        SimpleDateFormat df = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

        map.put("id",          this.id);
        map.put("name",        this.name);
        map.put("description", this.description);
        map.put("created",     df.format(this.created));
        map.put("modified",    df.format(this.modified));
        map.put("width",       this.width);
        map.put("height",      this.height);
        map.put("locked",      this.locked);

        StringBuilder sb = new StringBuilder();
        JSONStreamWriterStringBuilder jsb = new JSONStreamWriterStringBuilder(sb);
        JSONEncoder encoder = new JSONEncoder(jsb, formated);
        encoder.encode(map);
        return sb.toString();
    }
}