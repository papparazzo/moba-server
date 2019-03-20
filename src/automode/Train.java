/*
 *  Project:    moba-server
 *
 *  Copyright (C) 2018 Stefan Paproth <pappi-@gmx.de>
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

import datatypes.enumerations.DrivingDirection;

public class Train {
	protected DrivingDirection virtualDirection;

    protected DrivingDirection realDirection;

    protected int speed;

    protected int address;

    protected boolean hasStopped;

    public Train(int address, int speed, DrivingDirection virtual, DrivingDirection real) {
        this.address = address;
        this.speed = speed;
        this.realDirection = real;
        this.virtualDirection = virtual;
    }

    public void switchVirtualDirection() {
        virtualDirection = DrivingDirection.flip(virtualDirection);
    }

    public void switchRealDirection() {
        realDirection = DrivingDirection.flip(realDirection);
        switchVirtualDirection();
    }

    public int getSpeed() {
        return speed;
    }

    public int getAddress() {
        return address;
    }



}

