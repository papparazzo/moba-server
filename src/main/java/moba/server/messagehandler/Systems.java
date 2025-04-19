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
import moba.server.datatypes.enumerations.ClientError;
import moba.server.datatypes.enumerations.EmergencyTriggerReason;
import moba.server.datatypes.enumerations.HardwareState;
import moba.server.utilities.CheckedEnum;
import moba.server.datatypes.objects.ErrorData;
import moba.server.datatypes.objects.IncidentData;
import moba.server.datatypes.objects.helper.ActiveLayout;
import moba.server.messages.Message;
import moba.server.messages.MessageHandlerA;
import moba.server.messages.MessageQueue;
import moba.server.messages.messageType.ClientMessage;
import moba.server.messages.messageType.InternMessage;
import moba.server.messages.messageType.SystemMessage;
import moba.server.utilities.exceptions.ClientErrorException;
import moba.server.utilities.lock.TrackLayoutLock;
import moba.server.utilities.messaging.IncidentHandler;

import java.sql.SQLException;

final public class Systems extends MessageHandlerA {
    private HardwareState status = HardwareState.ERROR;
    private boolean automaticMode = false;

    private final MessageQueue msgQueue;
    private final ActiveLayout activeLayout;
    private final TrackLayoutLock lock;
    private final IncidentHandler incidentHandler;

