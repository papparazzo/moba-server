/*
 *  Project:    moba-server
 *
 *  Copyright (C) 2025 Stefan Paproth <pappi-@gmx.de>
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

package moba.server.application;

import moba.server.com.Dispatcher;
import moba.server.com.Endpoint;
import moba.server.datatypes.enumerations.*;
import moba.server.datatypes.objects.EmergencyTriggerData;
import moba.server.datatypes.objects.ErrorData;
import moba.server.datatypes.objects.NotificationData;
import moba.server.messages.AbstractMessageHandler;
import moba.server.messages.Message;
import moba.server.messages.messagetypes.ClientMessage;
import moba.server.messages.messagetypes.ServerMessage;
import moba.server.messages.messagetypes.SystemMessage;
import moba.server.utilities.logger.Loggable;
import moba.server.utilities.messaging.NotificationHandler;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ServerStateMachine implements Loggable {
    private ServerState lastState = ServerState.HALT;
    private ServerState currState = ServerState.HALT;

    private final List<AbstractMessageHandler> handlers = new ArrayList<>();

    private final Dispatcher          dispatcher;
    private final NotificationHandler notificationHandler;

    public ServerStateMachine(Dispatcher dispatcher, NotificationHandler notificationHandler) {
        this.dispatcher          = dispatcher;
        this.notificationHandler = notificationHandler;
    }

    public void addHandler(AbstractMessageHandler msgHandler) {
        handlers.add(msgHandler);
    }

    public ServerState getState() {
        return currState;
    }

    public void activateIncident(EmergencyTriggerData triggerData, Endpoint endpoint)
    throws SQLException {
        switch(currState) {
            case INCIDENT:
            case STANDBY:
            case HALT:
            case READY_FOR_SHUTDOWN:
                sendErrorMessage(ServerState.INCIDENT, endpoint);
                return;
        }
        setNewServerState(ServerState.INCIDENT);

        String message = triggerData.message();

        if(!message.isEmpty()) {
            message = " (" + message + ")";
        }

        String reason = switch(triggerData.reason()) {
            case CENTRAL_STATION                 -> "Nothalt durch CentralStation ausgelöst.";
            case EXTERN                          -> "Externes Ereignis (z.B. Notausschalter).";
            case SELF_ACTING_BY_EXTERN_SWITCHING -> "Manuelle Weichenstellung im Automatikmodus.";
            case RECONNECTED                     -> "Verbindung zur Hardware wiederhergestellt.";
            case CONNECTION_LOST                 -> "Verbindung zur Hardware verloren.";
            case SOFTWARE_ERROR                  -> "Automatisch durch Softwarefehler.";
            case SOFTWARE_MANUAL                 -> "Manuell durch die Software (Notausbutton).";
        };

        notificationHandler.add(new NotificationData(
            NotificationLevel.CRITICAL,
            NotificationType.STATUS_CHANGED,
            "Nothalt ausgelöst",
            "Auslösegrund: " + reason + message,
            "Systems.setEmergencyStopReason()",
            endpoint
        ));

    }
    
    public void releaseIncident(Endpoint ep)
    throws SQLException {
        switch(currState) {
            case AUTOMATIC_MODE:
            case AUTOMATIC_HALT:
            case AUTOMATIC_HALT_FOR_SHUTDOWN:
            case MANUAL_MODE:
            case READY_FOR_AUTOMATIC_MODE:
            case STANDBY:
            case HALT:
            case READY_FOR_SHUTDOWN:
                sendErrorMessage(lastState, ep);
                return;
        }
        setNewServerState(lastState);
        addStatusChangedNotification("Nothalt Freigabe", "Der Nothalt wurde wieder freigegeben.", ep);
    }

    public void activateAutomaticMode(Endpoint ep)
    throws SQLException {
        if(currState != ServerState.READY_FOR_AUTOMATIC_MODE && !checkPreConditions()) {
           sendErrorMessage(ServerState.AUTOMATIC_MODE, ep);
           return;
        }
        setNewServerState(ServerState.AUTOMATIC_MODE);
        addStatusChangedNotification("Automatik an", "Die Hardware befindet sich im Automatikmodus", ep);
    }

    public void deactivateAutomaticMode(Endpoint endpoint)
    throws SQLException {
        if(currState != ServerState.AUTOMATIC_MODE) {
           sendErrorMessage(ServerState.AUTOMATIC_HALT, endpoint);
           return;
        }
        setNewServerState(ServerState.AUTOMATIC_HALT);
        addStatusChangedNotification("Automatik anhalten", "Automatikmodus wird deaktiviert...", endpoint);
    }

    public void automaticModeFinished(Endpoint endpoint)
    throws SQLException {
        if(currState == ServerState.AUTOMATIC_HALT) {
            setReadyForAutomaticMode(endpoint);
            return;
        }

        if(currState == ServerState.AUTOMATIC_HALT_FOR_SHUTDOWN) {
            handleServerShutdown();
        }

       sendErrorMessage(ServerState.AUTOMATIC_MODE, endpoint);
    }

    public void activateStandby(Endpoint endpoint)
    throws SQLException {
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
        setNewServerState(ServerState.STANDBY);
        addStatusChangedNotification("Standby an", "Anlage wird in den Standby-Modus geschickt", endpoint);
    }

    public void deactivateStandby(Endpoint endpoint)
    throws SQLException {
        if(currState != ServerState.STANDBY) {
            sendErrorMessage(lastState, endpoint);
            return;
        }

        String message = switch(lastState) {
            case MANUAL_MODE -> "den manuellen Zustand";
            case AUTOMATIC_MODE -> "den Automatikmodus";
            case READY_FOR_AUTOMATIC_MODE -> "bereit für Automatikmodus";
            default -> "unbekannt";
        };

        setNewServerState(lastState);
        addStatusChangedNotification(
            "Standby aus",
            "Die Anlage wird aus dem Standby-Modus geholt und wechselt in " + message + ".",
            endpoint
        );
    }

    public void setReadyForAutomaticMode(Endpoint endpoint)
    throws SQLException {
        if(currState != ServerState.MANUAL_MODE && currState != ServerState.AUTOMATIC_HALT) {
           sendErrorMessage(ServerState.READY_FOR_AUTOMATIC_MODE, endpoint);
           return;
        }
        setNewServerState(ServerState.READY_FOR_AUTOMATIC_MODE);
        addStatusChangedNotification(
            "manueller Modus (bereit für Automatik)",
            "Automatikmodus kann aktiviert werden...",
            endpoint
        );
    }

    public void setManualMode(Endpoint endpoint)
    throws SQLException {
        if(currState == ServerState.MANUAL_MODE) {
           sendErrorMessage(ServerState.MANUAL_MODE, endpoint);
           return;
        }
        setNewServerState(ServerState.MANUAL_MODE);
        addStatusChangedNotification(
            "manueller Modus",
            "Anlage befindet sich im manuellen Modus",
            endpoint
        );
    }
    
    public void setConnected(Endpoint endpoint, boolean onInitialize)
    throws SQLException {

        if(currState == ServerState.HALT && onInitialize) {
            // TODO: Check, if ready for automatic-mode
            setNewServerState(ServerState.MANUAL_MODE);
            addStatusChangedNotification(
                "manueller Modus",
                "Die Verbindung zur Hardware wurde hergestellt",
                endpoint
            );
            return;
        }
        if(onInitialize) {
            activateIncident(
                new EmergencyTriggerData(
                    EmergencyTriggerReason.SOFTWARE_ERROR,
                    "Verbindung mit initialize == true"
                ),
                endpoint
            );
            lastState = ServerState.MANUAL_MODE;
            return;
        }
        activateIncident(
            new EmergencyTriggerData(
                EmergencyTriggerReason.RECONNECTED,
                "temporäre Unterbrechung"
            ),
            endpoint
        );
        lastState = ServerState.MANUAL_MODE;
    }

    public void setConnectionLost(Endpoint endpoint)
    throws SQLException {
        if(currState == ServerState.CONNECTION_LOST) {
           sendErrorMessage(ServerState.CONNECTION_LOST, endpoint);
           return;
        }

        setNewServerState(ServerState.CONNECTION_LOST);

        notificationHandler.add(new NotificationData(
            NotificationLevel.NOTICE,
            NotificationType.STATUS_CHANGED,
            "Hardwareverbindung",
            "Die Verbindung zur Hardware wurde unterbrochen",
            "ServerStateMachine.setConnectionLost()",
            endpoint
        ));
    }
    
    public void setSystemShutdown(Endpoint endpoint)
    throws Exception {
        switch(currState) {
            case HALT:
            case INCIDENT:
                sendErrorMessage(ServerState.READY_FOR_SHUTDOWN, endpoint);
                break;

            case MANUAL_MODE:
            case READY_FOR_AUTOMATIC_MODE:
                setNewServerState(ServerState.READY_FOR_SHUTDOWN);
                break;

            case AUTOMATIC_MODE:
            case AUTOMATIC_HALT:
                setNewServerState(ServerState.AUTOMATIC_HALT_FOR_SHUTDOWN);
                break;

            case STANDBY:
                setNewServerState(lastState);
                setSystemShutdown(endpoint);
                break;

            case AUTOMATIC_HALT_FOR_SHUTDOWN:
            case READY_FOR_SHUTDOWN:
                break;
        }
    }

    public void handleServerReset()
    throws Exception {
        notificationHandler.add(new NotificationData(
            NotificationLevel.NOTICE,
            NotificationType.SERVER_NOTICE,
            "Server Neustart (reset)",
            "Neustart der Serverapplikation aufgrund eines Server-Resets",
            "ServerApplication.handleServerReset()"
        ));
        currState = ServerState.HALT;
        lastState = ServerState.HALT;

        dispatcher.sendAll(new Message(ClientMessage.RESET, true));
        for(AbstractMessageHandler handler: handlers) {
            handler.reset();
        }
    }

    public void handleServerShutdown()
    throws SQLException {
        notificationHandler.add(new NotificationData(
            NotificationLevel.NOTICE,
            NotificationType.SERVER_NOTICE,
            "Server reset / shutdown (System beenden)",
            "Shutdown der Serverapplikation",
            "ServerApplication.handleServerShutdown()"
        ));

        currState = ServerState.HALT;
        lastState = ServerState.HALT;

        dispatcher.sendAll(new Message(ClientMessage.SHUTDOWN));
        for(AbstractMessageHandler handler: handlers) {
            handler.reset();
        }
    }

    public void handleRemoveClient(Message msg)
    throws Exception {
        long appId = msg.getEndpoint().getAppId();

        NotificationData notificationData = (NotificationData)msg.getData();

        if(notificationData.getLevel() == NotificationLevel.CRITICAL) {
            activateIncident(
                new EmergencyTriggerData(
                    EmergencyTriggerReason.SOFTWARE_ERROR,
                    notificationData.getMessage()
                ),
                msg.getEndpoint()
            );
        }

        notificationHandler.add(notificationData);

        for(AbstractMessageHandler handler: handlers) {
            handler.freeResources(appId);
        }

        dispatcher.removeEndpoint(msg.getEndpoint());
        dispatcher.sendGroup(new Message(ServerMessage.CLIENT_CLOSED, appId));
    }

    private void setNewServerState(ServerState state)
    throws SQLException {
        if(currState == state) {
            return;
        }

        dispatcher.sendGroup(new Message(SystemMessage.HARDWARE_STATE_CHANGED, state.toSystemState().toString()));
        
        getLogger().info("Server state changed from <" + currState + "> to <" + state + ">");

        lastState = currState;
        currState = state;

        for(AbstractMessageHandler handler: handlers) {
            handler.serverStateChanged(state);
        }

        if(state == ServerState.READY_FOR_SHUTDOWN) {
            handleServerShutdown();
        }
    }

    private boolean checkPreConditions() {
        // TODO: Prüfen, ob alle Blöcke besetzt sind. Sprich: Block A belegt -> ist Block A laut DB belegt?
        //       Laut DB befindet ein Zug in Block A, ist Block a belegt
        return true;
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

    private void addStatusChangedNotification(String caption, String message, Endpoint ep) {
        notificationHandler.add(new NotificationData(
            NotificationLevel.NOTICE,
            NotificationType.STATUS_CHANGED,
            caption,
            message,
            "ServerStateMachine.addStatusChangedNotification()",
            ep
        ));
    }
}
