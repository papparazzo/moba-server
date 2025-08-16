/*
 *  Project:    moba-server
 *
 *  Copyright (C) 2020 Stefan Paproth <pappi-@gmx.de>
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

import java.sql.SQLException;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import moba.server.com.Dispatcher;
import moba.server.datatypes.collections.BlockContactDataMap;
import moba.server.datatypes.collections.SwitchStateMap;
import moba.server.datatypes.collections.TrainList;
import moba.server.datatypes.objects.ActionListCollection;
import moba.server.datatypes.objects.TrainData;
import moba.server.messages.messageType.InterfaceMessage;
import moba.server.repositories.BlockListRepository;
import moba.server.repositories.LayoutRepository;
import moba.server.repositories.SwitchStateRepository;
import moba.server.repositories.TrainlistRepository;
import moba.server.routing.LayoutParser;
import moba.server.routing.Router;
import moba.server.routing.TrainRun;
import moba.server.routing.typedefs.*;
import moba.server.utilities.Database;
import moba.server.utilities.ActiveLayout;
import moba.server.messages.Message;
import moba.server.messages.MessageHandlerA;
import moba.server.messages.messageType.ControlMessage;
import moba.server.utilities.exceptions.ClientErrorException;
import moba.server.utilities.lock.BlockLock;
import moba.server.utilities.logger.Loggable;

public class Control extends MessageHandlerA implements Loggable {
    protected final Database       database;
    protected final BlockLock      blockLock;
    protected final Queue<Message> queue;
    protected final ActiveLayout   activeLayout;

    public Control(Dispatcher dispatcher, Database database, ActiveLayout activeLayout)
    throws SQLException {
        this.dispatcher = dispatcher;
        this.database   = database;
        this.blockLock  = new BlockLock(database);
        //this.lock       = new TrackLayoutLock(database);
        this.queue      = new LinkedList<>();
        this.blockLock.resetAll();
        this.activeLayout = activeLayout;
    }

    @Override
    public int getGroupId() {
        return ControlMessage.GROUP_ID;
    }

    @Override
    public void shutdown()
    throws SQLException {
        blockLock.resetAll();
    }

    @Override
    public void freeResources(long appId)
    throws SQLException {
        blockLock.resetOwn(appId);
    }

    @Override
    public void handleMsg(Message msg)
    throws ClientErrorException, SQLException {
        switch(ControlMessage.fromId(msg.getMessageId())) {
            case GET_BLOCK_LIST_REQ        -> getBlockList(msg);
            case SAVE_BLOCK_LIST           -> saveBlockList(msg);
            case GET_SWITCH_STAND_LIST_REQ -> getSwitchStateList(msg);
            case GET_TRAIN_LIST_REQ        -> getTrainList(msg);
            case LOCK_BLOCK                -> lockBlock(msg, false);
            case LOCK_BLOCK_WAITING        -> lockBlock(msg, true);
            case UNLOCK_BLOCK              -> unLockBlock(msg);
            case PUSH_TRAIN                -> pushTrain(msg);
        }
    }

    protected void getBlockList(Message msg)
    throws SQLException, ClientErrorException {
        BlockListRepository blocklistRepository = new BlockListRepository(database);
        long id = activeLayout.getActiveLayout(msg.getData());

        BlockContactDataMap blockContactDataMap = blocklistRepository.getBlockList(id);
        dispatcher.sendSingle(new Message(ControlMessage.GET_BLOCK_LIST_RES, blockContactDataMap), msg.getEndpoint());
    }

    @SuppressWarnings("unchecked")
    protected void saveBlockList(Message msg)
    throws SQLException, ClientErrorException {

        Map<String, Object> map = (Map<String, Object>)msg.getData();
        long id = (long)map.get("id");

        /*
        if(!lock.isLockedByApp(msg.getEndpoint().getAppId(), id)) {
            throw new ClientErrorException(ClientError.DATASET_NOT_LOCKED, "layout <" + String.valueOf(id) + "> not locked");
        }
        */
        BlockListRepository blocklist = new BlockListRepository(database);

        //dispatcher.dispatch(new IncidentData(LayoutMessage.LAYOUT_CHANGED, map));
    }

    protected void getSwitchStateList(Message msg)
    throws SQLException, ClientErrorException {
        SwitchStateRepository switchStateRepository = new SwitchStateRepository(database);
        long id = activeLayout.getActiveLayout(msg.getData());
        SwitchStateMap switchStateList = switchStateRepository.getSwitchStateList(id);
        dispatcher.sendSingle(new Message(ControlMessage.GET_SWITCH_STAND_LIST_RES, switchStateList), msg.getEndpoint());
    }

    protected void getTrainList(Message msg)
    throws SQLException, ClientErrorException {
        TrainlistRepository trainlistRepository = new TrainlistRepository(database);
        long id = activeLayout.getActiveLayout(msg.getData());
        TrainList trainList = trainlistRepository.getTrainList(id);
        dispatcher.sendSingle(new Message(ControlMessage.GET_TRAIN_LIST_RES, trainList), msg.getEndpoint());
    }

    @SuppressWarnings("unchecked")
    protected void pushTrain(Message msg)
    throws SQLException, ClientErrorException {

        //long id = activeLayout.getActiveLayout(msg.getData());
        long id = 10;


        TrainlistRepository trainList = new TrainlistRepository(database);

        LayoutRepository layout = new LayoutRepository(database);
        BlockListRepository blocklist = new BlockListRepository(database);
        BlockContactDataMap blockContacts = blocklist.getBlockList(id);

        SwitchStateRepository switchState = new SwitchStateRepository(database);

        LayoutParser parser = new LayoutParser(
            layout.getLayout(id),
            blockContacts,
            switchState.getSwitchStateList(id)
        );

        parser.parse();

        BlockNodeMap blocks = parser.getBlockMap();
        SwitchNodeMap switches = parser.getSwitchMap();

        Router router = new Router(blocks);

        TrainRun runner = new TrainRun(blockContacts, router);

        long trainId = 1;

        TrainData train = trainList.getTrainList(id).get(trainId);

        int toBlockId = (int)(long)msg.getData();

        ActionListCollection actionLists = runner.getActionList(train, toBlockId);
        dispatcher.sendGroup(new Message(InterfaceMessage.SET_ACTION_LIST, actionLists));
         
       /*

        Connection con = database.getConnection();

        var data = (Map<String, Object>)msg.getData();
        var trainId = (long)data.get("trainId");
        var fromBlock = (long)data.get("fromBlock");
        var toBlock = (long)data.get("toBlock");

        // FIXME: Transaction
        String q =
            "UPDATE `BlockSections` SET `BlockSections`.`TrainId` = NULL " +
            "WHERE `BlockSections`.`Id` = ? AND `BlockSections`.`TrainId` = ?";

        try (PreparedStatement pstmt = con.prepareStatement(q)) {
            pstmt.setLong(1, fromBlock);
            pstmt.setLong(2, trainId);
            if(pstmt.executeUpdate() != 1) {
                throw new ClientErrorException(ClientError.DATASET_MISSING, "could not update <" + trainId + ">");
            }
        }

        q =
            "UPDATE `BlockSections` SET `BlockSections`.`TrainId` = ? " +
            "WHERE `BlockSections`.`Id` = ?";

        try (PreparedStatement pstmt = con.prepareStatement(q)) {
            pstmt.setLong(1, trainId);
            pstmt.setLong(2, toBlock);
            if(pstmt.executeUpdate() != 1) {
                throw new ClientErrorException(ClientError.DATASET_MISSING, "could not update <" + trainId + ">");
            }
        }

        // TODO Das hier per Api setzen!

        q =
            "UPDATE `Trains` SET `Trains`.`DrivingDirection` = ? " +
            "WHERE `Trains`.`Id` = ?";

        try (PreparedStatement pstmt = con.prepareStatement(q)) {
            pstmt.setString(1, (String)data.get("direction"));
            pstmt.setLong(2, trainId);
            if(pstmt.executeUpdate() == 0) {
                throw new ClientErrorException(ClientError.DATASET_MISSING, "could not update <" + trainId + ">");
            }
        }
       // dispatcher.sendGroup(new Message(ControlMessage.PUSH_TRAIN, msg.getData()));
        */
    }

    protected void lockBlock(Message msg, boolean wait)
    throws SQLException {
        try {
            blockLock.tryLock(msg.getEndpoint().getAppId(), msg.getData());
            dispatcher.sendSingle(new Message(ControlMessage.BLOCK_LOCKED, msg.getData()), msg.getEndpoint());
        } catch(ClientErrorException ex) {
            if(!wait) {
                dispatcher.sendSingle(new Message(ControlMessage.BLOCK_LOCKING_FAILED, msg.getData()), msg.getEndpoint());
                return;
            }
            queue.add(msg);
        }
    }

    protected void unLockBlock(Message msg)
    throws ClientErrorException, SQLException {
        blockLock.unlock(msg.getEndpoint().getAppId(), msg.getData());

        if(!queue.isEmpty()) {
            lockBlock(queue.remove(), true);
        }
    }
}
