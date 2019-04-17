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

package messagehandler;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.SenderI;
import database.Database;
import datatypes.enumerations.ErrorId;
import datatypes.objects.TrackLayoutInfoData;
import datatypes.objects.ErrorData;
import datatypes.objects.TracklayoutSymbolData;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import json.JSONException;
import messages.Message;
import messages.MessageHandlerA;
import messages.MessageType;
import tracklayout.utilities.TracklayoutLock;
import utilities.config.Config;
import utilities.config.ConfigException;

public class Layout extends MessageHandlerA {

    protected static final Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    protected Database  database   = null;
    protected SenderI   dispatcher = null;
    protected TracklayoutLock lock = null;
    protected Config        config = null;
    protected long     activeLayout = 0;

    public Layout(SenderI dispatcher, Database database, TracklayoutLock lock, Config config) {
        this.database   = database;
        this.dispatcher = dispatcher;
        this.lock       = lock;
        this.config     = config;
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
    public void shutdown() {
        freeResources(-1);
        try {
            storeData();
        } catch(ConfigException | IOException | JSONException e) {
            Layout.LOGGER.log(Level.WARNING, "<{0}>", new Object[]{e.toString()});
        }
    }

    @Override
    public void freeResources(long appId) {
        lock.freeLocks(appId);
    }

    @Override
    public void handleMsg(Message msg) {
        try {
            handleMsgUnsafe(msg);
        } catch(SQLException e) {
            Layout.LOGGER.log(Level.WARNING, e.toString());
            dispatcher.dispatch(new Message(
                MessageType.CLIENT_ERROR,
                new ErrorData(ErrorId.DATABASE_ERROR, e.getMessage()),
                msg.getEndpoint()
            ));
        } catch(ConfigException | IOException | JSONException e) {
            Layout.LOGGER.log(Level.WARNING, e.toString());
            dispatcher.dispatch(new Message(
                MessageType.CLIENT_ERROR,
                new ErrorData(ErrorId.UNKNOWN_ERROR, e.getMessage()),
                msg.getEndpoint()
            ));
        }
    }

    protected void handleMsgUnsafe(Message msg) throws SQLException, IOException, JSONException, ConfigException {
        switch(msg.getMsgType()) {
            case LAYOUT_GET_LAYOUTS_REQ:
                getLayouts(msg);
                break;

            case LAYOUT_DELETE_LAYOUT:
                deleteLayout(msg);
                break;

            case LAYOUT_CREATE_LAYOUT:
                createLayout(msg);
                break;

            case LAYOUT_UPDATE_LAYOUT:
                updateLayout(msg);
                break;

            case LAYOUT_UNLOCK_LAYOUT:
                lock.unlockLayout(msg);
                break;

            case LAYOUT_GET_LAYOUT_REQ:
                getLayout(msg);
                break;

            case LAYOUT_SAVE_LAYOUT:
                saveLayout(msg);
                break;

            default:
                throw new UnsupportedOperationException("unknow msg <" + msg.getMsgType().toString() + ">.");
        }
    }

    protected void getLayouts(Message msg) throws SQLException {
        String q = "SELECT * FROM `TrackLayouts`;";

        ArrayList<TrackLayoutInfoData> arraylist;
        Layout.LOGGER.log(Level.INFO, q);
        try(ResultSet rs = database.query(q)) {
            arraylist = new ArrayList();
            while(rs.next()) {
                long id = rs.getLong("Id");
                arraylist.add(new TrackLayoutInfoData(
                    id,
                    rs.getString("Name"),
                    rs.getString("Description"),
                    rs.getInt("Locked"),
                    (id == activeLayout),
                    rs.getDate("ModificationDate"),
                    rs.getDate("CreationDate")
                ));
            }
        }
        dispatcher.dispatch(new Message(MessageType.LAYOUT_GET_LAYOUTS_RES, arraylist, msg.getEndpoint()));
    }

    protected void deleteLayout(Message msg) throws SQLException, IOException, ConfigException, JSONException {
        long id = (Long)msg.getData();
        if(lock.isLocked(id, msg.getEndpoint())) {
            return;
        }

        Connection con = database.getConnection();
        String q = "DELETE FROM `TrackLayouts` WHERE (`locked` = 0 OR `locked` = ?) AND `id` = ? ";

        try (PreparedStatement pstmt = con.prepareStatement(q)) {
            pstmt.setLong(2, msg.getEndpoint().getAppId());
            pstmt.setLong(3, id);
            Layout.LOGGER.log(Level.INFO, "<{0}>", new Object[]{pstmt.toString()});
            if(pstmt.executeUpdate() == 0) {
                dispatcher.dispatch(new Message(
                    MessageType.CLIENT_ERROR,
                    new ErrorData(ErrorId.DATASET_MISSING, ""),
                    msg.getEndpoint()
                ));
                pstmt.close();
                return;
            }
        }
        if(id == activeLayout) {
            storeData(-1);
        }

        dispatcher.dispatch(new Message(MessageType.LAYOUT_LAYOUT_DELETED, id));
    }

    protected void createLayout(Message msg) throws SQLException, ConfigException, IOException, JSONException {
        Map<String, Object> map = (Map)msg.getData();

        TrackLayoutInfoData tl = new TrackLayoutInfoData((String)map.get("name"), (String)map.get("description"));
        Connection con = database.getConnection();

        String q =
            "INSERT INTO `TrackLayouts` " +
            "(`Name`, `Description`, `CreationDate`, `ModificationDate`, `Locked`) " +
            "VALUES (?, ?, NOW(), NOW(), ?)";

        try(PreparedStatement pstmt = con.prepareStatement(q, PreparedStatement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, tl.getName());
            pstmt.setString(2, tl.getDescription());
            pstmt.setLong(3, msg.getEndpoint().getAppId());
            pstmt.executeUpdate();
            Layout.LOGGER.log(Level.INFO, pstmt.toString());
            try(ResultSet rs = pstmt.getGeneratedKeys()) {
                rs.next();
                storeData(rs.getInt(1));
                tl.setId(activeLayout);
            }
        }

        dispatcher.dispatch(new Message(MessageType.LAYOUT_LAYOUT_CREATED, tl));
    }






    



    protected void updateLayout(Message msg) throws SQLException, ConfigException, IOException, JSONException {
        Map<String, Object> map = (Map)msg.getData();

        long id = (Long)map.get("id");
        if(lock.isLocked(id, msg.getEndpoint())) {
            return;
        }
        TrackLayoutInfoData tl;
        boolean active = (boolean)map.get("active");
        if(active) {
            storeData(id);
        }

        tl = new TrackLayoutInfoData(
            id,
            (String)map.get("name"),
            (String)map.get("description"),
            (int)(long)msg.getEndpoint().getAppId(),
            (boolean)map.get("active"),
            new Date(),
            getCreationDate(id)
        );

        Connection con = database.getConnection();

        String q =
            "UPDATE `TrackLayouts` " +
            "SET `Name` = ?, `Description` = ?, `ModificationDate` = ? " +
            "WHERE (`locked` = ? " +
            "OR `locked` = ?) " +
            "AND `id` = ? ";

        try (PreparedStatement pstmt = con.prepareStatement(q)) {
            pstmt.setString(1, tl.getName());
            pstmt.setString(2, tl.getDescription());
            pstmt.setDate(3, new java.sql.Date(tl.getModificationDate().getTime()));
            pstmt.setLong(6, 0);
            pstmt.setLong(7, msg.getEndpoint().getAppId());
            pstmt.setLong(8, id);
            Layout.LOGGER.log(Level.INFO, pstmt.toString());
            if(pstmt.executeUpdate() == 0) {
                dispatcher.dispatch(new Message(
                    MessageType.CLIENT_ERROR,
                    new ErrorData(ErrorId.DATASET_MISSING, "Could not update <" + String.valueOf(id) + ">"),
                    msg.getEndpoint()
                ));
                pstmt.close();
                return;
            }
            dispatcher.dispatch(new Message(MessageType.LAYOUT_LAYOUT_UPDATED, tl));
        }
    }



/*
    protected void getLayout(Message msg) {
            Connection con = database.getConnection();
            long id = activeLayout;
            Object o = msg.getData();
            if(o != null) {
                id = (Long)o;
            } else if(activeLayout < 0) {
                dispatcher.dispatch(new Message(
                    MessageType.CLIENT_ERROR,
                    new ErrorData(ErrorId.NO_DEFAULT_GIVEN, "no default-tracklayout given"),
                    msg.getEndpoint()
                ));
            }

            HashMap<String, Object> map = new HashMap<>();

            String q =
                "SELECT MAX(`XPos`) AS `Width`, MAX(`YPos`) AS `Height` " +
                "FROM `TrackLayoutSymbols` " +
                "WHERE `TrackLayoutSymbols`.`TrackLayoutId` = ?";

            try (PreparedStatement pstmt = con.prepareStatement(q)) {
                pstmt.setLong(1, id);
                Layout.LOGGER.log(Level.INFO, pstmt.toString());
                ResultSet rs = pstmt.executeQuery();
                if(!rs.next()) {
                    throw new NoSuchElementException(String.format("no elements found for layout <%4d>", id));
                }
                map.put("id", id);
                map.put("width", rs.getLong("Width"));
                map.put("height", rs.getLong("Height"));
            }

            q =
                "SELECT `Id`, `XPos`, `YPos`, `Symbol` " +
                "FROM `TrackLayoutSymbols` " +
                "WHERE `TrackLayoutId` = ?";

            try (PreparedStatement pstmt = con.prepareStatement(q)) {
                pstmt.setLong(1, id);
                Layout.LOGGER.log(Level.INFO, pstmt.toString());

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

                dispatcher.dispatch(new Message(MessageType.LAYOUT_GET_LAYOUT_RES, map, msg.getEndpoint()));
            }
    }

    protected void saveLayout(Message msg) {
            Map<String, Object> map = (Map<String, Object>)msg.getData();
            long id = activeLayout;
            Object o = map.get("id");
            if(o != null) {
                id = (Long)o;
            } else if(activeLayout < 0) {
                dispatcher.dispatch(new Message(
                    MessageType.CLIENT_ERROR,
                    new ErrorData(ErrorId.NO_DEFAULT_GIVEN, "no default-tracklayout given"),
                    msg.getEndpoint()
                ));
            }

            Connection con = database.getConnection();

            String stmt =
                "DELETE FROM `TrackLayoutSymbols` " +
                "WHERE `TrackLayoutSymbols`.`TrackLayoutId` = ?";

            try(PreparedStatement pstmt = con.prepareStatement(stmt)) {
                pstmt.setLong(1, id);
                Layout.LOGGER.log(Level.INFO, pstmt.toString());
                pstmt.executeQuery();
            }

            ArrayList<Object> arrayList = (ArrayList<Object>)map.get("symbols");

            stmt =
                "INSERT INTO `TrackLayoutSymbols` " +
                "(`TrackLayoutId`, `XPos`, `YPos`, `Symbol`) " +
                "VALUES (?, ?, ?, ?)";

            try(PreparedStatement pstmt = con.prepareStatement(stmt)) {
                con.setAutoCommit(false);

                for(Object item : arrayList) {
                    Map<String, Object> symbol = (Map<String, Object>)item;
                    pstmt.setInt(1, (int)symbol.get("id"));
                    pstmt.setInt(2, (int)symbol.get("xPos"));
                    pstmt.setInt(3, (int)symbol.get("yPos"));
                    pstmt.setInt(4, (int)symbol.get("symbol"));
                    pstmt.addBatch();
                }
                con.commit();
            }

            stmt = "UPDATE `TrackLayouts` SET `ModificationDate` = NOW() ";

            try (PreparedStatement pstmt = con.prepareStatement(stmt)) {
                Layout.LOGGER.log(Level.INFO, pstmt.toString());
                pstmt.executeUpdate();
            }

            dispatcher.dispatch(new Message(MessageType.LAYOUT_LAYOUT_CHANGED, map));
    }

*/

    protected Date getCreationDate(long id) throws SQLException {
        String q = "SELECT `CreationDate` FROM `TrackLayouts` WHERE `Id` = ?;";
        Connection con = database.getConnection();

        try (PreparedStatement pstmt = con.prepareStatement(q)) {
            pstmt.setLong(1, id);
            Layout.LOGGER.log(Level.INFO, pstmt.toString());
            ResultSet rs = pstmt.executeQuery();
            if(!rs.next()) {
                throw new NoSuchElementException(String.format("no elements found for layout <%4d>", id));
            }
            return rs.getDate("CreationDate");
        }
    }

    protected void storeData(long id)
    throws ConfigException, IOException, JSONException {
        activeLayout = id;
        storeData();
    }

    protected void storeData()
    throws ConfigException, IOException, JSONException {
        HashMap<String, Object> map = new HashMap<>();
        map.put("activeTracklayoutId", activeLayout);
        config.setSection("trackLayout", map);
        config.writeFile();
    }
}
