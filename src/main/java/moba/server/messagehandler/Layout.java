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
 *  along with this program. If not, see <http://www.gnu.org/licenses/agpl.txt>.
 *
 */

package moba.server.messagehandler;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;

import moba.server.database.Database;
import moba.server.datatypes.enumerations.ErrorId;
import moba.server.datatypes.objects.TrackLayoutInfoData;
import moba.server.datatypes.objects.TracklayoutSymbolData;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import moba.server.com.Dispatcher;
import moba.server.datatypes.objects.helper.ActiveLayout;
import moba.server.messages.Message;
import moba.server.messages.MessageHandlerA;
import moba.server.messages.messageType.LayoutMessage;
import moba.server.utilities.lock.TracklayoutLock;
import moba.server.utilities.exceptions.ErrorException;
import moba.server.utilities.logger.Loggable;

public class Layout extends MessageHandlerA implements Loggable {
    protected Database        database     = null;
    protected TracklayoutLock lock         = null;
    protected ActiveLayout    activeLayout = null;

    public Layout(Dispatcher dispatcher, Database database, ActiveLayout activeLayout) {
        this.database     = database;
        this.dispatcher   = dispatcher;
        this.activeLayout = activeLayout;
        this.lock         = new TracklayoutLock(database);
        this.lock.resetAll();
    }

    @Override
    public int getGroupId() {
        return LayoutMessage.GROUP_ID;
    }

    @Override
    public void shutdown() {
        lock.resetAll();
    }

    @Override
    public void freeResources(long appId) {
        lock.resetOwn(appId);
    }

    @Override
    public void handleMsg(Message msg)
    throws ErrorException {
        try {
            switch(LayoutMessage.fromId(msg.getMessageId())) {
                case GET_LAYOUTS_REQ:
                    getLayouts(msg);
                    break;

                case GET_LAYOUT_REQ:
                    getLayout(msg, true);
                    break;

                case GET_LAYOUT_READ_ONLY_REQ:
                    getLayout(msg, false);
                    break;

                case DELETE_LAYOUT:
                    deleteLayout(msg);
                    break;

                case CREATE_LAYOUT:
                    createLayout(msg);
                    break;

                case UPDATE_LAYOUT:
                    updateLayout(msg);
                    break;

                case UNLOCK_LAYOUT:
                    unlockLayout(msg);
                    break;

                case LOCK_LAYOUT:
                    lockLayout(msg);
                    break;

                case SAVE_LAYOUT:
                    saveLayout(msg);
                    break;

                default:
                    throw new ErrorException(ErrorId.UNKNOWN_MESSAGE_ID, "unknow msg <" + Long.toString(msg.getMessageId()) + ">.");
            }
        } catch(SQLException e) {
            throw new ErrorException(ErrorId.DATABASE_ERROR, e.getMessage());
        } catch(IOException e) {
            throw new ErrorException(ErrorId.UNKNOWN_ERROR, e.getMessage());
        }
    }

    protected void getLayouts(Message msg)
    throws SQLException {
        String q = "SELECT * FROM `TrackLayouts`;";

        ArrayList<TrackLayoutInfoData> arraylist;
        getLogger().log(Level.INFO, q);
        try(ResultSet rs = database.query(q)) {
            arraylist = new ArrayList();
            while(rs.next()) {
                long id = rs.getLong("Id");
                arraylist.add(new TrackLayoutInfoData(
                    id,
                    rs.getString("Name"),
                    rs.getString("Description"),
                    rs.getInt("Locked"),
                    (id == activeLayout.getActiveLayout()),
                    rs.getDate("ModificationDate"),
                    rs.getDate("CreationDate")
                ));
            }
        }
        dispatcher.dispatch(new Message(LayoutMessage.GET_LAYOUTS_RES, arraylist, msg.getEndpoint()));
    }

