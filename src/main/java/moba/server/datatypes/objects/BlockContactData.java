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
package moba.server.datatypes.objects;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import moba.server.json.JSONEncoder;
import moba.server.json.JSONException;
import moba.server.json.JSONToStringI;
import moba.server.json.streamwriter.JSONStreamWriterStringBuilder;

public class BlockContactData implements JSONToStringI, Serializable {

    private ContactData breakTriggerContact;
    private ContactData blockContact;
    private int         localId;

    public BlockContactData() {
    }

    public BlockContactData(ContactData breakTriggerContact, ContactData blockContact, int localId) {
        this.breakTriggerContact = breakTriggerContact;
        this.blockContact = blockContact;
        this.localId = localId;
    }

    @Override
    public String toJsonString(boolean formated, int indent) throws JSONException, IOException {
        HashMap<String, Object> map = new HashMap<>();
        map.put("breakTriggerContact",  breakTriggerContact);
        map.put("blockContact", blockContact);
        map.put("localId", localId);

        StringBuilder sb = new StringBuilder();
        JSONStreamWriterStringBuilder jsb = new JSONStreamWriterStringBuilder(sb);
        JSONEncoder encoder = new JSONEncoder(jsb, formated);
        encoder.encode(map, indent);
        return sb.toString();
    }

    public ContactData getBreakTriggerContact() {
        return breakTriggerContact;
    }

    public ContactData getBlockContact() {
        return blockContact;
    }

    public int getLocalId() {
        return localId;
    }

    public void setBreakTriggerContact(ContactData value) {
        breakTriggerContact = value;
    }

    public void setBlockContact(ContactData value) {
        blockContact = value;
    }

    public void setLocalId(int value) {
        localId = value;
    }
}
