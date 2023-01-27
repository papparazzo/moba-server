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

public class BlockContactData {

    private final ContactData brakeTriggerContact;
    private final ContactData blockContact;
    private final int         id;
    private final int         xPos;
    private final int         yPos;

    public BlockContactData(int id, int xPos, int yPos, ContactData brakeTriggerContact, ContactData blockContact) {
        this.id   = id;
        this.xPos = xPos;
        this.yPos = yPos;
        this.brakeTriggerContact = brakeTriggerContact;
        this.blockContact = blockContact;
    }

    public int getId() {
        return id;
    }

    public int getXPos() {
        return xPos;
    }

    public int getYPos() {
        return yPos;
    }

    public ContactData getBrakeTriggerContact() {
        return brakeTriggerContact;
    }

    public ContactData getBlockContact() {
        return blockContact;
    }
}
