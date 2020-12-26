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
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import moba.server.com.Dispatcher;
import moba.server.database.Database;
import moba.server.datatypes.enumerations.ErrorId;
import moba.server.messages.Message;
import moba.server.messages.MessageHandlerA;
import moba.server.messages.messageType.BlockingMessage;
import moba.server.utilities.config.Config;
import moba.server.utilities.exceptions.ErrorException;

public class Blocking extends MessageHandlerA {
    protected static final Logger LOGGER       = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    protected Database            database     = null;
    protected Config              config       = null;
    protected long                activeLayout = 0;

    public Blocking(Dispatcher dispatcher, Database database, Config config) {
        this.dispatcher = dispatcher;
        this.database   = database;
        this.config     = config;
    }

    @Override
    public int getGroupId() {
        return BlockingMessage.GROUP_ID;
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
    public void handleMsg(Message msg) throws ErrorException {
        try {
            switch(BlockingMessage.fromId(msg.getMessageId())) {
                case GET_CONTACT_LIST_REQ:
                    getContactList(msg);
                    break;
            }
        } catch(SQLException e) {
            throw new ErrorException(ErrorId.DATABASE_ERROR, e.getMessage());
        }
    }

    protected void getContactList(Message msg)
    throws SQLException, ErrorException {
        long id = getId(msg.getData());

        Connection con = database.getConnection();

        HashMap<String, Object> map = new HashMap<>();
        map.put("id", id);

        String q =
            "SELECT `LocalId`, `Symbol` " +
            "FROM `BlockSectionData` " +
            "LEFT JOIN FeedbackContacts " +
            "ON " +
            "WHERE `Id` = ?";

        try (PreparedStatement pstmt = con.prepareStatement(q)) {
            pstmt.setLong(1, id);
            Layout.LOGGER.log(Level.INFO, pstmt.toString());


         //   brakeTriggerContact
//blockContact



            //ArrayList<TracklayoutSymbolData> arraylist;
            ResultSet rs = pstmt.executeQuery();
            //arraylist = new ArrayList();
            while(rs.next()) {
                /*
                arraylist.add(new TracklayoutSymbolData(
                    rs.getLong("Id"),
                    rs.getLong("XPos"),
                    rs.getLong("YPos"),
                    rs.getLong("Symbol")
                ));
                */
            }
            //map.put("symbols", arraylist);
            dispatcher.dispatch(new Message(BlockingMessage.GET_CONTACT_LIST_RES, map), msg.getEndpoint());
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