    public Systems(
        Dispatcher dispatcher,
        TrackLayoutLock lock,
        ActiveLayout activeLayout,
        MessageQueue msgQueue,
        IncidentHandler incidentHandler
    ) {
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
    public void shutdown() {
        lock.resetAll();
    }

    @Override
    public void handleMsg(Message msg)
    throws ClientErrorException, SQLException {
        switch(SystemMessage.fromId(msg.getMessageId())) {
            case SET_AUTOMATIC_MODE:
                setAutomaticMode(msg);
                break;

            case TOGGLE_AUTOMATIC_MODE:
                setAutomaticMode(msg, true);
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
                dispatcher.send(new Message(SystemMessage.HARDWARE_STATE_CHANGED, status.toString()), msg.getEndpoint());
                break;

            case HARDWARE_SHUTDOWN:
                msgQueue.add(new Message(InternMessage.SERVER_SHUTDOWN, null));
                break;

            case HARDWARE_RESET:
                msgQueue.add(new Message(InternMessage.SERVER_RESET, null));
                break;
        }
    }

    private void setAutomaticMode(Message msg)
    throws ClientErrorException, SQLException {
        setAutomaticMode(msg, false);
    }

    private void setAutomaticMode(Message msg, boolean toggle)
    throws ClientErrorException, SQLException {
        if(status == HardwareState.ERROR || status == HardwareState.EMERGENCY_STOP || status == HardwareState.STANDBY) {
            sendErrorMessage(msg.getEndpoint());
            return;
        }

        if(toggle) {
            if(!automaticMode) {
                lock.tryLock(0, activeLayout.getActiveLayout());
            }
            automaticMode = !automaticMode;
        } else {
            boolean setAutomaticMode = (boolean)msg.getData();
            if(setAutomaticMode && status == HardwareState.AUTOMATIC) {
                sendErrorMessage(msg.getEndpoint());
                return;
            }
            if(!setAutomaticMode) {
                lock.tryLock(0, activeLayout.getActiveLayout());
            }
            automaticMode = setAutomaticMode;
        }

        if(automaticMode) {
            msgQueue.add(new Message(InternMessage.SET_HARDWARE_STATE, HardwareState.AUTOMATIC));
            incidentHandler.add(new IncidentData(
                IncidentData.Level.NOTICE,
                IncidentData.Type.STATUS_CHANGE,
                "Automatik an",
                "Die Hardware befindet sich im Automatikmodus",
                msg.getEndpoint()
            ));
            return;
        }

        if(status != HardwareState.AUTOMATIC) {
            sendErrorMessage(msg.getEndpoint());
            return;
        }

        lock.resetOwn(0);

        msgQueue.add(new Message(InternMessage.SET_HARDWARE_STATE, HardwareState.MANUEL));
        incidentHandler.add(new IncidentData(
            IncidentData.Level.NOTICE,
            IncidentData.Type.STATUS_CHANGE,
            "Automatik aus",
            "Automatikmodus wurde deaktiviert",
            msg.getEndpoint()
        ));
    }

    private void triggerEmergencyStop(Message msg)
    throws ClientErrorException {
        if(status == HardwareState.ERROR || status == HardwareState.STANDBY) {
            sendErrorMessage(msg.getEndpoint());
            return;
        }

        if(status == HardwareState.EMERGENCY_STOP) {
            sendErrorMessage(msg.getEndpoint());
            return;
        }
        msgQueue.add(new Message(InternMessage.SET_HARDWARE_STATE, HardwareState.EMERGENCY_STOP));
        setEmergencyStopReason(msg);
    }

    private void setEmergencyStopReason(Message msg)
    throws ClientErrorException {
        String reason = switch(CheckedEnum.getFromString(EmergencyTriggerReason.class, (String)msg.getData())) {
            case CENTRAL_STATION                 -> "Es wurde ein Nothalt durch die CentralStation ausgelöst";
            case EXTERN                          -> "Externe Hardware";
            case SELF_ACTING_BY_EXTERN_SWITCHING -> "Weichenstellung durch CS im Automatikmodus";
            case SOFTWARE_MANUAL                 -> "Manuell durch Steuerungssoftware";
        };

        incidentHandler.add(new IncidentData(
            IncidentData.Level.WARNING,
            IncidentData.Type.STATUS_CHANGE,
            "Nothalt gedrückt",
            "Auslösegrund: " + reason,
            msg.getEndpoint()
        ));
    }

    private void releaseEmergencyStop(Message msg) {
        if(status == HardwareState.ERROR || status == HardwareState.STANDBY) {
            sendErrorMessage(msg.getEndpoint());
            return;
        }

        if(status != HardwareState.EMERGENCY_STOP) {
            sendErrorMessage(msg.getEndpoint());
            return;
        }

        if(automaticMode) {
            msgQueue.add(new Message(InternMessage.SET_HARDWARE_STATE, HardwareState.AUTOMATIC));
        } else {
            msgQueue.add(new Message(InternMessage.SET_HARDWARE_STATE, HardwareState.MANUEL));
        }
        incidentHandler.add(new IncidentData(
            IncidentData.Level.NOTICE,
            IncidentData.Type.STATUS_CHANGE,
            "Nothaltfreigabe",
            "Der Nothalt wurde wieder freigegeben. " +
            "Anlage wechselt in den " + (automaticMode ? "Automatikmodus" : "Manuellmodus"),
            msg.getEndpoint()
        ));
    }

    private void setStandByMode(Message msg) {
        setStandByMode(msg, false);
    }

    private void setStandByMode(Message msg, boolean toggle) {
        if(status == HardwareState.ERROR || status == HardwareState.EMERGENCY_STOP) {
            sendErrorMessage(msg.getEndpoint());
            return;
        }

        boolean setStandByMode;

        if(toggle) {
            setStandByMode = (status != HardwareState.STANDBY);
        } else {
            setStandByMode = (boolean)msg.getData();

            if(setStandByMode && status == HardwareState.STANDBY) {
                sendErrorMessage(msg.getEndpoint());
                return;
            }
        }

        if(setStandByMode) {
            msgQueue.add(new Message(InternMessage.SET_HARDWARE_STATE, HardwareState.STANDBY));
            incidentHandler.add(new IncidentData(
                IncidentData.Level.NOTICE,
                IncidentData.Type.STATUS_CHANGE,
                "Standby an",
                "Anlage wird in den Standby-Modus geschickt",
                msg.getEndpoint()
            ));
            return;
        }

        if(status != HardwareState.STANDBY) {
            sendErrorMessage(msg.getEndpoint());
            return;
        }

        if(automaticMode) {
            msgQueue.add(new Message(InternMessage.SET_HARDWARE_STATE, HardwareState.AUTOMATIC));
        } else {
            msgQueue.add(new Message(InternMessage.SET_HARDWARE_STATE, HardwareState.MANUEL));
        }

        incidentHandler.add(new IncidentData(
            IncidentData.Level.NOTICE,
            IncidentData.Type.STATUS_CHANGE,
            "Standby aus",
            "Die Anlage wird aus dem Standby-Modus geholt" +
            "Anlage wechselt in den " + (automaticMode ? "Automatikmodus" : "Manuellmodus"),
            msg.getEndpoint()
        ));
    }

    private void sendErrorMessage(Endpoint endpoint) {
        dispatcher.send(
            new Message(
                ClientMessage.ERROR,
                new ErrorData(
                    ClientError.INVALID_STATUS_CHANGE,
                    "Current state is <" + status.toString() + ">"
                )
            ),
            endpoint
        );
    }

    @Override
    public void hardwareStateChanged(HardwareState state) {
        status = state;
        dispatcher.broadcast(new Message(SystemMessage.HARDWARE_STATE_CHANGED, status.toString()));
    }
}
