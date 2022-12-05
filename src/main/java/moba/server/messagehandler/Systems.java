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
 *  along with this program. If not, see <http://www.gnu.org/licenses/agpl.txt>.
 *
 */

package moba.server.messagehandler;

import moba.server.com.Dispatcher;
import moba.server.com.Endpoint;
import moba.server.datatypes.enumerations.EmergencyTriggerReason;
import moba.server.datatypes.enumerations.ErrorId;
import moba.server.datatypes.enumerations.HardwareState;
import moba.server.datatypes.enumerations.NoticeType;
import moba.server.datatypes.objects.ErrorData;
import moba.server.datatypes.objects.NoticeData;
import moba.server.messages.Message;
import moba.server.messages.MessageHandlerA;
import moba.server.messages.MessageQueue;
import moba.server.messages.messageType.ClientMessage;
import moba.server.messages.messageType.GuiMessage;
import moba.server.messages.messageType.InternMessage;
import moba.server.messages.messageType.SystemMessage;
import moba.server.utilities.exceptions.ErrorException;

public class Systems extends MessageHandlerA {
    protected HardwareState status = HardwareState.ERROR;
    protected MessageQueue msgQueue = null;

    protected boolean automaticMode = false;

    public Systems(Dispatcher dispatcher, MessageQueue msgQueue) {
        this.dispatcher = dispatcher;
        this.msgQueue   = msgQueue;
    }

    @Override
    public int getGroupId() {
        return SystemMessage.GROUP_ID;
    }

    @Override
    public void handleMsg(Message msg)
    throws ErrorException {
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
                dispatcher.dispatch(new Message(SystemMessage.HARDWARE_STATE_CHANGED, status.toString()), msg.getEndpoint());
                break;

            case HARDWARE_SHUTDOWN:
                msgQueue.add(new Message(InternMessage.SERVER_SHUTDOWN, null));
                break;

            case HARDWARE_RESET:
                msgQueue.add(new Message(InternMessage.SERVER_RESET, null));
                break;

            default:
                throw new ErrorException(ErrorId.UNKNOWN_MESSAGE_ID, "unknow msg <" + Long.toString(msg.getMessageId()) + ">.");
        }
    }

    protected void setAutomaticMode(Message msg) {
        setAutomaticMode(msg, false);
    }

    protected void setAutomaticMode(Message msg, boolean toggle) {
        if(status == HardwareState.ERROR || status == HardwareState.EMERGENCY_STOP || status == HardwareState.STANDBY) {
            sendErrorMessage(msg.getEndpoint());
            return;
        }

        if(toggle) {
            automaticMode = !automaticMode;
        } else {
            var setAutomaticMode = (boolean)msg.getData();
            if(setAutomaticMode && status == HardwareState.AUTOMATIC) {
                sendErrorMessage(msg.getEndpoint());
                return;
            }
            automaticMode = setAutomaticMode;
        }

        if(automaticMode) {
            msgQueue.add(new Message(InternMessage.SET_HARDWARE_STATE, HardwareState.AUTOMATIC));
            dispatcher.dispatch(
                new Message(GuiMessage.SYSTEM_NOTICE, new NoticeData(NoticeType.INFO, "Automatik", "Die Hardware befindet sich im Automatikmodus"))
            );
            return;
        }

        if(status != HardwareState.AUTOMATIC) {
            sendErrorMessage(msg.getEndpoint());
            return;
        }

        msgQueue.add(new Message(InternMessage.SET_HARDWARE_STATE, HardwareState.MANUEL));
        dispatcher.dispatch(new Message(GuiMessage.SYSTEM_NOTICE, new NoticeData(NoticeType.INFO, "Automatik", "Automatikmodus wurde deaktiviert")));
    }

    protected void triggerEmergencyStop(Message msg) {
        if(status == HardwareState.ERROR || status == HardwareState.STANDBY) {
            sendErrorMessage(msg.getEndpoint());
            return;
        }

        if(status == HardwareState.EMERGENCY_STOP) {
            sendErrorMessage(msg.getEndpoint());
            return;
        }

        dispatcher.dispatch(
            new Message(GuiMessage.SYSTEM_NOTICE, new NoticeData(NoticeType.WARNING, "Nothalt gedrückt", getEmergencyStopReason((String)msg.getData())))
        );
        msgQueue.add(new Message(InternMessage.SET_HARDWARE_STATE, HardwareState.EMERGENCY_STOP));
    }

    protected String getEmergencyStopReason(String reason) {

        switch(EmergencyTriggerReason.valueOf(reason)) {
            case CENTRAL_STATION:
                return "Auslösegrund: Es wurde ein Nothalt durch die CentralStation ausgelöst";

            case EXTERN:
                return "Auslösegrund: Externe Hardware";

            case SELF_ACTING_BY_EXTERN_SWITCHING:
                return "Auslösegrund: Weichenstellung durch CS im Automatikmodus";

            case SOFTWARE_MANUELL:
                return "Auslösegrund: Manuell durch Steuerungssoftware";

            default:
                return "unbekannter Auslösegrund";
        }
    }

    protected void releaseEmergencyStop(Message msg) {
        if(status == HardwareState.ERROR || status == HardwareState.STANDBY) {
            sendErrorMessage(msg.getEndpoint());
            return;
        }

        if(status != HardwareState.EMERGENCY_STOP) {
            sendErrorMessage(msg.getEndpoint());
            return;
        }

        dispatcher.dispatch(new Message(GuiMessage.SYSTEM_NOTICE, new NoticeData(NoticeType.INFO, "Nothaltfreigabe", "Der Nothalt wurde wieder freigegeben")));
        if(automaticMode) {
            msgQueue.add(new Message(InternMessage.SET_HARDWARE_STATE, HardwareState.AUTOMATIC));
        } else {
            msgQueue.add(new Message(InternMessage.SET_HARDWARE_STATE, HardwareState.MANUEL));
        }
    }

    protected void setStandByMode(Message msg) {
        setStandByMode(msg, false);
    }

    protected void setStandByMode(Message msg, boolean toggle) {
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
            dispatcher.dispatch(
                new Message(GuiMessage.SYSTEM_NOTICE, new NoticeData(NoticeType.INFO, "Standby", "Anlage wird in den Standby-Modus geschickt"))
            );
            msgQueue.add(new Message(InternMessage.SET_HARDWARE_STATE, HardwareState.STANDBY));
            return;
        }

        if(status != HardwareState.STANDBY) {
            sendErrorMessage(msg.getEndpoint());
            return;
        }

        dispatcher.dispatch(new Message(GuiMessage.SYSTEM_NOTICE, new NoticeData(NoticeType.INFO, "Standby", "Die Anlage wird aus dem Standby-Modus geholt")));
        if(automaticMode) {
            msgQueue.add(new Message(InternMessage.SET_HARDWARE_STATE, HardwareState.AUTOMATIC));
        } else {
            msgQueue.add(new Message(InternMessage.SET_HARDWARE_STATE, HardwareState.MANUEL));
        }
    }

    protected void sendErrorMessage(Endpoint endpoint) {
        dispatcher.dispatch(
            new Message(ClientMessage.ERROR, new ErrorData(ErrorId.INVALID_STATUS_CHANGE, "Current state is <" + status.toString() + ">")), endpoint
        );
    }

    @Override
    public void hardwareStateChanged(HardwareState state) {
        status = state;
        dispatcher.dispatch(new Message(SystemMessage.HARDWARE_STATE_CHANGED, status.toString()));
    }
}
