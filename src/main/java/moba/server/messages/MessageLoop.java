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

package moba.server.messages;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import moba.server.com.Dispatcher;
import moba.server.com.Endpoint;
import moba.server.datatypes.enumerations.*;
import moba.server.datatypes.objects.EmergencyTriggerData;
import moba.server.datatypes.objects.ErrorData;
import moba.server.datatypes.objects.IncidentData;
import moba.server.messages.messagetypes.ClientMessage;
import moba.server.messages.messagetypes.InternMessage;
import moba.server.messages.messagetypes.ServerMessage;
import moba.server.exceptions.ClientErrorException;
import moba.server.utilities.logger.Loggable;
import moba.server.utilities.messaging.IncidentHandler;

final public class MessageLoop implements Loggable {

    private ServerState lastState = ServerState.HALT;
    private ServerState currState = ServerState.HALT;

    private final Map<Integer, AbstractMessageHandler> handlers   = new HashMap<>();
    private final Dispatcher                    dispatcher;
    private final IncidentHandler               incidentHandler;

    public MessageLoop(Dispatcher dispatcher, IncidentHandler incidentHandler) {
        this.dispatcher      = dispatcher;
        this.incidentHandler = incidentHandler;
    }

    public void addHandler(AbstractMessageHandler msgHandler) {
        handlers.put(msgHandler.getGroupId(), msgHandler);
    }

    public boolean loop(MessageQueue in)
    throws InterruptedException {
        while(true) {
            Message msg = in.take();
            try {
                if(msg.getGroupId() == InternMessage.GROUP_ID) {
                    switch(InternMessage.fromId(msg.getMessageId())) {
                        case SERVER_RESET -> {
                            in.clear();
                            handleServerReset();
                            return true;
                        }

                        case SERVER_SHUTDOWN -> {
                            in.clear();
                            handleServerShutdown();
                            return false;
                        }

                        case SYSTEM_SHUTDOWN -> {
                            setSystemShutdown(msg.getEndpoint());
                            return true;
                        }

                        case EMERGENCY_STOP -> {
                            setEmergencyStop(EmergencyTriggerData.fromMessage(msg), msg.getEndpoint());
                            continue;
                        }

                        case REMOVE_CLIENT -> {
                            handleRemoveClient(msg);
                            continue;
                        }

                        case SET_SERVER_STATE -> {
                            setSystemState((ServerState)msg.getData(), msg.getEndpoint());
                            continue;
                        }

                        case RELEASE_INCIDENT_MODE -> {
                            releaseIncidentMode(msg.getEndpoint());
                            continue;
                        }

                        case RELEASE_STANDBY_MODE -> {
                            setStandByModeOff(msg.getEndpoint());
                            continue;
                        }
                    }
                }

                checkGroup(msg.getGroupId());
                handlers.get(msg.getGroupId()).handleMsg(msg);
            } catch(ClientErrorException e) {
                ClientError id = e.getErrorId();
                Endpoint ep = msg.getEndpoint();
                dispatcher.sendSingle(new Message(ClientMessage.ERROR, new ErrorData(id, e.getMessage())), ep);
                incidentHandler.add(new IncidentData(IncidentType.CLIENT_ERROR, e, ep));
            } catch(Throwable e) {
                incidentHandler.add(new IncidentData(IncidentType.EXCEPTION, e, msg.getEndpoint()));
                incidentHandler.add(new IncidentData(
                    IncidentLevel.CRITICAL,
                    IncidentType.SERVER_NOTICE,
                    "Restart of the server (reset)",
                    "Restart of the server application due to an error",
                    "MessageLoop.loop()")
                );
                in.clear();
                return true;
            }
        }
    }

    private void checkGroup(int groupId)
    throws ClientErrorException {
        if(!handlers.containsKey(groupId)) {
            throw new ClientErrorException(ClientError.UNKNOWN_GROUP_ID, "no handler for group <" + groupId + ">!");
        }
    }

