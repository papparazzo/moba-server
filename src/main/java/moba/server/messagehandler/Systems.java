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
    private ServerState currState = ServerState.HALT;
    private ServerState lastState = ServerState.HALT;

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
        currState = ServerState.HALT;
        lastState = ServerState.HALT;
    }

    @Override
    public void serverStateChanged(ServerState state) {
        if(currState.toSystemState() != state.toSystemState()) {
            dispatcher.sendGroup(new Message(SystemMessage.HARDWARE_STATE_CHANGED, state.toSystemState().toString()));
        }
        lastState = currState;
        currState = state;
    }

    @Override
    public void handleMsg(Message msg)
    throws ClientErrorException, SQLException {
        switch(SystemMessage.fromId(msg.getMessageId())) {
            case SET_AUTOMATIC_MODE:
                setAutomaticMode(msg);
                break;

            case READY_FOR_AUTOMATIC_MODE:
                setReadyForAutomaticMode(msg);
                break;

            case TRIGGER_EMERGENCY_STOP:
                triggerEmergencyStop(msg);
                break;

            case RELEASE_EMERGENCY_STOP:
                releaseEmergencyStop(msg.getEndpoint());
                break;

            case SET_STANDBY_MODE:
                setStandByMode(msg);
                break;

            case TOGGLE_STANDBY_MODE:
                toggleStandByMode(msg);
                break;

            case GET_HARDWARE_STATE:
                dispatcher.sendSingle(
                    new Message(SystemMessage.HARDWARE_STATE_CHANGED, currState.toSystemState().toString()),
                    msg.getEndpoint()
                );
                break;

            case HARDWARE_SHUTDOWN:
                setHardwareShutdown(msg.getEndpoint());
                break;

            case HARDWARE_RESET:
                msgQueue.add(new Message(InternMessage.SERVER_RESET, null));
                break;
        }
    }

    private void setAutomaticMode(Message msg)
    throws SQLException, ClientErrorException {
       if((boolean)msg.getData()) {
           setAutomaticModeOn(msg.getEndpoint());
       } else {
           setAutomaticModeOff(msg.getEndpoint());
       }
    }

    private void setAutomaticModeOn(Endpoint endpoint)
    throws SQLException, ClientErrorException {
        if(currState != ServerState.READY_FOR_AUTOMATIC_MODE && !checkPreConditions()) {
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
        if(currState != ServerState.AUTOMATIC_MODE) {
           sendErrorMessage(ServerState.AUTOMATIC_HALT, endpoint);
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

    private void setReadyForAutomaticMode(Message msg) {
       if((boolean)msg.getData()) {
           setReadyForAutomaticModeOn(msg.getEndpoint());
       } else {
           setReadyForAutomaticModeOff(msg.getEndpoint());
       }
    }

    private void setReadyForAutomaticModeOn(Endpoint endpoint) {
        if(currState != ServerState.MANUAL_MODE) {
           sendErrorMessage(ServerState.READY_FOR_AUTOMATIC_MODE, endpoint);
           return;
        }

        msgQueue.add(new Message(InternMessage.SET_SERVER_STATE, ServerState.READY_FOR_AUTOMATIC_MODE));
        incidentHandler.add(new IncidentData(
            IncidentLevel.NOTICE,
            IncidentType.STATUS_CHANGED,
            "bereit für Automatik",
            "Automatikmodus kann aktiviert werden...",
            "Systems.setReadyForAutomaticModeOn()",
            endpoint
        ));
    }

    private void setReadyForAutomaticModeOff(Endpoint endpoint) {
        if(currState != ServerState.READY_FOR_AUTOMATIC_MODE) {
           sendErrorMessage(ServerState.MANUAL_MODE, endpoint);
           return;
        }

        msgQueue.add(new Message(InternMessage.SET_SERVER_STATE, ServerState.MANUAL_MODE));
        incidentHandler.add(new IncidentData(
            IncidentLevel.NOTICE,
            IncidentType.STATUS_CHANGED,
            "Automatik nicht bereit",
            "Automatikmodus kann aktuell nicht aktiviert werden...",
            "Systems.setReadyForAutomaticModeOff()",
            endpoint
        ));
    }

    private void triggerEmergencyStop(Message msg)
    throws ClientErrorException {
        switch(currState) {
            case
                AUTOMATIC_MODE,
                AUTOMATIC_HALT_FOR_SHUTDOWN,
                AUTOMATIC_HALT,
                MANUAL_MODE,
                READY_FOR_AUTOMATIC_MODE:
                    setEmergencyStopReason(msg);
                    msgQueue.add(new Message(InternMessage.SET_SERVER_STATE, ServerState.INCIDENT));
                break;

            case INCIDENT:
            case STANDBY:
            case HALT:
            case READY_FOR_SHUTDOWN:
                sendErrorMessage(ServerState.INCIDENT, msg.getEndpoint());
        }
    }

    private void setEmergencyStopReason(Message msg)
    throws ClientErrorException {
        String reason = switch(CheckedEnum.getFromString(EmergencyTriggerReason.class, (String)msg.getData())) {
            case CENTRAL_STATION                 -> "Es wurde ein Nothalt durch die CentralStation ausgelöst";
            case EXTERN                          -> "Externe Hardware";
            case SELF_ACTING_BY_EXTERN_SWITCHING -> "Weichenstellung durch CS im Automatikmodus";
            case CONNECTION_LOST                 -> "Verbindung zur Hardware verloren";
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

    private void releaseEmergencyStop(Endpoint endpoint) {
        switch(currState) {
            case INCIDENT:
                msgQueue.add(new Message(InternMessage.SET_SERVER_STATE, lastState));
                break;

            case AUTOMATIC_MODE:
            case AUTOMATIC_HALT:
            case AUTOMATIC_HALT_FOR_SHUTDOWN:
            case MANUAL_MODE:
            case READY_FOR_AUTOMATIC_MODE:
            case STANDBY:
            case HALT:
            case READY_FOR_SHUTDOWN:
                sendErrorMessage(lastState, endpoint);
        }

        incidentHandler.add(new IncidentData(
            IncidentLevel.NOTICE,
            IncidentType.STATUS_CHANGED,
            "Nothalt Freigabe",
            "Der Nothalt wurde wieder freigegeben.",
            "Systems.releaseEmergencyStop()",
            endpoint
        ));
    }

    private void toggleStandByMode(Message msg) {
        if(currState == ServerState.STANDBY) {
            setStandByModeOff(msg.getEndpoint());
        } else {
             setStandByModeOn(msg.getEndpoint());
        }
    }

    private void setStandByMode(Message msg) {
        if((boolean)msg.getData()) {
            setStandByModeOn(msg.getEndpoint());
        } else {
            setStandByModeOff(msg.getEndpoint());
        }
    }

    private void setStandByModeOn(Endpoint endpoint) {
        switch(currState) {
            case INCIDENT:
            case AUTOMATIC_HALT:
            case AUTOMATIC_HALT_FOR_SHUTDOWN:
            case HALT:
            case READY_FOR_SHUTDOWN:
            case STANDBY:
                sendErrorMessage(ServerState.STANDBY, endpoint);
                return;
        }

        msgQueue.add(new Message(InternMessage.SET_SERVER_STATE, ServerState.STANDBY));
        incidentHandler.add(new IncidentData(
            IncidentLevel.NOTICE,
            IncidentType.STATUS_CHANGED,
        "Standby an",
            "Anlage wird in den Standby-Modus geschickt",
            "Systems.setStandByMode()",
            endpoint
        ));
    }

    private void setStandByModeOff(Endpoint endpoint) {
        if(currState != ServerState.STANDBY) {
            sendErrorMessage(lastState, endpoint);
            return;
        }

        msgQueue.add(new Message(InternMessage.SET_SERVER_STATE, lastState));
        incidentHandler.add(new IncidentData(
            IncidentLevel.NOTICE,
            IncidentType.STATUS_CHANGED,
            "Standby aus",
            "Die Anlage wird aus dem Standby-Modus geholt" +
            "Anlage wechselt in den " + (lastState == ServerState.MANUAL_MODE ? "Manuell modus" : "Automatikmodus"),
            "Systems.setStandByMode()",
            endpoint
        ));
    }

    private void sendErrorMessage(ServerState newState, Endpoint endpoint) {
        dispatcher.sendSingle(
            new Message(
                ClientMessage.ERROR,
                new ErrorData(
                    ClientError.INVALID_STATUS_CHANGE,
                    "Invalid status-change from <" + currState.toString() +
                        "> to <" + newState.toString() + ">"
                )
            ),
            endpoint
        );
    }

    private boolean checkPreConditions()
    throws ClientErrorException, SQLException {
        // TODO: Prüfen, ob alle Blöcke besetzt sind. Sprich: Block A belegt -> ist Block A laut DB belegt?
        //       Laut DB befindet ein Zug in Block A, ist Block a belegt

        lock.tryLock(0, activeLayout.getActiveLayout());
        return true;
    }

    private void setHardwareShutdown(Endpoint endpoint) {
        switch(currState) {
            case HALT:
            case INCIDENT:
                sendErrorMessage(ServerState.READY_FOR_SHUTDOWN, endpoint);
                break;

            case MANUAL_MODE:
            case READY_FOR_AUTOMATIC_MODE:
                msgQueue.add(new Message(InternMessage.SET_SERVER_STATE, ServerState.READY_FOR_SHUTDOWN));
                break;

            case AUTOMATIC_MODE:
            case AUTOMATIC_HALT:
                msgQueue.add(new Message(InternMessage.SET_SERVER_STATE, ServerState.AUTOMATIC_HALT_FOR_SHUTDOWN));
                break;

            case STANDBY:
                setStandByModeOff(endpoint);
                setHardwareShutdown(endpoint);
                break;

            case AUTOMATIC_HALT_FOR_SHUTDOWN:
            case READY_FOR_SHUTDOWN:
                break;
        }
    }
}