    protected void deleteLayout(Message msg)
    throws SQLException, ErrorException {
        long id = (Long)msg.getData();
        lock.isLockedByApp(msg.getEndpoint().getAppId(), id);

        Connection con = database.getConnection();
        String q = "DELETE FROM `TrackLayouts` WHERE (`locked` IS NULL OR `locked` = ?) AND `id` = ? ";

        try (PreparedStatement pstmt = con.prepareStatement(q)) {
            pstmt.setLong(1, msg.getEndpoint().getAppId());
            pstmt.setLong(2, id);
            getLogger().log(Level.INFO, "<{0}>", new Object[]{pstmt.toString()});
            if(pstmt.executeUpdate() == 0) {
                throw new ErrorException(ErrorId.DATASET_MISSING, "could not delete <" + String.valueOf(id) + ">");
            }
        }
        if(id == activeLayout.getActiveLayout()) {
            activeLayout.setActiveLayout(-1);
        }
        dispatcher.dispatch(new Message(LayoutMessage.DELETE_LAYOUT, id));
    }

    protected void createLayout(Message msg)
    throws SQLException, IOException, ErrorException {
        Map<String, Object> map = (Map)msg.getData();
        boolean isActive = (boolean)map.get("active");
        long    currAppId = msg.getEndpoint().getAppId();

        if(isActive) {
            lock.isLockedByApp(msg.getEndpoint().getAppId(), activeLayout);
        }

        TrackLayoutInfoData tl = new TrackLayoutInfoData((String)map.get("name"), (String)map.get("description"), currAppId, isActive);
        Connection con = database.getConnection();

        String q = "INSERT INTO `TrackLayouts` (`Name`, `Description`, `CreationDate`, `ModificationDate`, `Locked`) VALUES (?, ?, NOW(), NOW(), ?)";

        try(PreparedStatement pstmt = con.prepareStatement(q, PreparedStatement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, tl.getName());
            pstmt.setString(2, tl.getDescription());
            pstmt.setLong(3, currAppId);
            pstmt.executeUpdate();
            getLogger().log(Level.INFO, pstmt.toString());
            try(ResultSet rs = pstmt.getGeneratedKeys()) {
                rs.next();
                int id = rs.getInt(1);
                if(isActive) {
                    activeLayout.setActiveLayout(id);
                }
                tl.setId(id);
            }
        }
        dispatcher.dispatch(new Message(LayoutMessage.CREATE_LAYOUT, tl));
    }

    protected void updateLayout(Message msg)
    throws SQLException, ErrorException {
        Map<String, Object> map = (Map)msg.getData();

        long id = (Long)map.get("id");
        lock.isLockedByApp(msg.getEndpoint().getAppId(), id);

        TrackLayoutInfoData tl;
        boolean active = (boolean)map.get("active");
        long appId = msg.getEndpoint().getAppId();
        tl = new TrackLayoutInfoData(id, (String)map.get("name"), (String)map.get("description"), appId, active, new Date(), getCreationDate(id));

        Connection con = database.getConnection();

        String q = "UPDATE `TrackLayouts` SET `Name` = ?, `Description` = ?, `ModificationDate` = ? WHERE (`locked` IS NULL OR `locked` = ?) AND `id` = ? ";

        try (PreparedStatement pstmt = con.prepareStatement(q)) {
            pstmt.setString(1, tl.getName());
            pstmt.setString(2, tl.getDescription());
            pstmt.setDate(3, new java.sql.Date(tl.getModificationDate().getTime()));
            pstmt.setLong(4, appId);
            pstmt.setLong(5, id);
            getLogger().log(Level.INFO, pstmt.toString());
            if(pstmt.executeUpdate() == 0) {
                throw new ErrorException(ErrorId.DATASET_MISSING, "could not update <" + String.valueOf(id) + ">");
            }
            if(active) {
                activeLayout.setActiveLayout(id);
            }
            dispatcher.dispatch(new Message(LayoutMessage.UPDATE_LAYOUT, tl));
        }
    }

    protected void unlockLayout(Message msg)
    throws ErrorException {
        long id = activeLayout.getActiveLayout(msg.getData());
        lock.unlock(msg.getEndpoint().getAppId(), id);
        dispatcher.dispatch(new Message(LayoutMessage.UNLOCK_LAYOUT, id));
    }

