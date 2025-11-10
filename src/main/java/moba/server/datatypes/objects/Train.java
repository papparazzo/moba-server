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
 *  along with this program. If not, see <https://www.gnu.org/licenses/agpl.txt>.
 *
 */

package moba.server.datatypes.objects;

import moba.server.datatypes.enumerations.DrivingDirection;
import moba.server.datatypes.enumerations.TrainType;

// FIXME: Record Train ist abweichend: Hier ist die Id mit hinterlegt!!
public record Train(
    int trainId,
    int address,
    int speed,
    DrivingDirection drivingDirection,
    TrainType trainType,
    boolean hasPantograph,
    boolean noDirectionalControl // keine Fahrtrichtungsängerung (z.B. Güterzug)
) {
    public Train withDrivingDirection(DrivingDirection drivingDirection) {
        return new Train(trainId, address, speed, drivingDirection, trainType, hasPantograph, noDirectionalControl);
    }

    public Train withSpeed(int speed) {
        return new Train(trainId, address, speed, drivingDirection, trainType, hasPantograph, noDirectionalControl);
    }
/*
        # 'MaxGeschwindigkeit' => '80%',                // (FIXME: MoBa-Server???)
           # 'Position' => 12123,                          // Sollte eher vom moba-server verwaltet werden (FIXME: MoBa-Server???)
           # 'Bremsweg' => '50%',                          // Muss aus Geschwindigkeit berechnet werden (FIXME: MoBa-Server???)
           # 'aktuelle Richtung' => 'vorwärts',            // (FIXME: MoBa-Server???)
           # 'LocId' => '1234567890',                     // Decoder-Adresse (FIXME: MoBa-Server???)
*/
}
