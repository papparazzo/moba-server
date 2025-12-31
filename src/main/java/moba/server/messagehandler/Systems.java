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

import moba.server.application.ServerStateMachine;
import moba.server.com.Dispatcher;
import moba.server.com.Endpoint;
import moba.server.datatypes.enumerations.*;
import moba.server.datatypes.objects.EmergencyTriggerData;
import moba.server.messages.AbstractMessageHandler;
import moba.server.messages.Message;
import moba.server.messages.MessageQueue;
import moba.server.messages.messagetypes.InternMessage;
import moba.server.messages.messagetypes.SystemMessage;
import moba.server.utilities.layout.TrackLayoutLock;

import java.sql.SQLException;

final public class Systems extends AbstractMessageHandler {
    private final MessageQueue msgQueue;
    private final TrackLayoutLock lock;
    private final ServerStateMachine stateMachine;

    public Systems(Dispatcher dispatcher, TrackLayoutLock lock, MessageQueue msgQueue, ServerStateMachine stateMachine)
    throws SQLException {
        this.dispatcher      = dispatcher;
        this.msgQueue        = msgQueue;
        this.lock            = lock;
        this.stateMachine    = stateMachine;
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
    }

    @Override
    public void handleMsg(Message msg)
    throws Exception {
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
                    new Message(
                        SystemMessage.HARDWARE_STATE_CHANGED,
                        stateMachine.getState().toSystemState().toString()
                    ),
                    msg.getEndpoint()
                );
                break;

            case HARDWARE_SHUTDOWN:
                stateMachine.setSystemShutdown(msg.getEndpoint());
                break;

            case HARDWARE_RESET:
                msgQueue.add(new Message(InternMessage.SERVER_RESET));
                break;
        }
    }

    private void setAutomaticMode(Message msg)
    throws SQLException {
        if((boolean)msg.getData()) {
            stateMachine.activateAutomaticMode(msg.getEndpoint());
        } else {
            stateMachine.deactivateAutomaticMode(msg.getEndpoint());
        }
    }

    private void setReadyForAutomaticMode(Message msg)
    throws SQLException {
       if((boolean)msg.getData()) {
           stateMachine.setReadyForAutomaticMode(msg.getEndpoint());
       } else {
           stateMachine.setManualMode(msg.getEndpoint());
       }
    }

    private void triggerEmergencyStop(Message msg)
    throws Exception {
        stateMachine.activateIncident(EmergencyTriggerData.fromMessage(msg), msg.getEndpoint());
    }

    private void releaseEmergencyStop(Endpoint endpoint)
    throws SQLException{
        stateMachine.releaseIncident(endpoint);
    }

    private void toggleStandByMode(Message msg)
    throws SQLException {
        if(stateMachine.getState() == ServerState.STANDBY) {
            stateMachine.deactivateStandby(msg.getEndpoint());
        } else {
            stateMachine.activateStandby(msg.getEndpoint());
        }
    }

    private void setStandByMode(Message msg)
    throws SQLException {
        if((boolean)msg.getData()) {
            stateMachine.activateStandby(msg.getEndpoint());
        } else {
            stateMachine.deactivateStandby(msg.getEndpoint());
        }
    }
}
