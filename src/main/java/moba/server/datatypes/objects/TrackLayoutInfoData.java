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
 *  along with this program. If not, see <https://www.gnu.org/licenses/agpl.txt>.
 *
 */

package moba.server.datatypes.objects;

import java.util.Date;

public class TrackLayoutInfoData {
    protected long    id;
    protected String  name;
    protected String  description;
    protected Date    created;
    protected Date    modified;
    protected long    locked;
    protected boolean active;

    public TrackLayoutInfoData(
        long id, String name, String description, long locked, boolean active, Date modified, Date created
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

    public TrackLayoutInfoData(String name, String description, long appId, boolean active) {
        this(-1, name, description, appId, active, new Date(), new Date());
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

    public Date getModified() {
        return modified;
    }

    public Date getCreated() {
        return created;
    }

    public boolean isActive() {
        return active;
    }

    public long getLocked() {
        return locked;
    }
}