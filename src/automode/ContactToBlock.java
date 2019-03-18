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

package automode;

import automode.node.Block;
import datatypes.objects.Contact;
import java.util.HashMap;
import java.util.Map;

public class ContactToBlock {
    protected final Map<Contact, Block> map;

    public ContactToBlock() {
         map = new HashMap<>();
    }

    public void addBlock(Contact contact, Block block) {
        map.put(contact, block);
    }

    public void freeBlock(Contact contact) {
        if(!map.containsKey(contact)) {
            return;
        }
        Block block = map.get(contact);
    }

    public void blockBlock(Contact contact) {
        if(!map.containsKey(contact)) {
            return;
        }
        Block block = map.get(contact);
    }
}
