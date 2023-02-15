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
 *  along with this program. If not, see <http://www.gnu.org/licenses/agpl.txt>.
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
import moba.server.database.Database;
import moba.server.datatypes.enumerations.DrivingDirection;
import moba.server.datatypes.enumerations.ErrorId;
import moba.server.datatypes.enumerations.SwitchStand;
import moba.server.datatypes.objects.BlockContactData;
import moba.server.datatypes.objects.ContactData;
import moba.server.datatypes.objects.SwitchStateData;
import moba.server.datatypes.objects.TrainData;
import moba.server.messages.Message;
import moba.server.messages.MessageHandlerA;
import moba.server.messages.messageType.ControlMessage;
import moba.server.messages.messageType.LayoutMessage;
import moba.server.utilities.config.Config;
import moba.server.utilities.exceptions.ErrorException;
import moba.server.utilities.lock.BlockLock;
import moba.server.utilities.lock.TracklayoutLock;
import moba.server.utilities.logger.Loggable;

public class Control extends MessageHandlerA implements Loggable {
    protected Database       database     = null;
    protected BlockLock      blockLock    = null;
    protected Config         config       = null;
    protected Queue<Message> queue        = null;
    protected long           activeLayout = 0;

    public Control(Dispatcher dispatcher, Database database, Config config) {
        this.dispatcher = dispatcher;
        this.database   = database;
        this.blockLock       = new BlockLock(database);
        //this.lock       = new TracklayoutLock(database);
        this.queue      = new LinkedList<>();
        this.config     = config;
        this.blockLock.resetAll();
    }

    @Override
    public int getGroupId() {
        return ControlMessage.GROUP_ID;
    }

    @Override
    public void init() {
        Object o;
        o = config.getSection("trackLayout.activeTracklayoutId");
        if(o != null) {
            activeLayout = (long)o;
        }
    }

    @Override
    public void freeResources() {
        blockLock.resetAll();
    }

    @Override
    public void freeResources(long appId) {
        blockLock.resetOwn(appId);
    }

    @Override
    public void handleMsg(Message msg)
    throws ErrorException {
        try {
            switch(ControlMessage.fromId(msg.getMessageId())) {
                case GET_BLOCK_LIST_REQ:
                    getBlockList(msg);
                    break;

                case SAVE_BLOCK_LIST:
                    saveBlockList(msg);

                case GET_SWITCH_STAND_LIST_REQ:
                    getSwitchStateList(msg);
                    break;

                case GET_TRAIN_LIST_REQ:
                    getTrainList(msg);
                    break;

                case LOCK_BLOCK:
                    lockBlock(msg, false);
                    break;

                case LOCK_BLOCK_WAITING:
                    lockBlock(msg, true);
                    break;

                case UNLOCK_BLOCK:
                    unLockBlock(msg);
                    break;

                case PUSH_TRAIN:
                    pushTrain(msg);
                    dispatcher.dispatch(msg);
                    break;

            }
        } catch(SQLException e) {
            throw new ErrorException(ErrorId.DATABASE_ERROR, e.getMessage());
        }
    }

