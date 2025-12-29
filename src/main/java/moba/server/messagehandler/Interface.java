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
import moba.server.datatypes.enumerations.*;
import moba.server.datatypes.objects.EmergencyTriggerData;
import moba.server.datatypes.objects.IncidentData;
import moba.server.datatypes.objects.TrainJourney;
import moba.server.messages.AbstractMessageHandler;
import moba.server.messages.Message;
import moba.server.messages.MessageQueue;
import moba.server.messages.messagetypes.InterfaceMessage;
import moba.server.messages.messagetypes.InternMessage;
import moba.server.exceptions.ClientErrorException;
import moba.server.utilities.messaging.IncidentHandler;

import java.sql.SQLException;

final public class Interface extends AbstractMessageHandler {

    private final MessageQueue msgQueueIn;
    private final TrainRunner runner;
    private ServerState serverState = ServerState.HALT;
    private final IncidentHandler incidentHandler;

    public Interface(Dispatcher dispatcher, MessageQueue msgQueueIn, IncidentHandler incidentHandler, TrainRunner runner) {
        this.dispatcher      = dispatcher;
        this.msgQueueIn      = msgQueueIn;
        this.runner          = runner;
        this.incidentHandler = incidentHandler;
    }

    @Override
    public int getGroupId() {
        return InterfaceMessage.GROUP_ID;
    }

    @Override
    public void serverStateChanged(ServerState state) {
        serverState = state;
    }

    @Override
    public void handleMsg(Message msg)
    throws SQLException, ClientErrorException {
        switch(InterfaceMessage.fromId(msg.getMessageId())) {
            case CONNECTED
                -> setConnected(msg);

            case CONNECTION_LOST
                -> setConnectionLost(msg);

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

    private void setConnected(Message msg) {
        boolean onInitialize = (boolean)msg.getData();
         Endpoint ep =msg.getEndpoint();

        if(serverState == ServerState.HALT && onInitialize) {
            // TODO: Check, if ready for automatic-mode
            msgQueueIn.add(new Message(InternMessage.SET_SERVER_STATE, ServerState.MANUAL_MODE));
            addIncident(IncidentLevel.NOTICE, "Die Verbindung zur Hardware wurde hergestellt", ep);
            return;
        }
        if(onInitialize) {
            msgQueueIn.add(new Message(InternMessage.EMERGENCY_STOP, new EmergencyTriggerData(EmergencyTriggerReason.CONNECTION_LOST, "")));
            addIncident(IncidentLevel.CRITICAL, "Die Verbindung zur Hardware wurde hergestellt (ungültiger Zustand!)", ep);
            return;
        }
        msgQueueIn.add(new Message(InternMessage.EMERGENCY_STOP, new EmergencyTriggerData(EmergencyTriggerReason.CONNECTION_LOST, "")));
        addIncident(IncidentLevel.NOTICE, "Die Verbindung zur Hardware wurde wieder hergestellt", ep);
    }

    private void setConnectionLost(Message msg) {
        msgQueueIn.add(new Message(InternMessage.SET_SERVER_STATE, ServerState.CONNECTION_LOST));
        addIncident(
            IncidentLevel.ERROR,
            "Die Verbindung zur Hardware wurde unterbrochen",
            msg.getEndpoint()
        );
    }

    private void routeSwitched(Message msg) {
        int id = (int)msg.getData();
        runner.setSwitched(id);
        checkServerState();
    }

    private void releaseRoute(Message msg)
    throws SQLException, ClientErrorException {
        int id = (int)msg.getData();
        runner.releaseRoute(id);
        checkServerState();
    }

    private void releaseBlock(Message msg)
    throws SQLException, ClientErrorException {
        int blockId = (int)msg.getData();
        // TODO: Wir brauchen hier noch die TrainId
        int trainId = 0;
        runner.releaseBlock(trainId, blockId);
        checkServerState();
    }

    private void pushTrain(Message msg) {
        TrainJourney train = (TrainJourney)msg.getData();

        runner.pushTrain(train);
        checkServerState();
    }


    private void checkServerState() {
        if(runner.trainsToHandle()) {
            return;
        }

        if(serverState == ServerState.AUTOMATIC_HALT) {
            msgQueueIn.add(new Message(InternMessage.SET_SERVER_STATE, ServerState.READY_FOR_AUTOMATIC_MODE));
        } else if(serverState == ServerState.AUTOMATIC_HALT_FOR_SHUTDOWN) {
            msgQueueIn.add(new Message(InternMessage.SET_SERVER_STATE, ServerState.READY_FOR_SHUTDOWN));
        }
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
