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
import datatypes.objects.ErrorData;
import datatypes.objects.TracklayoutSymbolData;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import messages.Message;
import messages.MessageHandlerA;
import messages.MessageType;
import tracklayout.utilities.TracklayoutLock;
import utilities.config.Config;

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
    public void handleMsg(Message msg) {
        switch(msg.getMsgType()) {
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

    protected void getLayout(Message msg) {
        try {
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
        } catch(SQLException e) {
            Layout.LOGGER.log(Level.WARNING, e.toString());
            dispatcher.dispatch(new Message(
                    MessageType.CLIENT_ERROR,
                    new ErrorData(ErrorId.DATABASE_ERROR, e.getMessage()),
                    msg.getEndpoint()
                )
            );
        }
    }

    protected void saveLayout(Message msg) {
        try {
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

        } catch(SQLException e) {
            Layout.LOGGER.log(Level.WARNING, "<{0}>", new Object[]{e.toString()});
            dispatcher.dispatch(new Message(
                MessageType.CLIENT_ERROR,
                new ErrorData(ErrorId.DATABASE_ERROR, e.getMessage()),
                msg.getEndpoint()
            ));
        }
    }
}