    protected void getBlockList(Message msg)
    throws SQLException, ErrorException {
        long id = getId(msg.getData());

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
            arraylist = new ArrayList();
            while(rs.next()) {
                arraylist.add(new BlockContactData(
                    rs.getInt("Id"),
                    rs.getInt("XPos"),
                    rs.getInt("YPos"),
                    new ContactData(rs.getInt("TriggerModulAddress"), rs.getInt("TriggerModulContactNumber")),
                    new ContactData(rs.getInt("BlockModulAddress"), rs.getInt("BlockModulContactNumber")),
                    rs.getInt("TrainId")
                ));
            }
            dispatcher.dispatch(new Message(ControlMessage.GET_BLOCK_LIST_RES, arraylist), msg.getEndpoint());
        }
    }

    protected void saveBlockList(Message msg)
    throws SQLException, ErrorException {
/*
        Map<String, Object> map = (Map<String, Object>)msg.getData();
        long id = getId(map.get("id"));

        / *
        if(!lock.isLockedByApp(msg.getEndpoint().getAppId(), id)) {
            throw new ErrorException(ErrorId.DATASET_NOT_LOCKED, "layout <" + String.valueOf(id) + "> not locked");
        }
        * /

        Connection con = database.getConnection();

        String stmt = "UPDATE `TrackLayouts` SET `ModificationDate` = NOW() WHERE `Id` = ? ";

        try (PreparedStatement pstmt = con.prepareStatement(stmt)) {
            pstmt.setLong(1, id);
            getLogger().log(Level.INFO, pstmt.toString());
            if(pstmt.executeUpdate() == 0) {
                throw new ErrorException(ErrorId.DATASET_MISSING, "could not save <" + String.valueOf(id) + ">");
            }
        }

        ArrayList<Object> arrayList = (ArrayList<Object>)map.get("symbols");

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

        for(Object item : arrayList) {
            Map<String, Object> symbol = (Map<String, Object>)item;

            stmt =
                "INSERT INTO `BlockSections` (`Id`, `TrackLayoutId`, `XPos`, `YPos`, `Symbol`) " +
                "VALUES (?, ?, ?, ?, ?)";




Integer	id
Integer	xPos
Integer	yPos
ContactData	brakeTriggerContact
ContactData	blockContact
Integer	trainId








            try(PreparedStatement pstmt = con.prepareStatement(stmt)) {
                if(symbol.get("id") == null) {
                    pstmt.setNull(1, java.sql.Types.INTEGER);
                } else {
                    pstmt.setLong(1, (long)symbol.get("id"));
                }

                pstmt.setLong(2, id);
                pstmt.setLong(3, (long)symbol.get("xPos"));
                pstmt.setLong(4, (long)symbol.get("yPos"));
                pstmt.setLong(5, (long)symbol.get("symbol"));
                getLogger().log(Level.INFO, pstmt.toString());
                pstmt.executeUpdate();
            }
        }

        dispatcher.dispatch(new Message(LayoutMessage.LAYOUT_CHANGED, map));





        String q =
                "";



*/

    }

    protected void getSwitchStateList(Message msg)
    throws SQLException, ErrorException {
        long id = getId(msg.getData());

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
            arraylist = new ArrayList();
            while(rs.next()) {
                arraylist.add(new SwitchStateData(
                    rs.getInt("Id"),
                    rs.getInt("XPos"),
                    rs.getInt("YPos"),
                    SwitchStand.valueOf(rs.getString("SwitchStand"))
                ));
            }
            dispatcher.dispatch(new Message(ControlMessage.GET_SWITCH_STAND_LIST_RES, arraylist), msg.getEndpoint());
        }
    }

    protected void getTrainList(Message msg)
    throws SQLException, ErrorException {
        long id = getId(msg.getData());

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
            arraylist = new ArrayList();
            while(rs.next()) {
                arraylist.add(new TrainData(
                    rs.getInt("Id"),
                    rs.getInt("Address"),
                    rs.getInt("Speed"),
                    DrivingDirection.valueOf(rs.getString("DrivingDirection"))
                ));
            }
            dispatcher.dispatch(new Message(ControlMessage.GET_TRAIN_LIST_RES, arraylist), msg.getEndpoint());
        }
    }

    protected void pushTrain(Message msg)
    throws SQLException, ErrorException {
        Connection con = database.getConnection();

/*
        var train = (Map<String, Object>)msg.getData();
        var trainId = (long)train.get("id");

        // FIXME: Transaction
        String q =
            "UPDATE `BlockSections` SET `BlockSections`.`TrainId` = NULL " +
            "WHERE `BlockSections`.`TrainId` = ?";

        try (PreparedStatement pstmt = con.prepareStatement(q)) {
            pstmt.setLong(1, trainId);
            if(pstmt.executeUpdate() == 0) {
                throw new ErrorException(ErrorId.DATASET_MISSING, "could not update <" + String.valueOf(trainId) + ">");
            }
        }

        q =
            "UPDATE `BlockSections` SET `BlockSections`.`TrainId` = ? " +
            "WHERE `BlockSections`.`Id` = ?";

        try (PreparedStatement pstmt = con.prepareStatement(q)) {
            pstmt.setLong(1, trainId);
            if(pstmt.executeUpdate() == 0) {
                throw new ErrorException(ErrorId.DATASET_MISSING, "could not update <" + String.valueOf(trainId) + ">");
            }
        }

        q =
            "UPDATE `Trains` SET `Trains`.`DrivingDirection` = ? " +
            "WHERE `Trains`.`Id` = ?";

        try (PreparedStatement pstmt = con.prepareStatement(q)) {
            pstmt.setString(1, (String)train.get("direction"));
            pstmt.setLong(2, trainId);
            if(pstmt.executeUpdate() == 0) {
                throw new ErrorException(ErrorId.DATASET_MISSING, "could not update <" + String.valueOf(trainId) + ">");
            }
        }
*/
    }

    protected void lockBlock(Message msg, boolean wait)
    throws ErrorException {
        try {
            blockLock.tryLock(msg.getEndpoint().getAppId(), msg.getData());
            dispatcher.dispatch(new Message(ControlMessage.BLOCK_LOCKED, msg.getData()), msg.getEndpoint());
        } catch(ErrorException ex) {
            if(!wait) {
                dispatcher.dispatch(new Message(ControlMessage.BLOCK_LOCKING_FAILED, msg.getData()), msg.getEndpoint());
                return;
            }
            queue.add(msg);
        }
    }

    protected void unLockBlock(Message msg)
    throws ErrorException {
        blockLock.unlock(msg.getEndpoint().getAppId(), msg.getData());

        if(!queue.isEmpty()) {
            lockBlock(queue.remove(), true);
        }
    }

    protected long getId(Object o)
    throws ErrorException {
        if(o != null) {
            return (long)o;
        }
        if(activeLayout >= 0) {
            return activeLayout;
        }
        throw new ErrorException(ErrorId.NO_DEFAULT_GIVEN, "no default-tracklayout given");
    }
}
