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

import moba.server.com.Dispatcher;
import moba.server.com.Endpoint;
import moba.server.datatypes.enumerations.*;
import moba.server.messages.AbstractMessageHandler;
import moba.server.datatypes.objects.ErrorData;
import moba.server.datatypes.objects.IncidentData;
import moba.server.messages.Message;
import moba.server.messages.MessageQueue;
import moba.server.messages.messagetypes.ClientMessage;
import moba.server.messages.messagetypes.InternMessage;
import moba.server.messages.messagetypes.SystemMessage;
import moba.server.exceptions.ClientErrorException;
import moba.server.utilities.CheckedEnum;
import moba.server.utilities.layout.ActiveTrackLayout;
import moba.server.utilities.layout.TrackLayoutLock;
import moba.server.utilities.messaging.IncidentHandler;

import java.sql.SQLException;

final public class Systems extends AbstractMessageHandler {
    private ServerState state = ServerState.HALT;

    private final MessageQueue msgQueue;
    private final ActiveTrackLayout activeLayout;
    private final TrackLayoutLock lock;
    private final IncidentHandler incidentHandler;

    public Systems(
        Dispatcher dispatcher,
        TrackLayoutLock lock,
        ActiveTrackLayout activeLayout,
        MessageQueue msgQueue,
        IncidentHandler incidentHandler
    ) throws SQLException {
        this.incidentHandler = incidentHandler;
        this.dispatcher      = dispatcher;
        this.msgQueue        = msgQueue;
        this.activeLayout    = activeLayout;
        this.lock            = lock;
        this.lock.resetAll();
    }

    @Override
    public int getGroupId() {
        return SystemMessage.GROUP_ID;
    }

    @Override
    public void reset()
    throws SQLException {
        lock.resetAll();
        state = ServerState.HALT;
    }

    @Override
    public void serverStateChanged(ServerState state) {
        this.state = state;
        dispatcher.sendGroup(new Message(SystemMessage.HARDWARE_STATE_CHANGED, this.state.toSystemState().toString()));
    }

    @Override
    public void handleMsg(Message msg)
    throws ClientErrorException, SQLException {
        switch(SystemMessage.fromId(msg.getMessageId())) {
            case SET_AUTOMATIC_MODE:
                setAutomaticMode(msg);
                break;

            case TRIGGER_EMERGENCY_STOP:
                triggerEmergencyStop(msg);
                break;

            case RELEASE_EMERGENCY_STOP:
                releaseEmergencyStop(msg);
                break;

            case SET_STANDBY_MODE:
                setStandByMode(msg);
                break;

            case TOGGLE_STANDBY_MODE:
                setStandByMode(msg, true);
                break;

            case GET_HARDWARE_STATE:
                dispatcher.sendSingle(new Message(SystemMessage.HARDWARE_STATE_CHANGED, status.toString()), msg.getEndpoint());
                break;

            case HARDWARE_SHUTDOWN:
                setHardwareShutdown(msg.getEndpoint());
                break;

            case HARDWARE_RESET:
                msgQueue.add(new Message(InternMessage.SERVER_RESET, null));
                break;
        }
    }

    private void setAutomaticMode(Message msg) {
       if((boolean)msg.getData()) {
           setAutomaticModeOn(msg.getEndpoint());
       } else {
           setAutomaticModeOff(msg.getEndpoint());
       }
    }

    private void setAutomaticModeOn(Endpoint endpoint) {
        if(state != ServerState.READY_FOR_AUTOMATIC_MODE) {
           sendErrorMessage(ServerState.AUTOMATIC_MODE, endpoint);
           return;
        }

        msgQueue.add(new Message(InternMessage.SET_SERVER_STATE, ServerState.AUTOMATIC_MODE));
        incidentHandler.add(new IncidentData(
            IncidentLevel.NOTICE,
            IncidentType.STATUS_CHANGED,
            "Automatik an",
            "Die Hardware befindet sich im Automatikmodus",
            "Systems.setAutomaticMode()",
            endpoint
        ));
    }

    private void setAutomaticModeOff(Endpoint endpoint) {
        if(state != ServerState.AUTOMATIC_MODE) {
           sendErrorMessage(ServerState.MANUAL_MODE, endpoint);
           return;
        }

        msgQueue.add(new Message(InternMessage.SET_SERVER_STATE, ServerState.AUTOMATIC_HALT));
        incidentHandler.add(new IncidentData(
            IncidentLevel.NOTICE,
            IncidentType.STATUS_CHANGED,
            "Automatik anhalten",
            "Automatikmodus wird deaktiviert...",
            "Systems.setAutomaticMode()",
            endpoint
        ));
    }

