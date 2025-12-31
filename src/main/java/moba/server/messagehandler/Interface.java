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
 *  along with this program. If not, see <https://www.gnu.org/licenses/agpl.txt>.
 *
 */

package moba.server.messagehandler;

import moba.server.actionhandler.TrainRunner;
import moba.server.application.ServerStateMachine;
import moba.server.com.Dispatcher;
import moba.server.com.Endpoint;
import moba.server.datatypes.enumerations.ServerState;
import moba.server.datatypes.objects.TrainJourney;
import moba.server.exceptions.ClientErrorException;
import moba.server.messages.AbstractMessageHandler;
import moba.server.messages.Message;
import moba.server.messages.messagetypes.InterfaceMessage;

import java.sql.SQLException;

final public class Interface extends AbstractMessageHandler {

    private final ServerStateMachine stateMachine;
    private final TrainRunner runner;

    public Interface(Dispatcher dispatcher, ServerStateMachine stateMachine, TrainRunner runner) {
        this.dispatcher      = dispatcher;
        this.stateMachine    = stateMachine;
        this.runner          = runner;
    }

    @Override
    public int getGroupId() {
        return InterfaceMessage.GROUP_ID;
    }

    @Override
    public void serverStateChanged(ServerState state)
    throws SQLException {
        checkServerState(null);
    }

    @Override
    public void handleMsg(Message msg)
    throws Exception {
        switch(InterfaceMessage.fromId(msg.getMessageId())) {
            case CONNECTED
                -> setConnected(msg);

            case CONNECTION_LOST
                -> setConnectionLost(msg);

            case ROUTE_SWITCHED
                -> routeSwitched(msg);

            case ROUTE_RELEASED
                -> releaseRoute(msg);

            case BLOCK_RELEASED
                -> releaseBlock(msg);

            case PUSH_TRAIN
                -> pushTrain(msg);

            case SET_ACTION_LIST,
                 REPLACE_ACTION_LIST,
                 DELETE_ACTION_LIST
                -> dispatcher.sendGroup(msg);
        }
    }

    private void setConnected(Message msg)
    throws SQLException {
        boolean onInitialize = (boolean)msg.getData();
        stateMachine.setConnected(msg.getEndpoint(), onInitialize);
    }

    private void setConnectionLost(Message msg)
    throws  SQLException {
        stateMachine.setConnectionLost(msg.getEndpoint());
    }

    private void routeSwitched(Message msg)
    throws  SQLException {
        int id = (int)msg.getData();
        runner.setSwitched(id);
        checkServerState(msg.getEndpoint());
    }

    private void releaseRoute(Message msg)
    throws  SQLException, ClientErrorException {
        int id = (int)msg.getData();
        runner.releaseRoute(id);
        checkServerState(msg.getEndpoint());
    }

    private void releaseBlock(Message msg)
    throws Exception {
        int blockId = (int)msg.getData();
        // TODO: Wir brauchen hier noch die TrainId
        int trainId = 0;
        runner.releaseBlock(trainId, blockId);
        checkServerState(msg.getEndpoint());
    }

    private void pushTrain(Message msg)
    throws SQLException {
        TrainJourney train = (TrainJourney)msg.getData();

        runner.pushTrain(train);
        checkServerState(msg.getEndpoint());
    }


    private void checkServerState(Endpoint endpoint)
    throws SQLException {
        if(runner.trainsToHandle()) {
            return;
        }
        stateMachine.automaticModeFinished(endpoint);
    }
}
