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

package moba.server.messagehandler;

import java.io.IOException;
import moba.server.com.Dispatcher;

import moba.server.datatypes.enumerations.HardwareState;
import moba.server.messages.AbstractMessageHandler;
import moba.server.messages.Message;
import moba.server.messages.messageType.EnvironmentMessage;
import moba.server.repositories.FunctionAddressesRepository;
import moba.server.exceptions.ClientErrorException;

/*
 * TODO Environment: Hier benötigen wir auch eine Rückmeldung, wann eine Aktion abgeschlossen ist.
 *                   Zum Beispiel Bahnübergang: Zug darf erst fahren wenn Schranken unten sind
 *                   Rückmeldung auch bei Rollladen rauf / runter, Gewittersturm, Orgelkonzert in Kirche...
 *
 *                   Action:
 *                       On:       eingeschaltet (z.B. Orgelkonzert, Endlosschleife)
 *                       Off:      ausgeschaltet
 *                       Trigger:  einmalig durchlaufen (Orgelkonzert einmalig, Lampe an, 5 Sek. warten, Lampe aus)
 *                                 Trigger verhält sich wie on bei Dingen, die nicht einmal durchlaufen werden können
 *                                 (z.B. Bahnübergängen)
 *
 *                   Status kann aktiv gemeldet werden wenn Aktion fertig (Finish) oder abgefragt werden (Running, Ready)
 *
 *                   Status:
 *                       Off:          ausgeschaltet.
 *                       SwitchingOn:  wird gerade eingeschaltet (Bahnübergang: Schranken senken sich)
 *                       Aktive:       eingeschaltet, aktiv, läuft aktuell
 *                       SwitchingOff: wird gerade ausgeschaltet (Bahnübergang: Schranken senken sich)
 *
 * Environment Applikation muss beim Shutdown speichern, welche Aktion gerade an war.
 *
 * FIXME: Müssen wir in der Tabelle FunktionAddresses speichern ob Kontakt nur "triggable" ist? -> nein!
 *
 */

final public class Environment extends AbstractMessageHandler {
    private final FunctionAddressesRepository addressesRepo;

    public Environment(Dispatcher dispatcher, FunctionAddressesRepository addressesRepo) {
        this.dispatcher = dispatcher;
        this.addressesRepo = addressesRepo;
    }

    @Override
    public int getGroupId() {
        return EnvironmentMessage.GROUP_ID;
    }

    @Override
    public void hardwareStateChanged(HardwareState state) {
        /*
         * TODO: Je nach HardwareState unterschiedliche Actions: Hier benötigen wir eine zusätzliche Tabelle:
         *       AmbienceData       ToggleState   curtainUp     Rollo rauf Rollo rauf runter
         * 	                        ToggleState   mainLightOn   Schreibtischlampe an / aus
         *       EMERGENCY_STOP: Hauptlicht an.
         *       AUTOMATIC:      Rollos runter
         *
         *       STANDBY,          // Energiesparmodus
         *       EMERGENCY_STOP,   // Nothalt
         *       MANUEL,           // Manueller Betrieb
         *       AUTOMATIC,        // Anlage im Automatikbetrieb
         *       AUTOMATIC_HALT    // Analge anhalten
         */
    }

    @Override
    public void handleMsg(Message msg)
    throws ClientErrorException, IOException {
        switch(EnvironmentMessage.fromId(msg.getMessageId())) {
            //case SET_FUNCTION ->

            //case GET_ENVIRONMENT ->
              //  dispatcher.sendSingle(new Message(EnvironmentMessage.SET_ENVIRONMENT, environment), msg.getEndpoint());

            //case SET_ENVIRONMENT ->
                //dispatcher.sendGroup(new Message(EnvironmentMessage.SET_ENVIRONMENT, environment));


/*
            case SET_AMBIENCE, SET_AMBIENT_LIGHT ->
                dispatcher.sendGroup(msg);
// TAgs??
          //  "SELECT Id, DeviceId, Address, Description, Active, ActiveInAutomaticMode"
*/
        }
    }
}
