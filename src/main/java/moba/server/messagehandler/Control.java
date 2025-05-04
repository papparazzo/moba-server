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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.logging.Level;

import moba.server.com.Dispatcher;
import moba.server.utilities.Database;
import moba.server.datatypes.enumerations.DrivingDirection;
import moba.server.datatypes.enumerations.ClientError;
import moba.server.datatypes.enumerations.SwitchStand;
import moba.server.utilities.CheckedEnum;
import moba.server.datatypes.objects.BlockContactData;
import moba.server.datatypes.objects.ContactData;
import moba.server.datatypes.objects.SwitchStateData;
import moba.server.datatypes.objects.TrainData;
import moba.server.datatypes.objects.helper.ActiveLayout;
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
        long id = activeLayout.getActiveLayout(msg.getData());

        Connection con = database.getConnection();

        String q =
            "SELECT `BlockSections`.`Id`, `BlockSections`.`TrainId`, " +
            "`TrackLayoutSymbols`.`XPos`, `TrackLayoutSymbols`.`YPos`, " +
            "`TriggerContact`.`ModulAddress` AS `TriggerModulAddress`, " +
            "`TriggerContact`.`ContactNumber` AS `TriggerModulContactNumber`, " +
            "`BlockContact`.`ModulAddress` AS `BlockModulAddress`, " +
            "`BlockContact`.`ContactNumber` AS `BlockModulContactNumber` " +
            "FROM `BlockSections` " +
            "LEFT JOIN `FeedbackContacts` AS `TriggerContact` " +
            "ON `BrakeTriggerContactId` = `TriggerContact`.`Id` " +
            "LEFT JOIN `FeedbackContacts` AS `BlockContact` " +
            "ON `BlockContactId` = `BlockContact`.`Id` " +
            "LEFT JOIN `TrackLayoutSymbols` " +
            "ON `TrackLayoutSymbols`.`Id` = `BlockSections`.`Id` " +
            "WHERE `TrackLayoutSymbols`.`TrackLayoutId` = ? ";

        try (PreparedStatement pstmt = con.prepareStatement(q)) {
            pstmt.setLong(1, id);

            ArrayList<BlockContactData> arraylist;
            ResultSet rs = pstmt.executeQuery();
            arraylist = new ArrayList<>();
            
            while(rs.next()) {
                Integer trainId = rs.getInt("TrainId");
                trainId = rs.wasNull() ? null : trainId;

                arraylist.add(new BlockContactData(
                    rs.getInt("Id"),
                    rs.getInt("XPos"),
                    rs.getInt("YPos"),
                    new ContactData(rs.getInt("TriggerModulAddress"), rs.getInt("TriggerModulContactNumber")),
                    new ContactData(rs.getInt("BlockModulAddress"), rs.getInt("BlockModulContactNumber")),
                    trainId
                ));
            }
            dispatcher.sendSingle(new Message(ControlMessage.GET_BLOCK_LIST_RES, arraylist), msg.getEndpoint());
        }
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

       //// map.get("blockContacts");

        Connection con = database.getConnection();

        String stmt = "UPDATE `TrackLayouts` SET `ModificationDate` = NOW() WHERE `Id` = ? ";

        try (PreparedStatement pstmt = con.prepareStatement(stmt)) {
            pstmt.setLong(1, id);
            getLogger().log(Level.INFO, pstmt.toString());
            if(pstmt.executeUpdate() == 0) {
                throw new ClientErrorException(ClientError.DATASET_MISSING, "could not save <" + id + ">");
            }
        }

        stmt =
            "DELETE `BlockSections`.* " +
            "FROM `BlockSections` " +
            "LEFT JOIN `TrackLayoutSymbols` ON `TrackLayoutSymbols`.`Id` = `BlockSections`.`Id` "    +
            "WHERE `TrackLayoutId` = ?";

        try(PreparedStatement pstmt = con.prepareStatement(stmt)) {
            pstmt.setLong(1, id);
            getLogger().log(Level.INFO, pstmt.toString());
            pstmt.executeUpdate();
        }

        ArrayList<Object> arrayList = (ArrayList<Object>)map.get("symbols");

        for(Object item : arrayList) {
            Map<String, Object> block = (Map<String, Object>)item;

            stmt =
                "INSERT INTO `BlockSections` " +
                "(`Id`, `BrakeTriggerContactId`, `BlockContactId`, `TrainId`, `Locked`) " +
                "SELECT `Id`, ?, ?, ?, ? " +
                "FROM `TrackLayoutSymbols` " +
                "WHERE TrackLayoutSymbols.XPos = ? AND TrackLayoutSymbols.YPos = ?";

            try(PreparedStatement pstmt = con.prepareStatement(stmt)) {
/*

Integer	xPos
Integer	yPos
ContactData	brakeTriggerContact
ContactData	blockContact
Integer	trainId

*/

                pstmt.setLong(1, (long)block.get("symbol"));

                pstmt.setLong(2, id);
                pstmt.setLong(3, (long)block.get("xPos"));
                pstmt.setLong(4, (long)block.get("yPos"));
                getLogger().log(Level.INFO, pstmt.toString());
                pstmt.executeUpdate();
            }
        }

        //dispatcher.dispatch(new IncidentData(LayoutMessage.LAYOUT_CHANGED, map));

    }

    protected void getSwitchStateList(Message msg)
    throws SQLException, ClientErrorException {
        long id = activeLayout.getActiveLayout(msg.getData());

        Connection con = database.getConnection();

        String q =
            "SELECT `SwitchDrive`.`Id`, `SwitchDrive`.`SwitchStand`, " +
            "`TrackLayoutSymbols`.`XPos`, `TrackLayoutSymbols`.`YPos` " +
            "FROM SwitchDrive " +
            "LEFT JOIN `TrackLayoutSymbols` " +
            "ON `TrackLayoutSymbols`.`Id` = `SwitchDrive`.`Id` " +
            "WHERE `TrackLayoutSymbols`.`TrackLayoutId` = ? ";

        try (PreparedStatement pstmt = con.prepareStatement(q)) {
            pstmt.setLong(1, id);

            ArrayList<SwitchStateData> arraylist;
            ResultSet rs = pstmt.executeQuery();
            arraylist = new ArrayList<>();
            while(rs.next()) {
                arraylist.add(new SwitchStateData(
                    rs.getInt("Id"),
                    rs.getInt("XPos"),
                    rs.getInt("YPos"),
                    CheckedEnum.getFromString(SwitchStand.class, rs.getString("SwitchStand"))
                ));
            }
            dispatcher.sendSingle(new Message(ControlMessage.GET_SWITCH_STAND_LIST_RES, arraylist), msg.getEndpoint());
        }
    }

    protected void getTrainList(Message msg)
    throws SQLException, ClientErrorException {
        long id = activeLayout.getActiveLayout(msg.getData());

        Connection con = database.getConnection();
        String q =
            "SELECT Trains.Id, Address, Speed, DrivingDirection " +
            "FROM Trains " +
            "LEFT JOIN BlockSections " +
            "ON BlockSections.TrainId = Trains.Id " +
            "LEFT JOIN `TrackLayoutSymbols` " +
            "ON `TrackLayoutSymbols`.`Id` = `BlockSections`.`Id` " +
            "WHERE `TrackLayoutSymbols`.`TrackLayoutId` = ? ";

        try (PreparedStatement pstmt = con.prepareStatement(q)) {
            pstmt.setLong(1, id);

            ArrayList<TrainData> arraylist;
            ResultSet rs = pstmt.executeQuery();
            arraylist = new ArrayList<>();
            while(rs.next()) {
                arraylist.add(new TrainData(
                    rs.getInt("Id"),
                    rs.getInt("Address"),
                    rs.getInt("Speed"),
                    CheckedEnum.getFromString(DrivingDirection.class, rs.getString("DrivingDirection"))
                ));
            }
            dispatcher.sendSingle(new Message(ControlMessage.GET_TRAIN_LIST_RES, arraylist), msg.getEndpoint());
        }
    }

    @SuppressWarnings("unchecked")
    protected void pushTrain(Message msg)
    throws SQLException, ClientErrorException {
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
        dispatcher.sendGroup(new Message(ControlMessage.PUSH_TRAIN, msg.getData()));
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
