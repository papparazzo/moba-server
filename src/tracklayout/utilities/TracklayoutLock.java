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

    protected static final int APP_SERVER_ID = 1;
    protected static final Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    protected Database database   = null;
    protected SenderI  dispatcher = null;

    public enum LockState {
        LOCKED_BY_OWN_APP,
        LOCKED_BY_OTHER_APP,
        UNLOCKED
    }

    public TracklayoutLock(SenderI dispatcher, Database database) {
        this.database   = database;
        this.dispatcher = dispatcher;
    }

    public boolean tryLock(long id, Endpoint ep) throws SQLException {
        if(getLockState(id, ep) == LockState.LOCKED_BY_OTHER_APP) {
//        if(isLocked(id, ep)) {
            return true;
        }
        Connection con = database.getConnection();
        String q = "UPDATE `TrackLayouts` SET `locked` = ? WHERE `locked` = NULL AND `id` = ? ";

        try(PreparedStatement pstmt = con.prepareStatement(q)) {
            pstmt.setLong(2, getAppId(ep));
            pstmt.setLong(3, id);

            TracklayoutLock.LOGGER.log(Level.INFO, pstmt.toString());

            if(pstmt.executeUpdate() == 0) {
                return false;
            }
            return true;
        }
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
                TracklayoutLock.LOGGER.log(Level.INFO, pstmt.toString());
            }
        } catch(SQLException e) {
            TracklayoutLock.LOGGER.log(Level.WARNING, e.toString());
        }
    }

    public LockState getLockState(long id, Endpoint ep)
    throws SQLException, NoSuchElementException {
        long appId = getAppId(ep);
        long lockedBy = getIdOfLockingApp(id);

        if(lockedBy == 0) {
            return LockState.UNLOCKED;
        }
        if(lockedBy == appId) {
            return LockState.LOCKED_BY_OWN_APP;
        }

        TracklayoutLock.LOGGER.log(Level.WARNING, "layout <{0}> is locked by <{1}>", new Object[]{id, lockedBy});
        dispatcher.dispatch(new Message(
            MessageType.CLIENT_ERROR,
            new ErrorData(ErrorId.DATASET_LOCKED, "layout is locked by <" + Long.toString(lockedBy) + ">"),
            ep
        ));
        return LockState.LOCKED_BY_OTHER_APP;
    }

    protected long getIdOfLockingApp(long id) throws SQLException, NoSuchElementException {
        Connection con = database.getConnection();

        String q = "SELECT `locked` FROM `TrackLayouts` WHERE `Id` = ?";

        try(PreparedStatement pstmt = con.prepareStatement(q)) {
            pstmt.setLong(1, id);
            TracklayoutLock.LOGGER.log(Level.INFO, pstmt.toString());
            ResultSet rs = pstmt.executeQuery();
            if(!rs.next()) {
                throw new NoSuchElementException("no layout found with id <" + Long.toString(id) + ">");
            }
            return rs.getLong("locked");
        }
    }

    public void unlockLayout(Message msg) throws SQLException {
        long id = (Long)msg.getData();
        if(getLockState(id, msg.getEndpoint()) != LockState.LOCKED_BY_OWN_APP) {
            return;
        }
        Connection con = database.getConnection();
        String q = "UPDATE `TrackLayouts` SET `locked` = NULL WHERE `locked` = ? AND `id` = ? ";

        try(PreparedStatement pstmt = con.prepareStatement(q)) {
            pstmt.setLong(2, getAppId(msg.getEndpoint()));
            pstmt.setLong(3, id);

            TracklayoutLock.LOGGER.log(Level.INFO, pstmt.toString());

            if(pstmt.executeUpdate() == 0) {
                dispatcher.dispatch(new Message(MessageType.CLIENT_ERROR, new ErrorData(ErrorId.DATASET_MISSING), msg.getEndpoint()));
                return;
            }
        }
        dispatcher.dispatch(new Message(MessageType.LAYOUT_LAYOUT_UNLOCKED, id));
    }

    public void lockLayout(Message msg) throws SQLException {
        long id = (Long)msg.getData();
        if(getLockState(id, msg.getEndpoint()) != LockState.UNLOCKED) {
            return;
        }
        Connection con = database.getConnection();
        String q = "UPDATE `TrackLayouts` SET `locked` = ? WHERE `locked` = NULL AND `id` = ? ";

        try(PreparedStatement pstmt = con.prepareStatement(q)) {
            pstmt.setLong(2, getAppId(msg.getEndpoint()));
            pstmt.setLong(3, id);

            TracklayoutLock.LOGGER.log(Level.INFO, pstmt.toString());

            if(pstmt.executeUpdate() == 0) {
                dispatcher.dispatch(new Message(MessageType.CLIENT_ERROR, new ErrorData(ErrorId.DATASET_MISSING), msg.getEndpoint()));
                return;
            }
        }
        dispatcher.dispatch(new Message(MessageType.LAYOUT_LAYOUT_UNLOCKED, id));
    }

    protected long getAppId(Endpoint ep) {
        if(ep == null) {
            return TracklayoutLock.APP_SERVER_ID;
        }
        return ep.getAppId();
    }
}
