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
 *  along with this program. If not, see <http://www.gnu.org/licenses/agpl.txt>.
 *
 */

package moba.server.messagehandler;

import moba.server.application.ServerApplication;
import moba.server.com.Dispatcher;
import moba.server.datatypes.enumerations.Connectivity;
import moba.server.datatypes.enumerations.HardwareState;
import moba.server.datatypes.enumerations.NoticeType;
import moba.server.datatypes.objects.NoticeData;
import moba.server.datatypes.enumerations.ErrorId;
import moba.server.messages.Message;
import moba.server.messages.MessageHandlerA;
import moba.server.messages.MessageQueue;
import moba.server.messages.messageType.GuiMessage;
import moba.server.messages.messageType.InterfaceMessage;
import moba.server.messages.messageType.InternMessage;
import moba.server.utilities.exceptions.ErrorException;

public class Interface extends MessageHandlerA {
    protected ServerApplication app = null;

    protected MessageQueue msgQueueIn = null;

    public Interface(Dispatcher dispatcher, MessageQueue msgQueueIn) {
        this.dispatcher = dispatcher;
        this.msgQueueIn = msgQueueIn;
    }

    @Override
    public int getGroupId() {
        return InterfaceMessage.GROUP_ID;
    }

    @Override
    public void handleMsg(Message msg)
    throws ErrorException {
        switch(InterfaceMessage.fromId(msg.getMessageId())) {
            case CONNECTIVITY_STATE_CHANGED:
                setConnectivity(Connectivity.valueOf((String)msg.getData()));
                return;

            case CONTACT_TRIGGERED:
            case SET_BRAKE_VECTOR:
            case SET_LOCO_SPEED:
            case SET_LOCO_DIRECTION:
                dispatcher.dispatch(msg);
                return;

            default:
                throw new ErrorException(ErrorId.UNKNOWN_MESSAGE_ID, "unknow msg <" + Long.toString(msg.getMessageId()) + ">.");
        }
    }

    private void setConnectivity(Connectivity connectivity) {
        switch(connectivity) {
            case CONNECTED:
                msgQueueIn.add(new Message(InternMessage.SET_HARDWARE_STATE, HardwareState.MANUEL));
                dispatcher.dispatch(
                    new Message(
                        GuiMessage.SYSTEM_NOTICE,
                        new NoticeData(NoticeType.INFO, "Hardwareverbindung", "Die Verbindung zur Harware wurde hergestellt")
                    )
                );
                break;

            case ERROR:
                msgQueueIn.add(new Message(InternMessage.SET_HARDWARE_STATE, HardwareState.ERROR));
                dispatcher.dispatch(
                    new Message(
                        GuiMessage.SYSTEM_NOTICE,
                        new NoticeData(NoticeType.ERROR, "Hardwareverbindung", "Die Verbindung zur Harware wurde unterbrochen")
                    )
                );
                break;
        }
    }
}
