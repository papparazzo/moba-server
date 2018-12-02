/*
 *  Project:    moba-server
 *
 *  Copyright (C) 2018 Stefan Paproth <pappi-@gmx.de>
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

package tracklayout.utilities;

import com.Endpoint;
import com.SenderI;
import database.Database;
import datatypes.enumerations.ErrorId;
import datatypes.objects.ErrorData;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.NoSuchElementException;
import java.util.logging.Level;
import java.util.logging.Logger;
import messages.Message;
import messages.MessageType;

public class TracklayoutLock {

    protected static final Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    protected Database database   = null;
    protected SenderI  dispatcher = null;

    public TracklayoutLock(SenderI dispatcher, Database database) {
        this.database   = database;
        this.dispatcher = dispatcher;
    }

    public void freeLocks(long id) {
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
                TracklayoutLock.LOGGER.log(Level.INFO, "<{0}>", new Object[]{pstmt.toString()});
            }
        } catch(SQLException e) {
            TracklayoutLock.LOGGER.log(Level.WARNING, "<{0}>", new Object[]{e.toString()});
        }
    }

    public boolean isLocked(long id, Endpoint ep)
    throws SQLException, NoSuchElementException {
        long appId = ep.getAppId();
        long lockedBy = isLocked(id);

        if(lockedBy == 0 || lockedBy == appId) {
            return false;
        }
        TracklayoutLock.LOGGER.log(Level.WARNING, "layout <{0}> is locked", new Object[]{id});
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

                TracklayoutLock.LOGGER.log(Level.INFO, "<{0}>", new Object[]{pstmt.toString()});

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
            TracklayoutLock.LOGGER.log(Level.WARNING, "<{0}>", new Object[]{e.toString()});
            dispatcher.dispatch(
                new Message(MessageType.ERROR, new ErrorData(ErrorId.DATABASE_ERROR, e.getMessage()), msg.getEndpoint())
            );
        }
    }
}
