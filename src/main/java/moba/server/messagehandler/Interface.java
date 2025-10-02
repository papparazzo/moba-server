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

import moba.server.actionhandler.TrainRunner;
import moba.server.com.Dispatcher;
import moba.server.com.Endpoint;
import moba.server.datatypes.enumerations.Connectivity;
import moba.server.datatypes.enumerations.HardwareState;
import moba.server.datatypes.enumerations.IncidentLevel;
import moba.server.datatypes.enumerations.IncidentType;
import moba.server.datatypes.objects.TrainJourney;
import moba.server.messages.AbstractMessageHandler;
import moba.server.utilities.CheckedEnum;
import moba.server.datatypes.objects.IncidentData;
import moba.server.messages.Message;
import moba.server.messages.MessageQueue;
import moba.server.messages.messageType.InterfaceMessage;
import moba.server.messages.messageType.InternMessage;
import moba.server.exceptions.ClientErrorException;
import moba.server.utilities.messaging.IncidentHandler;

import java.sql.SQLException;

final public class Interface extends AbstractMessageHandler {

    private final MessageQueue msgQueueIn;
    private final IncidentHandler incidentHandler;
    private final TrainRunner runner;

    public Interface(Dispatcher dispatcher, MessageQueue msgQueueIn, IncidentHandler incidentHandler, TrainRunner runner) {
        this.dispatcher      = dispatcher;
        this.msgQueueIn      = msgQueueIn;
        this.incidentHandler = incidentHandler;
        this.runner          = runner;
    }

    @Override
    public int getGroupId() {
        return InterfaceMessage.GROUP_ID;
    }

    @Override
    public void handleMsg(Message msg)
    throws SQLException, ClientErrorException {
        Endpoint ep = msg.getEndpoint();

        switch(InterfaceMessage.fromId(msg.getMessageId())) {
            case CONNECTIVITY_STATE_CHANGED
                -> setConnectivity(CheckedEnum.getFromString(Connectivity.class, (String)msg.getData()), ep);

            case ROUTE_SWITCHED
                -> routeSwitched(msg);

            case ROUTE_RELEASED
                -> releaseRoute(msg);

            case BLOCK_RELEASED
                -> releaseBlock(msg);

            case PUSH_TRAIN
                -> pushTrain(msg);

            case SET_ACTION_LIST,
                 REPLACE_ACTION_LIST,
                 DELETE_ACTION_LIST
                -> dispatcher.sendGroup(msg);
        }
    }

    private void setConnectivity(Connectivity connectivity, Endpoint ep) {
        switch(connectivity) {
            case CONNECTED:
                msgQueueIn.add(new Message(InternMessage.SET_HARDWARE_STATE, HardwareState.MANUEL));
                addIncident(IncidentLevel.NOTICE, "Die Verbindung zur Hardware wurde hergestellt", ep);
                break;

            case ERROR:
                msgQueueIn.add(new Message(InternMessage.SET_HARDWARE_STATE, HardwareState.ERROR));
                addIncident(IncidentLevel.ERROR, "Die Verbindung zur Hardware wurde unterbrochen", ep);
                break;
        }
    }

    private void routeSwitched(Message msg) {
        int id = (int)msg.getData();
        runner.setSwitched(id);
    }

    private void releaseRoute(Message msg)
    throws SQLException, ClientErrorException {
        int id = (int)msg.getData();
        runner.releaseRoute(id);
    }

    private void releaseBlock(Message msg)
    throws SQLException, ClientErrorException {
        int blockId = (int)msg.getData();
        // TODO: Wir brauchen hier noch die TrainId
        int trainId = 0;
        runner.releaseBlock(trainId, blockId);
    }

    private void pushTrain(Message msg) {
        TrainJourney train = (TrainJourney)msg.getData();

        runner.pushTrain(train);
    }

    private void addIncident(IncidentLevel level, String message, Endpoint ep) {
        incidentHandler.add(new IncidentData(
            level,
            IncidentType.STATUS_CHANGED,
            "Hardwareverbindung",
            message,
            "Interface.addIncident()",
            ep
        ));
    }
}
