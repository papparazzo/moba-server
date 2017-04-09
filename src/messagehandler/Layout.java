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
import java.util.NoSuchElementException;
import messages.Message;
import messages.MessageHandlerA;
import messages.MessageType;

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
            case GET_LAYOUT_REQ:
                this.getLayout(msg);
                break;

            default:
                throw new UnsupportedOperationException(
                    "unknow msg <" + msg.getMsgType().toString() + ">."
                );
        }
    }

    protected void getLayout(Message msg) {
        try {
            Connection con = this.db.getConnection();
            long id = (Long)msg.getData();
            HashMap<String, Object> map = new HashMap<>();

            String q =
                "SELECT `Name` " +
                "FROM `TrackLayouts` " +
                "WHERE `Id` = ?";

            try (PreparedStatement pstmt = con.prepareStatement(q)) {
                pstmt.setLong(1, id);
                Layouts.logger.log(Level.INFO, "<{0}>", new Object[]{pstmt.toString()});
                ResultSet rs = pstmt.executeQuery();
                if(!rs.next()) {
                    throw new NoSuchElementException(String.format("No layout with id <%4d>", id));
                }
                map.put("name", rs.getString("Name"));
            }

            q =
                "SELECT MAX(`XPos`) AS `Width`, MAX(`YPos`) AS `Height` " +
                "FROM `TrackLayoutSymbols` " +
                "WHERE `TrackLayoutSymbols`.`TrackLayoutId` = ?";
            try (PreparedStatement pstmt = con.prepareStatement(q)) {
                pstmt.setLong(1, id);
                Layouts.logger.log(
                    Level.INFO,
                    "<{0}>",
                    new Object[]{pstmt.toString()}
                );
                ResultSet rs = pstmt.executeQuery();
                if(!rs.next()) {
                    throw new IllegalStateException(String.format("no elements found for layout <%4d>", id));
                }
                map.put("width", rs.getLong("Width"));
                map.put("height", rs.getLong("Height"));
            }

            q =
                "SELECT * " +
                "FROM `TrackLayoutSymbols` " +
                "WHERE `TrackLayoutId` = ?";

            try (PreparedStatement pstmt = con.prepareStatement(q)) {
                pstmt.setLong(1, id);
                Layouts.logger.log(Level.INFO, "<{0}>", new Object[]{pstmt.toString()});

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

                this.dispatcher.dispatch(
                    new Message(
                        MessageType.GET_LAYOUT_RES,
                        arraylist,
                        msg.getEndpoint()
                    )
                );
            }
        } catch(SQLException e) {
            Layouts.logger.log(Level.WARNING, "<{0}>", new Object[]{e.toString()});
            this.dispatcher.dispatch(new Message(
                    MessageType.ERROR,
                    new ErrorData(
                        ErrorId.DATABASE_ERROR,
                        e.getMessage()
                    ),
                    msg.getEndpoint()
                )
            );
        }
    }
}
