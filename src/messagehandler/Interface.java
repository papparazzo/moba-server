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

package messagehandler;

import application.ServerApplication;
import automode.ContactToBlock;
import com.SenderI;
import datatypes.enumerations.Connectivity;
import datatypes.enumerations.HardwareState;
import datatypes.enumerations.NoticeType;
import datatypes.objects.NoticeData;
import java.util.concurrent.PriorityBlockingQueue;
import messages.Message;
import messages.MessageHandlerA;
import messages.MessageType;

public class Interface extends MessageHandlerA {

    protected SenderI dispatcher = null;
    protected ServerApplication app = null;

    protected PriorityBlockingQueue<Message> msgQueueIn = null;

    protected ContactToBlock contactToBlock = null;

    public Interface(SenderI dispatcher, PriorityBlockingQueue<Message> msgQueueIn) {
        this.dispatcher = dispatcher;
        this.msgQueueIn = msgQueueIn;
    }

    public void init() throws ExceptionInInitializerError {
        contactToBlock = new ContactToBlock();

        contactToBlock.add

    }



    public void freeResources(long appId) {
    }


    public void shutdown() {
    }

    public void hardwareStateChanged(HardwareState state) {
    }





    @Override
    public void handleMsg(Message msg) {
        switch(msg.getMsgType()) {
            case INTERFACE_CONNECTIVITY_STATE_CHANGED:
                setConnectivity(Connectivity.valueOf((String)msg.getData()));
                return;

            case INTERFACE_CONTACT_TRIGGERED:

            default:
                throw new UnsupportedOperationException("unknow msg <" + msg.getMsgType().toString() + ">.");
        }
    }

    private void setConnectivity(Connectivity connectivity) {
        switch(connectivity) {
            case CONNECTED:
                msgQueueIn.add(new Message(MessageType.BASE_SET_HARDWARE_STATE, HardwareState.MANUEL));
                break;

            case ERROR:
                msgQueueIn.add(new Message(MessageType.BASE_SET_HARDWARE_STATE, HardwareState.ERROR));
                dispatcher.dispatch(
                    new Message(
                        MessageType.GUI_SYSTEM_NOTICE,
                        new NoticeData(NoticeType.ERROR, "Hardwarefehler", "Die Verbindung zur Harware wurde unterbrochen")
                    )
                );
                break;
        }
    }
}