    private void triggerEmergencyStop(Message msg)
    throws ClientErrorException {
        switch(state) {
            case AUTOMATIC_MODE:
                setEmergencyStopReason(msg);
                msgQueue.add(new Message(InternMessage.SET_SERVER_STATE, ServerState.EMERGENCY_STOP_IN_AUTOMATIC_MODE));
                break;

            case AUTOMATIC_HALT:
                setEmergencyStopReason(msg);
                msgQueue.add(new Message(InternMessage.SET_SERVER_STATE, ServerState.EMERGENCY_STOP_IN_AUTOMATIC_HALT));
                break;

            case AUTOMATIC_HALT_FOR_SHUTDOWN:
                setEmergencyStopReason(msg);
                msgQueue.add(new Message(
                    InternMessage.SET_SERVER_STATE,
                    ServerState.EMERGENCY_STOP_IN_AUTOMATIC_HALT_FOR_SHUTDOWN
                ));
                break;

            case MANUAL_MODE:
            case READY_FOR_AUTOMATIC_MODE:
                setEmergencyStopReason(msg);
                msgQueue.add(new Message(InternMessage.SET_SERVER_STATE, ServerState.EMERGENCY_STOP_IN_MANUAL_MODE));
                break;

            case EMERGENCY_STOP_IN_MANUAL_MODE:
            case EMERGENCY_STOP_IN_AUTOMATIC_MODE:
            case STANDBY_IN_MANUAL_MODE:
            case STANDBY_IN_AUTOMATIC_MODE:
            case HALT:
            case ERROR:
            case READY_FOR_SHUTDOWN:
                sendErrorMessage(ServerState.EMERGENCY_STOP_IN_MANUAL_MODE, msg.getEndpoint());
        }
    }

    private void setEmergencyStopReason(Message msg)
    throws ClientErrorException {
        String reason = switch(CheckedEnum.getFromString(EmergencyTriggerReason.class, (String)msg.getData())) {
            case CENTRAL_STATION                 -> "Es wurde ein Nothalt durch die CentralStation ausgelöst";
            case EXTERN                          -> "Externe Hardware";
            case SELF_ACTING_BY_EXTERN_SWITCHING -> "Weichenstellung durch CS im Automatikmodus";
            case SOFTWARE_ERROR                  -> "Automatisch durch Softwarefehler";
            case SOFTWARE_MANUAL                 -> "Manuell durch Steuerungssoftware";
        };

        incidentHandler.add(new IncidentData(
            IncidentLevel.WARNING,
            IncidentType.STATUS_CHANGED,
            "Nothalt gedrückt",
            "Auslösegrund: " + reason,
            "Systems.setEmergencyStopReason()",
            msg.getEndpoint()
        ));
    }

    private void releaseEmergencyStop(Message msg) {
        switch(state) {
            case EMERGENCY_STOP_IN_AUTOMATIC_HALT:
                msgQueue.add(new Message(InternMessage.SET_SERVER_STATE, ServerState.AUTOMATIC_HALT));
                break;

            case EMERGENCY_STOP_IN_AUTOMATIC_HALT_FOR_SHUTDOWN:
                msgQueue.add(new Message(InternMessage.SET_SERVER_STATE, ServerState.AUTOMATIC_HALT_FOR_SHUTDOWN));
                break;

            case EMERGENCY_STOP_IN_MANUAL_MODE:
                msgQueue.add(new Message(InternMessage.SET_SERVER_STATE, ServerState.MANUAL_MODE));
                break;

            case EMERGENCY_STOP_IN_AUTOMATIC_MODE:
                msgQueue.add(new Message(InternMessage.SET_SERVER_STATE, ServerState.AUTOMATIC_MODE));
                break;

            case AUTOMATIC_MODE:
            case AUTOMATIC_HALT:
            case AUTOMATIC_HALT_FOR_SHUTDOWN:
            case MANUAL_MODE:
            case READY_FOR_AUTOMATIC_MODE:
            case STANDBY_IN_MANUAL_MODE:
            case STANDBY_IN_AUTOMATIC_MODE:
            case HALT:
            case ERROR:
            case READY_FOR_SHUTDOWN:
                sendErrorMessage(ServerState.EMERGENCY_STOP_IN_MANUAL_MODE, msg.getEndpoint());
        }

        incidentHandler.add(new IncidentData(
            IncidentLevel.NOTICE,
            IncidentType.STATUS_CHANGED,
            "Nothaltfreigabe",
            "Der Nothalt wurde wieder freigegeben.",
            "Systems.releaseEmergencyStop()",
            msg.getEndpoint()
        ));
    }

