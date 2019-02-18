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

package messagehandler;

import com.Endpoint;
import java.util.concurrent.PriorityBlockingQueue;

import datatypes.enumerations.ErrorId;
import datatypes.enumerations.HardwareState;
import datatypes.enumerations.NoticeType;
import datatypes.objects.ErrorData;
import datatypes.objects.NoticeData;
import messages.Message;
import messages.MessageHandlerA;
import messages.MessageType;

public class Systems extends MessageHandlerA {
    protected HardwareState status = HardwareState.ERROR;
    protected PriorityBlockingQueue<Message> msgQueueOut = null;
    protected PriorityBlockingQueue<Message> msgQueue = null;

    protected boolean automaticMode = false;

    public Systems(PriorityBlockingQueue<Message> msgQueueOut, PriorityBlockingQueue<Message> msgQueue) {
        this.msgQueueOut = msgQueueOut;
        this.msgQueue   = msgQueue;
    }

    @Override
    public void handleMsg(Message msg) {
        switch(msg.getMsgType()) {
            case SYSTEM_SET_AUTOMATIC_MODE:
                setAutomaticMode(msg);
                break;

            case SYSTEM_SET_EMERGENCY_STOP:
                setEmergencyStop(msg);
                break;

            case SYSTEM_SET_STANDBY_MODE:
                setStandByMode(msg);
                break;

            case SYSTEM_GET_HARDWARE_STATE:
                msgQueueOut.add(
                    new Message(
                        MessageType.SYSTEM_HARDWARE_STATE_CHANGED,
                        status.toString(),
                        msg.getEndpoint()
                    )
                );
                break;

            case SYSTEM_HARDWARE_SHUTDOWN:
                msgQueue.add(new Message(MessageType.BASE_SERVER_SHUTDOWN));
                break;

            case SYSTEM_HARDWARE_RESET:
                msgQueue.add(new Message(MessageType.BASE_SERVER_RESET));
                break;

            default:
                throw new UnsupportedOperationException(
                    "unknow msg <" + msg.getMsgType().toString() + ">."
                );
        }
    }

    protected void setAutomaticMode(Message msg) {
        if(
            status == HardwareState.ERROR ||
            status == HardwareState.EMERGENCY_STOP ||
            status == HardwareState.STANDBY
        ) {
            sendErrorMessage(msg.getEndpoint());
            return;
        }

        boolean setAutomaticMode = (boolean)msg.getData();

        if(setAutomaticMode && status == HardwareState.AUTOMATIC) {
            sendErrorMessage(msg.getEndpoint());
            return;
        }

        automaticMode = setAutomaticMode;

        if(setAutomaticMode) {
            msgQueue.add(new Message(MessageType.BASE_SET_HARDWARE_STATE, HardwareState.AUTOMATIC));
            msgQueueOut.add(
                new Message(
                    MessageType.GUI_SYSTEM_NOTICE,
                    new NoticeData(NoticeType.INFO, "Automatik", "Die Hardware befindet sich im Automatikmodus")
                )
            );
            return;
        }
        msgQueue.add(new Message(MessageType.BASE_SET_HARDWARE_STATE, HardwareState.MANUEL));
        msgQueueOut.add(
            new Message(
                MessageType.GUI_SYSTEM_NOTICE,
                new NoticeData(NoticeType.INFO, "Automatik", "Automatikmodus wurde deaktiviert")
            )
        );
    }

    protected void setEmergencyStop(Message msg) {
        if(status == HardwareState.ERROR || status == HardwareState.STANDBY) {
            sendErrorMessage(msg.getEndpoint());
            return;
        }

        boolean emergencyStop = (boolean)msg.getData();

        if(emergencyStop && status == HardwareState.EMERGENCY_STOP) {
            sendErrorMessage(msg.getEndpoint());
            return;
        }

        if(emergencyStop) {
            msgQueueOut.add(
                new Message(
                    MessageType.GUI_SYSTEM_NOTICE,
                    new NoticeData(NoticeType.WARNING, "Nothalt gedrückt", "Es wurde ein Nothalt ausgelöst")
                )
            );
            msgQueue.add(new Message(MessageType.BASE_SET_HARDWARE_STATE, HardwareState.EMERGENCY_STOP));
            return;
        }
        msgQueueOut.add(
            new Message(
                MessageType.GUI_SYSTEM_NOTICE,
                new NoticeData(NoticeType.INFO, "Nothaltfreigabe", "Der Nothalt wurde wieder freigegeben")
            )
        );
        if(automaticMode) {
            msgQueue.add(new Message(MessageType.BASE_SET_HARDWARE_STATE, HardwareState.AUTOMATIC));
        } else {
            msgQueue.add(new Message(MessageType.BASE_SET_HARDWARE_STATE, HardwareState.MANUEL));
        }
    }

    protected void setStandByMode(Message msg) {
        if(status == HardwareState.ERROR || status == HardwareState.EMERGENCY_STOP) {
            sendErrorMessage(msg.getEndpoint());
            return;
        }

        boolean setStandByMode = (boolean)msg.getData();

        if(setStandByMode && status == HardwareState.STANDBY) {
            sendErrorMessage(msg.getEndpoint());
            return;
        }

        if(setStandByMode) {
            msgQueueOut.add(
                new Message(
                    MessageType.GUI_SYSTEM_NOTICE,
                    new NoticeData(NoticeType.WARNING, "Standby", "Anlage wird in den Standby-Modus geschickt")
                )
            );
            msgQueue.add(new Message(MessageType.BASE_SET_HARDWARE_STATE, HardwareState.STANDBY));
            return;
        }
        msgQueueOut.add(
            new Message(
                MessageType.GUI_SYSTEM_NOTICE,
                new NoticeData(NoticeType.INFO, "Standby", "Die Anlage wird aus dem Standby-Modus geholt")
            )
        );
        if(automaticMode) {
            msgQueue.add(new Message(MessageType.BASE_SET_HARDWARE_STATE, HardwareState.AUTOMATIC));
        } else {
            msgQueue.add(new Message(MessageType.BASE_SET_HARDWARE_STATE, HardwareState.MANUEL));
        }
    }

    protected void sendErrorMessage(Endpoint endpoint) {
        msgQueueOut.add(
            new Message(
                MessageType.CLIENT_ERROR,
                new ErrorData(
                    ErrorId.INVALID_STATUS_CHANGE,
                    "Current state is <" + status.toString() + ">"
                ),
                endpoint
            )
        );
    }

    @Override
    public void hardwareStateChanged(HardwareState state) {
        status = state;
        msgQueueOut.add(
            new Message(MessageType.SYSTEM_HARDWARE_STATE_CHANGED, status.toString())
        );
    }
}
