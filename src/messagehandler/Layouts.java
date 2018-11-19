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

import com.Endpoint;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.NoSuchElementException;

import com.SenderI;
import database.Database;
import datatypes.enumerations.ErrorId;
import datatypes.objects.TracklayoutData;
import datatypes.objects.ErrorData;
import messages.Message;
import messages.MessageHandlerA;
import messages.MessageType;

public class Layouts extends MessageHandlerA {

    protected static final Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    protected Database database   = null;
    protected SenderI  dispatcher = null;

    public Layouts(SenderI dispatcher, Database database) {
        this.database   = database;
        this.dispatcher = dispatcher;
    }

    @Override
    public void handleMsg(Message msg) {
        switch(msg.getMsgType()) {
            case GET_LAYOUTS_REQ:
                getLayouts(msg);
                break;

            case DEL_LAYOUT:
                deleteLayout(msg);
                break;

            case CREATE_LAYOUT_REQ:
                createLayout(msg);
                break;

            case UPDATE_LAYOUT:
                updateLayout(msg);
                break;

            case UNLOCK_LAYOUT:
                unlockLayout(msg);
                break;

            default:
                throw new UnsupportedOperationException(
                    "unknow msg <" + msg.getMsgType().toString() + ">."
                );
        }
    }

    @Override
    public void init() {
        freeResources(-1);
    }

    @Override
    public void reset() {
        freeResources(-1);
    }

    @Override
    public void shutdown() {
        freeResources(-1);
    }

    @Override
    public void freeResources(long id) {
        try {
            Connection con = database.getConnection();

            String q = "UPDATE `TrackLayouts` SET `Locked` = NULL ";

            if(id != -1) {
                q += "WHERE `Locked` = ?";
            }

            try(PreparedStatement pstmt = con.prepareStatement(q)) {
                if(id != -1) {
                    pstmt.setLong(1, id);
                }
                pstmt.executeUpdate();
                Layouts.LOGGER.log(Level.INFO, "<{0}>", new Object[]{pstmt.toString()});
            }
        } catch(SQLException e) {
            Layouts.LOGGER.log(Level.WARNING, "<{0}>", new Object[]{e.toString()});
        }
    }

    protected void getLayouts(Message msg) {
        try {
            String q = "SELECT * FROM `TrackLayouts`;";

            ArrayList<TracklayoutData> arraylist;
            try(ResultSet rs = database.query(q)) {
                arraylist = new ArrayList();
                while(rs.next()) {
                    arraylist.add(new TracklayoutData(
                        rs.getInt("Id"),
                        rs.getString("Name"),
                        rs.getString("Description"),
                        rs.getInt("Locked"),
                        rs.getDate("ModificationDate"),
                        rs.getDate("CreationDate")
                    ));
                }
            }
            dispatcher.dispatch(new Message(MessageType.GET_LAYOUTS_RES, arraylist, msg.getEndpoint()));
        } catch(SQLException e) {
            Layouts.LOGGER.log(Level.WARNING, "<{0}>", new Object[]{e.toString()});
            dispatcher.dispatch(new Message(
                MessageType.ERROR,
                new ErrorData(ErrorId.DATABASE_ERROR, e.getMessage()),
                msg.getEndpoint()
            ));
        }
    }

    protected boolean isLocked(long id, Endpoint ep)
    throws SQLException, NoSuchElementException {
        long appId = ep.getAppId();
        long lockedBy = isLocked(id);

        if(lockedBy == 0 || lockedBy == appId) {
            return false;
        }
        Layouts.LOGGER.log(Level.WARNING, "layout <{0}> is locked", new Object[]{id});
        dispatcher.dispatch(new Message(
            MessageType.ERROR,
            new ErrorData(ErrorId.DATASET_LOCKED, "layout is locked by <" + Long.toString(lockedBy) + ">"),
            ep
        ));
        return true;
    }

    protected long isLocked(long id)
    throws SQLException, NoSuchElementException {
        Connection con = database.getConnection();

        String q = "SELECT `locked` FROM `TrackLayouts` WHERE `Id` = ?";

        try(PreparedStatement pstmt = con.prepareStatement(q)) {
            pstmt.setLong(1, id);
            ResultSet rs = pstmt.executeQuery();

            if(!rs.next()) {
                throw new NoSuchElementException("no layout found with id <" + Long.toString(id) + ">");
            }
            return rs.getLong("locked");
        }
    }

