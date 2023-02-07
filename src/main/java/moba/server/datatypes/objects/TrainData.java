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

import moba.server.datatypes.enumerations.DrivingDirection;

public class TrainData {
    protected int id;
    protected int address;
    protected int speed;
    protected DrivingDirection drivingDirection;

    public TrainData(int id, int address, int speed, DrivingDirection drivingDirection) {
       this.id = id;
       this.address = address;
       this.speed = speed;
       this.drivingDirection = drivingDirection;
    }

    public int getId() {
        return id;
    }

    public int getAddress() {
        return address;
    }

    public int getSpeed() {
        return speed;
    }

    public DrivingDirection getDrivingDirection() {
        return drivingDirection;
    }
}