    private void setStandByMode(Message msg) {
        setStandByMode(msg, false);
    }

    private void setStandByMode(Message msg, boolean toggle) {
        switch(state) {
            case EMERGENCY_STOP_IN_AUTOMATIC_HALT:
            case EMERGENCY_STOP_IN_AUTOMATIC_HALT_FOR_SHUTDOWN:
            case EMERGENCY_STOP_IN_MANUAL_MODE:
            case EMERGENCY_STOP_IN_AUTOMATIC_MODE:
            case AUTOMATIC_HALT:
            case AUTOMATIC_HALT_FOR_SHUTDOWN:
            case HALT:
            case ERROR:
            case READY_FOR_AUTOMATIC_MODE:
            case READY_FOR_SHUTDOWN:
                sendErrorMessage(ServerState.EMERGENCY_STOP_IN_MANUAL_MODE, msg.getEndpoint());
                return;
        }

        boolean setStandByMode;

        if(toggle) {
            setStandByMode = (status != SystemState.STANDBY);
        } else {
            setStandByMode = (boolean)msg.getData();

            if(setStandByMode && status == SystemState.STANDBY) {
                sendErrorMessage(msg.getEndpoint());
                return;
            }
        }

        if(setStandByMode) {
            msgQueue.add(new Message(InternMessage.SET_HARDWARE_STATE, HardwareState.STANDBY));
            incidentHandler.add(new IncidentData(
                IncidentLevel.NOTICE,
                IncidentType.STATUS_CHANGED,
                "Standby an",
                "Anlage wird in den Standby-Modus geschickt",
                "Systems.setStandByMode()",
                msg.getEndpoint()
            ));
            return;
        }

        if(status != SystemState.STANDBY) {
            sendErrorMessage(msg.getEndpoint());
            return;
        }

        if(automaticMode) {
            msgQueue.add(new Message(InternMessage.SET_HARDWARE_STATE, HardwareState.AUTOMATIC));
        } else {
            msgQueue.add(new Message(InternMessage.SET_HARDWARE_STATE, HardwareState.MANUEL));
        }

        incidentHandler.add(new IncidentData(
            IncidentLevel.NOTICE,
            IncidentType.STATUS_CHANGED,
            "Standby aus",
            "Die Anlage wird aus dem Standby-Modus geholt" +
            "Anlage wechselt in den " + (automaticMode ? "Automatikmodus" : "Manuellmodus"),
            "Systems.setStandByMode()",
            msg.getEndpoint()
        ));
    }

    private void sendErrorMessage(ServerState newState, Endpoint endpoint) {
        dispatcher.sendSingle(
            new Message(
                ClientMessage.ERROR,
                new ErrorData(
                    ClientError.INVALID_STATUS_CHANGE,
                    "Invalid status-change from <" + state.toString() + "> to <" + newState.toString() + ">"
                )
            ),
            endpoint
        );
    }

    private void setHardwareShutdown(Endpoint endpoint) {
        switch(state) {
            case HALT:
            case ERROR:
            case EMERGENCY_STOP_IN_MANUAL_MODE:          // Nothalt (manueller Modus)
            case EMERGENCY_STOP_IN_AUTOMATIC_MODE:       // Nothalt (automatischer Modus)
            case EMERGENCY_STOP_IN_AUTOMATIC_HALT:
            case EMERGENCY_STOP_IN_AUTOMATIC_HALT_FOR_SHUTDOWN:
                sendErrorMessage(ServerState.AUTOMATIC_HALT_FOR_SHUTDOWN, endpoint);
                break;

            case MANUAL_MODE:
            case STANDBY_IN_MANUAL_MODE:
            case READY_FOR_AUTOMATIC_MODE:
                msgQueue.add(new Message(InternMessage.SET_SERVER_STATE, ServerState.READY_FOR_SHUTDOWN));
                //msgQueue.add(new Message(InternMessage.SERVER_SHUTDOWN, null));

            case AUTOMATIC_MODE:
            case STANDBY_IN_AUTOMATIC_MODE:
            case AUTOMATIC_HALT:
                msgQueue.add(new Message(InternMessage.SET_SERVER_STATE, ServerState.AUTOMATIC_HALT_FOR_SHUTDOWN));

            case AUTOMATIC_HALT_FOR_SHUTDOWN:
            case READY_FOR_SHUTDOWN:
                break;
        }
    }
}