    protected void deleteLayout(Message msg) {
        try {
            long id = (Long)msg.getData();
            if(isLocked(id, msg.getEndpoint())) {
                return;
            }

            Connection con = database.getConnection();
            String q = "DELETE FROM `TrackLayouts` WHERE (`locked` = ? OR `locked` = ?) AND `id` = ? ";

            try (PreparedStatement pstmt = con.prepareStatement(q)) {
                pstmt.setLong(1, 0);
                pstmt.setLong(2, msg.getEndpoint().getAppId());
                pstmt.setLong(3, id);
                Layouts.LOGGER.log(Level.INFO, "<{0}>", new Object[]{pstmt.toString()});
                if(pstmt.executeUpdate() == 0) {
                    dispatcher.dispatch(new Message(
                        MessageType.ERROR,
                        new ErrorData(ErrorId.DATASET_MISSING, ""),
                        msg.getEndpoint()
                    ));
                    pstmt.close();
                    return;
                }
            }
            dispatcher.dispatch(new Message(MessageType.LAYOUT_DELETED, id));
        } catch(SQLException e) {
            Layouts.LOGGER.log(Level.WARNING, "<{0}>", new Object[]{e.toString()});
            dispatcher.dispatch(new Message(
                MessageType.ERROR,
                new ErrorData(ErrorId.DATABASE_ERROR, e.getMessage()),
                msg.getEndpoint()
            ));
        }
    }

    protected void createLayout(Message msg) {
        try {
            Map<String, Object> map = (Map)msg.getData();

            TracklayoutData tl = new TracklayoutData((String)map.get("name"), (String)map.get("description"));
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
                Layouts.LOGGER.log(Level.INFO, "<{0}>", new Object[]{pstmt.toString()});
                try(ResultSet rs = pstmt.getGeneratedKeys()) {
                    rs.next();
                    tl.setId(rs.getInt(1));
                }
            }
            dispatcher.dispatch(new Message(MessageType.LAYOUT_CREATED, tl));
            dispatcher.dispatch(new Message(MessageType.CREATE_LAYOUT_RES, tl.getId(), msg.getEndpoint()));
        } catch(SQLException e) {
            Layouts.LOGGER.log(Level.WARNING, "<{0}>", new Object[]{e.toString()});
            dispatcher.dispatch(new Message(
                MessageType.ERROR,
                new ErrorData(ErrorId.DATABASE_ERROR, e.getMessage()),
                msg.getEndpoint()
            ));
        }
    }

    protected void updateLayout(Message msg) {
        Map<String, Object> map = (Map)msg.getData();
        try {
            long id = (Long)map.get("id");
            if(isLocked(id, msg.getEndpoint())) {
                return;
            }
            TracklayoutData tl;
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");

            tl = new TracklayoutData(
                id,
                (String)map.get("name"),
                (String)map.get("description"),
                (int)(long)msg.getEndpoint().getAppId(),
                new java.util.Date(),
                new java.util.Date() // FIXME: Wo kriegen wir hier das richtig Datum her? Aus der Datenbank
              //  (java.util.Date)formatter.parse((String)map.get("created"))
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
                if(pstmt.executeUpdate() == 0) {
                    dispatcher.dispatch(new Message(
                        MessageType.ERROR,
                        new ErrorData(ErrorId.DATASET_MISSING, "Could not update <" + String.valueOf(id) + ">"),
                        msg.getEndpoint()
                    ));
                    pstmt.close();
                    return;
                }
                Layouts.LOGGER.log(Level.INFO, "<{0}>", new Object[]{pstmt.toString()});
                dispatcher.dispatch(new Message(MessageType.LAYOUT_UPDATED, tl));
            }
        } catch(SQLException | NumberFormatException e) {
            Layouts.LOGGER.log(Level.WARNING, "<{0}>", new Object[]{e.toString()});
            dispatcher.dispatch(
                new Message(MessageType.ERROR, new ErrorData(ErrorId.UNKNOWN_ERROR, e.getMessage()), msg.getEndpoint())
            );
        }
    }

    public void unlockLayout(Message msg) {
        try {
            long id = (Long)msg.getData();
            if(isLocked(id, msg.getEndpoint())) {
                return;
            }
            Connection con = database.getConnection();
            String q = "UPDATE `TrackLayouts` SET `locked` = ? WHERE `locked` = ? AND `id` = ? ";

            try(PreparedStatement pstmt = con.prepareStatement(q)) {
                pstmt.setLong(1, 0);
                pstmt.setLong(2, msg.getEndpoint().getAppId());
                pstmt.setLong(3, id);

                Layouts.LOGGER.log(Level.INFO, "<{0}>", new Object[]{pstmt.toString()});

                if(pstmt.executeUpdate() == 0) {
                    dispatcher.dispatch(
                        new Message(MessageType.ERROR, new ErrorData(ErrorId.DATASET_MISSING), msg.getEndpoint())
                    );
                    pstmt.close();
                    return;
                }
            }
            dispatcher.dispatch(new Message(MessageType.LAYOUT_UNLOCKED, id));
        } catch(SQLException e) {
            Layouts.LOGGER.log(Level.WARNING, "<{0}>", new Object[]{e.toString()});
            dispatcher.dispatch(
                new Message(MessageType.ERROR, new ErrorData(ErrorId.DATABASE_ERROR, e.getMessage()), msg.getEndpoint())
            );
        }
    }
}
