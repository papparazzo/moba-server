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
import datatypes.enumerations.Connectivity;
import datatypes.enumerations.HardwareState;
import datatypes.enumerations.NoticeType;
import datatypes.objects.NoticeData;
import java.util.concurrent.Executors;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import messages.Message;
import messages.MessageHandlerA;
import messages.MessageType;

public class Interface extends MessageHandlerA implements Runnable {
    protected static final Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    protected ServerApplication app = null;

    protected PriorityBlockingQueue<Message> msgQueueIn = null;
    protected PriorityBlockingQueue<Message> msgQueueOut = null;

    protected final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public Interface(PriorityBlockingQueue<Message> msgQueueOut, PriorityBlockingQueue<Message> msgQueueIn) {
        this.msgQueueOut = msgQueueOut;
        this.msgQueueIn   = msgQueueIn;
        this.scheduler.scheduleWithFixedDelay(this, 1, 30, TimeUnit.SECONDS);
    }

    @Override
    public void run() {
        try {
            msgQueueOut.add(new Message(MessageType.INTERFACE_CONNECTIVITY_REQ));
        } catch(Exception e) {
            LOGGER.log(Level.WARNING, "exception in scheduler occured! <{0}>", new Object[]{e.toString()});
        }
    }

    @Override
    public void handleMsg(Message msg) {
        switch(msg.getMsgType()) {
            case INTERFACE_CONNECTIVITY_RES:
                setConnectivity(Connectivity.valueOf((String)msg.getData()));
                return;

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
                msgQueueOut.add(
                    new Message(
                        MessageType.GUI_SYSTEM_NOTICE,
                        new NoticeData(NoticeType.ERROR, "Hardwarefehler", "Die Verbindung zur Harware wurde unterbrochen")
                    )
                );
                break;
        }
    }
}