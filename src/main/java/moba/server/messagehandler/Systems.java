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
import moba.server.datatypes.enumerations.EmergencyTriggerReason;
import moba.server.datatypes.enumerations.ErrorId;
import moba.server.datatypes.enumerations.HardwareState;
import moba.server.datatypes.enumerations.NoticeType;
import moba.server.datatypes.enumerations.helper.CheckedEnum;
import moba.server.datatypes.objects.ErrorData;
import moba.server.datatypes.objects.NoticeData;
import moba.server.datatypes.objects.helper.ActiveLayout;
import moba.server.messages.Message;
import moba.server.messages.MessageHandlerA;
import moba.server.messages.MessageQueue;
import moba.server.messages.messageType.ClientMessage;
import moba.server.messages.messageType.GuiMessage;
import moba.server.messages.messageType.InternMessage;
import moba.server.messages.messageType.SystemMessage;
import moba.server.utilities.exceptions.ErrorException;
import moba.server.utilities.lock.TrackLayoutLock;

public class Systems extends MessageHandlerA {
    protected HardwareState status = HardwareState.ERROR;
    protected MessageQueue msgQueue;
    protected ActiveLayout activeLayout;

    protected boolean automaticMode = false;
    protected TrackLayoutLock lock;

    public Systems(Dispatcher dispatcher, TrackLayoutLock lock, ActiveLayout activeLayout, MessageQueue msgQueue) {
        this.dispatcher   = dispatcher;
        this.msgQueue     = msgQueue;
        this.activeLayout = activeLayout;
        this.lock         = lock;
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
                dispatcher.dispatch(new Message(SystemMessage.HARDWARE_STATE_CHANGED, status.toString(), msg.getEndpoint()));
                break;

            case HARDWARE_SHUTDOWN:
                msgQueue.add(new Message(InternMessage.SERVER_SHUTDOWN, null));
                break;

            case HARDWARE_RESET:
                msgQueue.add(new Message(InternMessage.SERVER_RESET, null));
                break;

            default:
                throw new ErrorException(ErrorId.UNKNOWN_MESSAGE_ID, "unknown msg <" + Long.toString(msg.getMessageId()) + ">.");
        }
    }

    protected void setAutomaticMode(Message msg)
    throws ErrorException {
        setAutomaticMode(msg, false);
    }

    protected void setAutomaticMode(Message msg, boolean toggle)
    throws ErrorException {
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
            dispatcher.dispatch(
                new Message(GuiMessage.SYSTEM_NOTICE, new NoticeData(NoticeType.INFO, "Automatik", "Die Hardware befindet sich im Automatikmodus"))
            );
            return;
        }

        if(status != HardwareState.AUTOMATIC) {
            sendErrorMessage(msg.getEndpoint());
            return;
        }

        lock.resetOwn(0);

        msgQueue.add(new Message(InternMessage.SET_HARDWARE_STATE, HardwareState.MANUEL));
        dispatcher.dispatch(new Message(GuiMessage.SYSTEM_NOTICE, new NoticeData(NoticeType.INFO, "Automatik", "Automatikmodus wurde deaktiviert")));
    }

    protected void triggerEmergencyStop(Message msg)
    throws ErrorException {
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

    protected String getEmergencyStopReason(String reason)
    throws ErrorException {
        return switch(CheckedEnum.getFromString(EmergencyTriggerReason.class, reason)) {
            case CENTRAL_STATION                 -> "Auslösegrund: Es wurde ein Nothalt durch die CentralStation ausgelöst";
            case EXTERN                          -> "Auslösegrund: Externe Hardware";
            case SELF_ACTING_BY_EXTERN_SWITCHING -> "Auslösegrund: Weichenstellung durch CS im Automatikmodus";
            case SOFTWARE_MANUEL                 -> "Auslösegrund: Manuell durch Steuerungssoftware";
        };
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
            new Message(ClientMessage.ERROR, new ErrorData(ErrorId.INVALID_STATUS_CHANGE, "Current state is <" + status.toString() + ">"), endpoint)
        );
    }

    @Override
    public void hardwareStateChanged(HardwareState state) {
        status = state;
        dispatcher.dispatch(new Message(SystemMessage.HARDWARE_STATE_CHANGED, status.toString()));
    }
}
