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
import moba.server.com.Dispatcher;
import moba.server.datatypes.enumerations.*;
import moba.server.datatypes.objects.TrainJourney;
import moba.server.messages.AbstractMessageHandler;
import moba.server.messages.Message;
import moba.server.messages.MessageQueue;
import moba.server.messages.messagetypes.InterfaceMessage;
import moba.server.messages.messagetypes.InternMessage;
import moba.server.exceptions.ClientErrorException;

import java.sql.SQLException;

final public class Interface extends AbstractMessageHandler {

    private final MessageQueue msgQueueIn;
    private final TrainRunner runner;
    private ServerState serverState = ServerState.HALT;

    public Interface(Dispatcher dispatcher, MessageQueue msgQueueIn, TrainRunner runner) {
        this.dispatcher      = dispatcher;
        this.msgQueueIn      = msgQueueIn;
        this.runner          = runner;
    }

    @Override
    public int getGroupId() {
        return InterfaceMessage.GROUP_ID;
    }

    @Override
    public void serverStateChanged(ServerState state) {
        serverState = state;
    }

    @Override
    public void handleMsg(Message msg)
    throws SQLException, ClientErrorException {
        switch(InterfaceMessage.fromId(msg.getMessageId())) {
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

    private void routeSwitched(Message msg) {
        int id = (int)msg.getData();
        runner.setSwitched(id);
        checkServerState();
    }

    private void releaseRoute(Message msg)
    throws SQLException, ClientErrorException {
        int id = (int)msg.getData();
        runner.releaseRoute(id);
        checkServerState();
    }

    private void releaseBlock(Message msg)
    throws SQLException, ClientErrorException {
        int blockId = (int)msg.getData();
        // TODO: Wir brauchen hier noch die TrainId
        int trainId = 0;
        runner.releaseBlock(trainId, blockId);
        checkServerState();
    }

    private void pushTrain(Message msg) {
        TrainJourney train = (TrainJourney)msg.getData();

        runner.pushTrain(train);
        checkServerState();
    }


    private void checkServerState() {
        if(runner.trainsToHandle()) {
            return;
        }

        if(serverState == ServerState.AUTOMATIC_HALT) {
            msgQueueIn.add(new Message(InternMessage.SET_SERVER_STATE, ServerState.READY_FOR_AUTOMATIC_MODE));
        } else if(serverState == ServerState.AUTOMATIC_HALT_FOR_SHUTDOWN) {
            msgQueueIn.add(new Message(InternMessage.SET_SERVER_STATE, ServerState.READY_FOR_SHUTDOWN));
        }
    }
}