    private void freeResources(long appId)
    throws SQLException {
        for(Integer key: handlers.keySet()) {
            handlers.get(key).freeResources(appId);
        }
    }

    private void handleServerReset()
    throws Exception {
        incidentHandler.add(new IncidentData(
            IncidentLevel.NOTICE,
            IncidentType.SERVER_NOTICE,
            "Server Neustart (reset)",
            "Neustart der Serverapplikation aufgrund eines Server-Resets",
            "ServerApplication.handleServerReset()"
        ));
        dispatcher.sendAll(new Message(ClientMessage.RESET, true));
        for(Integer key: handlers.keySet()) {
            handlers.get(key).reset();
        }
    }

    private void handleServerShutdown()
    throws Exception {
        incidentHandler.add(new IncidentData(
            IncidentLevel.NOTICE,
            IncidentType.SERVER_NOTICE,
            "Server reset / shutdown (System beenden)",
            "Shutdown der Serverapplikation",
            "ServerApplication.handleServerShutdown()"
        ));

        // TODO: Anderer Text: "Neustart der Serverapplikation aufgrund eines Server-Resets",

        dispatcher.sendAll(new Message(ClientMessage.SHUTDOWN, null));
        for(Integer key: handlers.keySet()) {
            handlers.get(key).reset();
        }
    }

    private void setSystemShutdown(Endpoint endpoint)
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

    private void setEmergencyStop(EmergencyTriggerData triggerData, Endpoint endpoint)
    throws Exception {
        switch(currState) {
            case
                AUTOMATIC_MODE,
                AUTOMATIC_HALT_FOR_SHUTDOWN,
                AUTOMATIC_HALT,
                MANUAL_MODE,
                READY_FOR_AUTOMATIC_MODE:
                setEmergencyStopReason(triggerData, endpoint);
                setNewServerState(ServerState.INCIDENT);
                break;

            case INCIDENT:
            case STANDBY:
            case HALT:
            case READY_FOR_SHUTDOWN:
                sendErrorMessage(ServerState.INCIDENT, endpoint);
        }
    }

    private void setEmergencyStopReason(EmergencyTriggerData triggerData, Endpoint endpoint) {
        String message = triggerData.message();

        if(!message.isEmpty()) {
            message = " (" + message + ")";
        }

        String reason = switch(triggerData.reason()) {
            case CENTRAL_STATION                 -> "Nothalt durch CentralStation ausgelöst.";
            case EXTERN                          -> "Externes Ereignis (z.B. Notausschalter).";
            case SELF_ACTING_BY_EXTERN_SWITCHING -> "Manuelle Weichenstellung im Automatikmodus.";
            case CONNECTION_LOST                 -> "Verbindung zur Hardware verloren.";
            case SOFTWARE_ERROR                  -> "Automatisch durch Softwarefehler.";
            case SOFTWARE_MANUAL                 -> "Manuell durch die Software (Notausbutton).";
        };

        incidentHandler.add(new IncidentData(
            IncidentLevel.WARNING,
            IncidentType.STATUS_CHANGED,
            "Nothalt ausgelöst",
            "Auslösegrund: " + reason + message,
            "Systems.setEmergencyStopReason()",
            endpoint
        ));
    }

    private void handleRemoveClient(Message msg)
    throws Exception {
        long appId = msg.getEndpoint().getAppId();

        IncidentData incidentData = (IncidentData)msg.getData();

        if(incidentData.getLevel() == IncidentLevel.CRITICAL) {
            setEmergencyStop(
                new EmergencyTriggerData(
                    EmergencyTriggerReason.SOFTWARE_ERROR,
                    incidentData.getMessage()
                ),
                msg.getEndpoint()
            );
        }

        incidentHandler.add(incidentData);

        freeResources(appId);
        dispatcher.removeEndpoint(msg.getEndpoint());
        dispatcher.sendGroup(new Message(ServerMessage.CLIENT_CLOSED, appId));
    }