    protected void lockLayout(Message msg)
    throws ErrorException {
        long id = activeLayout.getActiveLayout(msg.getData());
        lock.tryLock(msg.getEndpoint().getAppId(), id);
        dispatcher.dispatch(new Message(LayoutMessage.LOCK_LAYOUT, id));
    }

    protected void getLayout(Message msg, boolean tryLock)
    throws SQLException, ErrorException {
        long id = activeLayout.getActiveLayout(msg.getData());

        if(tryLock) {
            lock.tryLock(msg.getEndpoint().getAppId(), id);
        }

        Connection con = database.getConnection();

        HashMap<String, Object> map = new HashMap<>();
        map.put("id", id);

        String q = "SELECT `Id`, `XPos`, `YPos`, `Symbol` FROM `TrackLayoutSymbols` WHERE `TrackLayoutId` = ?";

        try (PreparedStatement pstmt = con.prepareStatement(q)) {
            pstmt.setLong(1, id);
            getLogger().log(Level.INFO, pstmt.toString());

            ArrayList<TracklayoutSymbolData> arraylist;
            ResultSet rs = pstmt.executeQuery();
            arraylist = new ArrayList();
            while(rs.next()) {
                arraylist.add(new TracklayoutSymbolData(
                    rs.getLong("Id"),
                    rs.getLong("XPos"),
                    rs.getLong("YPos"),
                    rs.getLong("Symbol")
                ));
            }
            map.put("symbols", arraylist);
            dispatcher.dispatch(new Message(LayoutMessage.GET_LAYOUT_RES, map, msg.getEndpoint()));
        }
    }

    protected void saveLayout(Message msg)
    throws SQLException, ErrorException {

        Map<String, Object> map = (Map<String, Object>)msg.getData();
        long id = activeLayout.getActiveLayout(map.get("id"));

        if(!lock.isLockedByApp(msg.getEndpoint().getAppId(), id)) {
            throw new ErrorException(ErrorId.DATASET_NOT_LOCKED, "layout <" + String.valueOf(id) + "> not locked");
        }

        Connection con = database.getConnection();
        // FIXME: Transaction
        String stmt = "UPDATE `TrackLayouts` SET `ModificationDate` = NOW() WHERE `Id` = ? ";

        try (PreparedStatement pstmt = con.prepareStatement(stmt)) {
            pstmt.setLong(1, id);
            getLogger().log(Level.INFO, pstmt.toString());
            if(pstmt.executeUpdate() == 0) {
                throw new ErrorException(ErrorId.DATASET_MISSING, "could not save <" + String.valueOf(id) + ">");
            }
        }

        ArrayList<Object> arrayList = (ArrayList<Object>)map.get("symbols");

        stmt = "DELETE FROM `TrackLayoutSymbols` WHERE `TrackLayoutId` = ?";
        try(PreparedStatement pstmt = con.prepareStatement(stmt)) {
            pstmt.setLong(1, id);
            getLogger().log(Level.INFO, pstmt.toString());
            pstmt.executeUpdate();
        }

        for(Object item : arrayList) {
            Map<String, Object> symbol = (Map<String, Object>)item;

            stmt =
                "INSERT INTO `TrackLayoutSymbols` (`Id`, `TrackLayoutId`, `XPos`, `YPos`, `Symbol`) " +
                "VALUES (?, ?, ?, ?, ?)";

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

        dispatcher.dispatch(new Message(LayoutMessage.LAYOUT_CHANGED, id));
    }

    protected Date getCreationDate(long id)
    throws SQLException {
        String q = "SELECT `CreationDate` FROM `TrackLayouts` WHERE `Id` = ?;";
        Connection con = database.getConnection();

        try (PreparedStatement pstmt = con.prepareStatement(q)) {
            pstmt.setLong(1, id);
            getLogger().log(Level.INFO, pstmt.toString());
            ResultSet rs = pstmt.executeQuery();
            if(!rs.next()) {
                throw new NoSuchElementException(String.format("no elements found for layout <%4d>", id));
            }
            return rs.getDate("CreationDate");
        }
    }
}
