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
import moba.server.messages.Message;
import moba.server.messages.MessageQueue;
import moba.server.messages.messagetypes.InternMessage;
import moba.server.messages.messagetypes.SystemMessage;
import moba.server.exceptions.ClientErrorException;
import moba.server.utilities.layout.TrackLayoutLock;

import java.sql.SQLException;

final public class Systems extends AbstractMessageHandler {
    private ServerState currState = ServerState.HALT;

    private final MessageQueue msgQueue;
    private final TrackLayoutLock lock;

    public Systems(Dispatcher dispatcher, TrackLayoutLock lock, MessageQueue msgQueue)
    throws SQLException {
        this.dispatcher      = dispatcher;
        this.msgQueue        = msgQueue;
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
    }

    @Override
    public void serverStateChanged(ServerState state) {
        dispatcher.sendGroup(new Message(SystemMessage.HARDWARE_STATE_CHANGED, state.toSystemState().toString()));
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
                msgQueue.add(new Message(InternMessage.SYSTEM_SHUTDOWN, msg.getEndpoint()));
                break;

            case HARDWARE_RESET:
                msgQueue.add(new Message(InternMessage.SERVER_RESET, msg.getEndpoint()));
                break;
        }
    }

    private void setAutomaticMode(Message msg) {
        if((boolean)msg.getData()) {
            msgQueue.add(new Message(InternMessage.SET_SERVER_STATE, ServerState.AUTOMATIC_MODE, msg.getEndpoint()));
        } else {
            msgQueue.add(new Message(InternMessage.SET_SERVER_STATE, ServerState.AUTOMATIC_HALT, msg.getEndpoint()));
        }
    }

    private void setReadyForAutomaticMode(Message msg) {
       if((boolean)msg.getData()) {
           msgQueue.add(new Message(InternMessage.SET_SERVER_STATE, ServerState.READY_FOR_AUTOMATIC_MODE, msg.getEndpoint()));
       } else {
           msgQueue.add(new Message(InternMessage.SET_SERVER_STATE, ServerState.MANUAL_MODE, msg.getEndpoint()));
       }
    }

    private void triggerEmergencyStop(Message msg) {
        msgQueue.add(new Message(InternMessage.EMERGENCY_STOP, msg.getData()));
    }

    private void releaseEmergencyStop(Endpoint endpoint) {
        msgQueue.add(new Message(InternMessage.RELEASE_INCIDENT_MODE, endpoint));
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
        msgQueue.add(new Message(InternMessage.SET_SERVER_STATE, ServerState.STANDBY, endpoint));
    }

    private void setStandByModeOff(Endpoint endpoint) {
        msgQueue.add(new Message(InternMessage.RELEASE_STANDBY_MODE, endpoint));
    }
}
