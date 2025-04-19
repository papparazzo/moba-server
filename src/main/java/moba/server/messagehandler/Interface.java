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
 *  along with this program. If not, see <https://www.gnu.org/licenses/agpl.txt>.
 *
 */

package moba.server.messagehandler;

import moba.server.com.Dispatcher;
import moba.server.com.Endpoint;
import moba.server.datatypes.enumerations.Connectivity;
import moba.server.datatypes.enumerations.HardwareState;
import moba.server.utilities.CheckedEnum;
import moba.server.datatypes.objects.IncidentData;
import moba.server.messages.Message;
import moba.server.messages.MessageHandlerA;
import moba.server.messages.MessageQueue;
import moba.server.messages.messageType.InterfaceMessage;
import moba.server.messages.messageType.InternMessage;
import moba.server.utilities.exceptions.ClientErrorException;
import moba.server.utilities.messaging.IncidentHandler;

final public class Interface extends MessageHandlerA {

    private final MessageQueue msgQueueIn;
    private final IncidentHandler incidentHandler;

    public Interface(Dispatcher dispatcher, MessageQueue msgQueueIn, IncidentHandler incidentHandler) {
        this.dispatcher      = dispatcher;
        this.msgQueueIn      = msgQueueIn;
        this.incidentHandler = incidentHandler;
    }

    @Override
    public int getGroupId() {
        return InterfaceMessage.GROUP_ID;
    }

    @Override
    public void handleMsg(Message msg)
    throws ClientErrorException {
        switch(InterfaceMessage.fromId(msg.getMessageId())) {
            case CONNECTIVITY_STATE_CHANGED:
                setConnectivity(CheckedEnum.getFromString(Connectivity.class, (String)msg.getData()), msg.getEndpoint());
                return;

            case CONTACT_TRIGGERED:
            case SET_BRAKE_VECTOR:
            case RESET_BRAKE_VECTOR: 
            case SET_LOCO_SPEED:
            case SET_LOCO_DIRECTION:
            case SET_LOCO_FUNCTION:
            case SWITCH_ACCESSORY_DECODERS:
                dispatcher.broadcast(msg);
        }
    }

    private void setConnectivity(Connectivity connectivity, Endpoint ep) {
        switch(connectivity) {
            case CONNECTED:
                msgQueueIn.add(new Message(InternMessage.SET_HARDWARE_STATE, HardwareState.MANUEL));
                addIncident(IncidentData.Level.NOTICE, "Die Verbindung zur Hardware wurde hergestellt", ep);
                break;

            case ERROR:
                msgQueueIn.add(new Message(InternMessage.SET_HARDWARE_STATE, HardwareState.ERROR));
                addIncident(IncidentData.Level.ERROR, "Die Verbindung zur Hardware wurde unterbrochen", ep);
                break;
        }
    }

    private void addIncident(IncidentData.Level level, String message, Endpoint ep) {
        incidentHandler.add(new IncidentData(level, IncidentData.Type.STATUS_CHANGE, "Hardwareverbindung", message, ep));
    }
}
