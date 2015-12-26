/*
 *  AppServer
 *
 *  Copyright (C) 2015 stefan
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

import java.sql.*;
import java.util.*;
import java.util.logging.*;
import java.text.*;

import com.*;
import database.*;
import messages.*;
import tracklayout.*;
import utilities.*;

public class Layout extends MessageHandlerA {

    protected static final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    protected Database db = null;
    protected SenderI  dispatcher = null;

    public Layout(SenderI dispatcher, Database db) {
        this.db = db;
        this.dispatcher = dispatcher;
    }

    @Override
    public void handleMsg(Message msg) {
        switch(msg.getMsgType()) {
            case GET_LAYOUTS_REQ:
                this.getLayouts(msg);
                break;

            case DEL_LAYOUT:
                this.deleteLayout(msg);
                break;

            case CREATE_LAYOUT_REQ:
                this.createLayout(msg);
                break;

            case UPDATE_LAYOUT:
                this.updateLayout(msg);
                break;

            case UNLOCK_LAYOUT:
                this.unlockLayout(msg);
                break;

            default:
                throw new UnsupportedOperationException(
                    "unknow msg <" + msg.getMsgType().toString() + ">."
                );
        }
    }

    @Override
    public void init() {
        this.freeResources(-1);
    }

    @Override
    public void reset() {
        this.freeResources(-1);
    }

    @Override
    public void shutdown() {
        this.freeResources(-1);
    }

    @Override
    public void freeResources(long id) {
        try {
            Connection con = this.db.getConnection();

            String q =
                "UPDATE `TrackLayout` " +
                "SET `Locked` = ? ";

            if(id != -1) {
                q += "WHERE `Locked` = ?";
            }

            try(PreparedStatement pstmt = con.prepareStatement(q)) {
                pstmt.setLong(1, 0);
                if(id != -1) {
                    pstmt.setLong(2, id);
                }
                pstmt.executeUpdate();
                Layout.logger.log(
                    Level.INFO, "<{0}>", new Object[]{pstmt.toString()}
                );
            }
        } catch(Exception e) {
            Layout.logger.log(
                Level.WARNING, "<{0}>", new Object[]{e.toString()}
            );
        }
    }

    protected void getLayouts(Message msg) {
        try {
            String q =
                "SELECT * " +
                "FROM `TrackLayout`;";

            ArrayList<TracklayoutInfo> arraylist;
            try(ResultSet rs = this.db.query(q)) {
                arraylist = new ArrayList();
                while(rs.next()) {
                    arraylist.add(new TracklayoutInfo(
                        rs.getInt("Id"),
                        rs.getString("Name"),
                        rs.getString("Description"),
                        rs.getInt("Width"),
                        rs.getInt("Height"),
                        rs.getInt("Locked"),
                        rs.getDate("ModificationDate"),
                        rs.getDate("CreationDate")
                    ));
                }
            }
            this.dispatcher.dispatch(
                new Message(
                    MessageType.GET_LAYOUTS_RES,
                    arraylist,
                    msg.getEndpoint()
                )
            );
        } catch(SQLException e) {
            Layout.logger.log(
                Level.WARNING,
                "<{0}>",
                new Object[]{e.toString()}
            );
            this.dispatcher.dispatch(
                new Message(
                    MessageType.ERROR,
                    new ErrorInfo(
                        ErrorInfo.ErrorId.DATABASE_ERROR,
                        e.getMessage()
                    ),
                    msg.getEndpoint()
                )
            );
        }
    }

    protected boolean isLocked(long id, long epid)
    throws SQLException {
        Connection con = this.db.getConnection();

        String q =
            "SELECT IF(`locked` = 0 OR `locked` = ?, 0, 1) AS `locked` " +
            "FROM `TrackLayout` " +
            "WHERE `Id` = ?";

        try(PreparedStatement pstmt = con.prepareStatement(q)) {
            pstmt.setLong(1, epid);
            pstmt.setLong(2, id);
            ResultSet rs = pstmt.executeQuery();

            if(rs.next()) {
                return rs.getBoolean("locked");
            }
            // FIXME: Wenn RS nicht existiert dann error-msg senden
            return false;
        }
    }

    protected void deleteLayout(Message msg) {
        try {
            long id = (Long)msg.getData();
            if(this.isLocked(id, msg.getEndpoint().getAppId())) {
                Layout.logger.log(
                    Level.WARNING,
                    "tracklayout <{0}> is locked",
                    new Object[]{id}
                );
                this.dispatcher.dispatch(
                    new Message(
                        MessageType.ERROR,
                        new ErrorInfo(
                            ErrorInfo.ErrorId.DATASET_LOCKED,
                            "" // FIXME: locked by...
                        ),
                        msg.getEndpoint()
                    )
                );
                return;
            }

            Connection con = this.db.getConnection();
            String q =
                "DELETE " +
                "FROM `TrackLayout` " +
                "WHERE (`locked` = ? " +
                "OR `locked` = ?) " +
                "AND `id` = ? ";

            try (PreparedStatement pstmt = con.prepareStatement(q)) {
                pstmt.setLong(1, 0);
                pstmt.setLong(2, msg.getEndpoint().getAppId());
                pstmt.setLong(3, id);
                Layout.logger.log(
                    Level.INFO,
                    "<{0}>",
                    new Object[]{pstmt.toString()}
                );
                if(pstmt.executeUpdate() == 0) {
                    this.dispatcher.dispatch(
                        new Message(
                            MessageType.ERROR,
                            new ErrorInfo(
                                ErrorInfo.ErrorId.DATASET_MISSING,
                                ""
                            ),
                            msg.getEndpoint()
                        )
                    );
                    pstmt.close();
                    return;
                }
            }
            this.dispatcher.dispatch(
                new Message(MessageType.LAYOUT_DELETED, id)
            );
        } catch(SQLException e) {
            Layout.logger.log(
                Level.WARNING,
                "<{0}>",
                new Object[]{e.toString()}
            );
            this.dispatcher.dispatch(
                new Message(
                    MessageType.ERROR,
                    new ErrorInfo(
                        ErrorInfo.ErrorId.DATABASE_ERROR,
                        e.getMessage()
                    ),
                    msg.getEndpoint()
                )
            );
        }
    }

    protected void createLayout(Message msg) {
        try {
            Map<String, Object> map = (Map)msg.getData();

            TracklayoutInfo tl = new TracklayoutInfo(
                (String)map.get("name"),
                (String)map.get("description"),
                (int)(long)map.get("width"),
                (int)(long)map.get("height")
            );

            Connection con = this.db.getConnection();

            String q =
                "INSERT INTO `TrackLayout` " +
                "(`Name`, `Description`, `CreationDate`, `ModificationDate`, " +
                "`Width`, `Height`, `Locked`) " +
                "VALUES (?, ?, NOW(), NOW(), ?, ?, ?)";

            try(
                PreparedStatement pstmt = con.prepareStatement(
                    q,
                    PreparedStatement.RETURN_GENERATED_KEYS
                 )
            ) {
                pstmt.setString(1, tl.getName());
                pstmt.setString(2, tl.getDescription());
                pstmt.setInt(3, tl.getWidth());
                pstmt.setInt(4, tl.getHeight());
                pstmt.setLong(5, msg.getEndpoint().getAppId());
                pstmt.executeUpdate();
                Layout.logger.log(Level.INFO, "<{0}>", new Object[]{pstmt.toString()});
                try(ResultSet rs = pstmt.getGeneratedKeys()) {
                    rs.next();
                    tl.setId(rs.getInt(1));
                }
            }
            this.dispatcher.dispatch(
                new Message(MessageType.LAYOUT_CREATED, tl)
            );
            this.dispatcher.dispatch(
                new Message(
                    MessageType.CREATE_LAYOUT_RES,
                    tl.getId(),
                    msg.getEndpoint()
                )
            );
        } catch(SQLException e) {
            Layout.logger.log(
                Level.WARNING,
                "<{0}>",
                new Object[]{e.toString()}
            );
            this.dispatcher.dispatch(
                new Message(
                    MessageType.ERROR,
                    new ErrorInfo(
                        ErrorInfo.ErrorId.DATABASE_ERROR,
                        e.getMessage()
                    ),
                    msg.getEndpoint()
                )
            );
        }
    }

    protected void updateLayout(Message msg) {
        Map<String, Object> map = (Map)msg.getData();
        try {
            long id = (Long)map.get("id");
            if(this.isLocked(id, msg.getEndpoint().getAppId())) {
                Layout.logger.log(
                    Level.WARNING,
                    "tracklayout <{0}> is locked",
                    new Object[]{id}
                );
                this.dispatcher.dispatch(
                    new Message(
                        MessageType.ERROR,
                        new ErrorInfo(
                            ErrorInfo.ErrorId.DATASET_LOCKED,
                            "" // FIXME: locked by...
                        ),
                        msg.getEndpoint()
                    )
                );
                return;
            }
            TracklayoutInfo tl;
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");

            tl = new TracklayoutInfo(
                id,
                (String)map.get("name"),
                (String)map.get("description"),
                (int)(long)map.get("width"),
                (int)(long)map.get("height"),
                (int)(long)msg.getEndpoint().getAppId(),
                new java.util.Date(),
                new java.util.Date() // FIXME: Wo kriegen wir hier das richtig DAtum her? Aus der Datenbank
              //  (java.util.Date)formatter.parse((String)map.get("created"))
            );

            Connection con = this.db.getConnection();

            String q =
                "UPDATE `TrackLayout` " +
                "SET `Name` = ?, `Description` = ?, `ModificationDate` = ?, " +
                "`Width` = ?, `Height` = ? " +
                "WHERE (`locked` = ? " +
                "OR `locked` = ?) " +
                "AND `id` = ? ";

            try (PreparedStatement pstmt = con.prepareStatement(q)) {
                pstmt.setString(1, tl.getName());
                pstmt.setString(2, tl.getDescription());
                pstmt.setDate(3, new java.sql.Date(tl.getModificationDate().getTime()));
                pstmt.setInt(4, tl.getWidth());
                pstmt.setInt(5, tl.getHeight());
                pstmt.setLong(6, 0);
                pstmt.setLong(7, msg.getEndpoint().getAppId());
                pstmt.setLong(8, id);
                if(pstmt.executeUpdate() == 0) {
                    this.dispatcher.dispatch(
                        new Message(
                            MessageType.ERROR,
                            new ErrorInfo(
                                ErrorInfo.ErrorId.DATASET_MISSING,
                                "Could not update <" + String.valueOf(id) + ">"
                            ),
                            msg.getEndpoint()
                        )
                    );
                    pstmt.close();
                    return;
                }
                Layout.logger.log(Level.INFO, "<{0}>", new Object[]{pstmt.toString()});
                this.dispatcher.dispatch(new Message(MessageType.LAYOUT_UPDATED, tl));
            }
        } catch(SQLException | NumberFormatException e) {
            Layout.logger.log(
                Level.WARNING,
                "<{0}>",
                new Object[]{e.toString()}
            );
            this.dispatcher.dispatch(
                new Message(
                    MessageType.ERROR,
                    new ErrorInfo(
                        ErrorInfo.ErrorId.UNKNOWN_ERROR,
                        e.getMessage()
                    ),
                    msg.getEndpoint()
                )
            );
        }
    }

    public void unlockLayout(Message msg) {
        try {
            long id = (Long)msg.getData();
            if(this.isLocked(id, msg.getEndpoint().getAppId())) {
                Layout.logger.log(
                    Level.WARNING,
                    "tracklayout <{0}> is locked",
                    new Object[]{id}
                );
                this.dispatcher.dispatch(
                    new Message(
                        MessageType.ERROR,
                        new ErrorInfo(
                            ErrorInfo.ErrorId.DATASET_LOCKED
                        ),
                        msg.getEndpoint()
                    )
                );
                return;
            }
            Connection con = this.db.getConnection();

            String q =
                "UPDATE `TrackLayout` " +
                "SET `locked` = ? " +
                "WHERE `locked` = ? " +
                "AND `id` = ? ";

            try(PreparedStatement pstmt = con.prepareStatement(q)) {
                pstmt.setLong(1, 0);
                pstmt.setLong(2, msg.getEndpoint().getAppId());
                pstmt.setLong(3, id);

                Layout.logger.log(
                    Level.INFO,
                    "<{0}>",
                    new Object[]{pstmt.toString()}
                );

                if(pstmt.executeUpdate() == 0) {
                    this.dispatcher.dispatch(
                        new Message(
                            MessageType.ERROR,
                            new ErrorInfo(
                                ErrorInfo.ErrorId.DATASET_MISSING
                            ),
                            msg.getEndpoint()
                        )
                    );
                    pstmt.close();
                    return;
                }
            }
            this.dispatcher.dispatch(
                new Message(MessageType.LAYOUT_UNLOCKED, id)
            );
        } catch(Exception e) {
            Layout.logger.log(
                Level.WARNING,
                "<{0}>",
                new Object[]{e.toString()}
            );
            this.dispatcher.dispatch(
                new Message(
                    MessageType.ERROR,
                    new ErrorInfo(
                        ErrorInfo.ErrorId.DATABASE_ERROR,
                        e.getMessage()
                    ),
                    msg.getEndpoint()
                )
            );
        }
    }
}