    private void setSystemState(ServerState state, Endpoint endpoint)
    throws Exception {
        switch(state) {
            case HALT:
                break;

            case CONNECTION_LOST:
                setNewServerState(ServerState.CONNECTION_LOST);
                break;

            case READY_FOR_AUTOMATIC_MODE:
                setReadyForAutomaticModeOn(endpoint);
                break;

            case AUTOMATIC_MODE:
                activateAutomaticMode(endpoint);
                break;

            case STANDBY:
                setStandByModeOn(endpoint);
                break;
                
            case AUTOMATIC_HALT:
                deactivateAutomaticMode(endpoint);
                break;

            case MANUAL_MODE:
            case AUTOMATIC_HALT_FOR_SHUTDOWN:
            case READY_FOR_SHUTDOWN:
                setNewServerState(state);
                break;

            case INCIDENT:
                // TODO: throw Exception
                break;
        }
    }

    private void setNewServerState(ServerState state)
    throws Exception {
        if(currState == state) {
            return;
        }

        getLogger().info("Server state changed from <" + currState + "> to <" + state + ">");

        lastState = currState;
        currState = state;

        for(Integer key: handlers.keySet()) {
            handlers.get(key).serverStateChanged(state);
        }

        if(state == ServerState.READY_FOR_SHUTDOWN) {
            handleServerShutdown();
        }
    }

    private void setStandByModeOn(Endpoint endpoint)
    throws Exception {
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
        
        incidentHandler.add(new IncidentData(
            IncidentLevel.NOTICE,
            IncidentType.STATUS_CHANGED,
        "Standby an",
            "Anlage wird in den Standby-Modus geschickt",
            "Systems.setStandByMode()",
            endpoint
        ));
    }

    private void releaseIncidentMode(Endpoint endpoint)
    throws Exception {
        switch(currState) {
            case INCIDENT:
                setNewServerState(lastState);
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
            "Systems.releaseIncidentMode()",
            endpoint
        ));
    }

    private void setStandByModeOff(Endpoint endpoint)
    throws Exception {
        if(currState != ServerState.STANDBY) {
            sendErrorMessage(lastState, endpoint);
            return;
        }

        setNewServerState(lastState);

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

    private void activateAutomaticMode(Endpoint endpoint)
    throws Exception {
        if(currState != ServerState.READY_FOR_AUTOMATIC_MODE && !checkPreConditions()) {
           sendErrorMessage(ServerState.AUTOMATIC_MODE, endpoint);
           return;
        }

        setNewServerState(ServerState.AUTOMATIC_MODE);
        incidentHandler.add(new IncidentData(
            IncidentLevel.NOTICE,
            IncidentType.STATUS_CHANGED,
            "Automatik an",
            "Die Hardware befindet sich im Automatikmodus",
            "Systems.setAutomaticMode()",
            endpoint
        ));
    }

    private void deactivateAutomaticMode(Endpoint endpoint)
    throws Exception {
        if(currState != ServerState.AUTOMATIC_MODE) {
           sendErrorMessage(ServerState.AUTOMATIC_HALT, endpoint);
           return;
        }

        setNewServerState(ServerState.AUTOMATIC_HALT);
        incidentHandler.add(new IncidentData(
            IncidentLevel.NOTICE,
            IncidentType.STATUS_CHANGED,
            "Automatik anhalten",
            "Automatikmodus wird deaktiviert...",
            "Systems.setAutomaticMode()",
            endpoint
        ));
    }

    private void setReadyForAutomaticModeOn(Endpoint endpoint)
    throws Exception {
        if(currState != ServerState.MANUAL_MODE) {
           sendErrorMessage(ServerState.READY_FOR_AUTOMATIC_MODE, endpoint);
           return;
        }

        setNewServerState(ServerState.READY_FOR_AUTOMATIC_MODE);

        incidentHandler.add(new IncidentData(
            IncidentLevel.NOTICE,
            IncidentType.STATUS_CHANGED,
            "bereit für Automatik",
            "Automatikmodus kann aktiviert werden...",
            "Systems.setReadyForAutomaticModeOn()",
            endpoint
        ));
    }

    /*
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
    */

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
}
