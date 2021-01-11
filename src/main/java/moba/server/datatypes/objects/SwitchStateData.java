/*
 *  Project:    moba-server
 *
 *  Copyright (C) 2021 Stefan Paproth <pappi-@gmx.de>
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

import moba.server.datatypes.enumerations.SwitchStand;

public class SwitchStateData {
    private final int         id;
    private final int         xPos;
    private final int         yPos;
    private final SwitchStand switchStand;

    public SwitchStateData(int id, int xPos, int yPos, SwitchStand switchStand) {
        this.id   = id;
        this.xPos = xPos;
        this.yPos = yPos;
        this.switchStand = switchStand;
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

    public SwitchStand getSwitchStand() {
        return switchStand;
    }
}
