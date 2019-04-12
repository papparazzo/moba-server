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
package datatypes.objects;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import json.JSONEncoder;
import json.JSONException;
import json.JSONToStringI;
import json.streamwriter.JSONStreamWriterStringBuilder;

public class TracklayoutData implements JSONToStringI {
    protected long    id;
    protected String  name;
    protected String  description;
    protected Date    created;
    protected Date    modified;
    protected int     locked;
    protected boolean active;

    public TracklayoutData(
        long id, String name, String description, int locked, boolean active, Date modified, Date created
    ) {
        if(name == null || name.isEmpty()) {
            name = "";
        }
        if(modified == null) {
            modified = created;
        }

        this.id          = id;
        this.locked      = locked;
        this.active      = active;
        this.name        = name;
        this.description = description;
        this.modified    = modified;
        this.created     = created;
    }

    public TracklayoutData(String name, String description) {
        this(-1, name, description, 0, false, new Date(), new Date());
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Date getModificationDate() {
        return modified;
    }

    public Date getCreationDate() {
        return created;
    }

    public boolean isActive() {
        return active;
    }

    @Override
    public String toJsonString(boolean formated, int indent)
    throws IOException, JSONException {
        HashMap<String, Object> map = new HashMap<>();

        SimpleDateFormat df = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

        map.put("id",          id);
        map.put("name",        name);
        map.put("description", description);
        map.put("created",     df.format(created));
        map.put("modified",    df.format(modified));
        map.put("active",      active);
        map.put("locked",      locked);

        StringBuilder sb = new StringBuilder();
        JSONStreamWriterStringBuilder jsb = new JSONStreamWriterStringBuilder(sb);
        JSONEncoder encoder = new JSONEncoder(jsb, formated);
        encoder.encode(map);
        return sb.toString();
    }
}