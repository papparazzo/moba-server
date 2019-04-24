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
import java.util.logging.Level;
import java.util.logging.Logger;
import messages.Message;
import messages.MessageType;
import utilities.exceptions.ErrorException;

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
        Connection con = database.getConnection();
        long appId = getAppId(ep);
        // ModificationDate = NOW damit affected-Rows auch 1 zurück liefert wenn bereits durch eigene App gelocket!
        String q =
            "UPDATE `TrackLayouts` SET `locked` = ?, `ModificationDate` = NOW() " +
            "WHERE (`locked` = NULL OR `locked` = ?) AND `id` = ? ";

        try(PreparedStatement pstmt = con.prepareStatement(q)) {
            pstmt.setLong(1, appId);
            pstmt.setLong(2, appId);
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

    public void checkLockState(long id, long appId) throws SQLException, ErrorException{
        long lockedBy = getIdOfLockingApp(id);

        if(lockedBy == 0) {
            return;
        }
        if(lockedBy == appId) {
            return;
        }
        throw new ErrorException(ErrorId.DATASET_LOCKED, "layout is locked by <" + Long.toString(lockedBy) + ">");
    }

    public boolean isLockedByApp(long id, Endpoint ep) throws SQLException, ErrorException {
        long appId = getAppId(ep);
        long lockedBy = getIdOfLockingApp(id);

        TracklayoutLock.LOGGER.log(Level.INFO, "layout <{0}> is locked by <{1}>", new Object[]{id, lockedBy});

        if(lockedBy == 0) {
            return false;
        }
        if(lockedBy == appId) {
            return true;
        }
        throw new ErrorException(ErrorId.DATASET_LOCKED, "layout is locked by <" + Long.toString(lockedBy) + ">");
    }

    protected long getIdOfLockingApp(long id) throws SQLException, ErrorException {
        Connection con = database.getConnection();

        String q = "SELECT `locked` FROM `TrackLayouts` WHERE `Id` = ?";

        try(PreparedStatement pstmt = con.prepareStatement(q)) {
            pstmt.setLong(1, id);
            TracklayoutLock.LOGGER.log(Level.INFO, pstmt.toString());
            ResultSet rs = pstmt.executeQuery();
            if(!rs.next()) {
                throw new ErrorException(ErrorId.DATASET_MISSING, "no layout found with id <" + Long.toString(id) + ">");
            }
            return rs.getLong("locked");
        }
    }

    public void unlockLayout(long id, Endpoint ep) throws SQLException, ErrorException {
        if(!isLockedByApp(id, ep)) {
            return;
        }

        Connection con = database.getConnection();
        String q = "UPDATE `TrackLayouts` SET `locked` = NULL WHERE `locked` = ? AND `id` = ? ";

        try(PreparedStatement pstmt = con.prepareStatement(q)) {
            pstmt.setLong(2, getAppId(ep));
            pstmt.setLong(3, id);

            TracklayoutLock.LOGGER.log(Level.INFO, pstmt.toString());

            if(pstmt.executeUpdate() == 0) {
                throw new ErrorException(ErrorId.DATASET_MISSING, "no layout found with id <" + Long.toString(id) + ">");
            }
        }
    }

    public void lockLayout(long id, Endpoint ep) throws SQLException, ErrorException {
        if(isLockedByApp(id, ep)) {
            return;
        }
        Connection con = database.getConnection();
        String q = "UPDATE `TrackLayouts` SET `locked` = ? WHERE `locked` = NULL AND `id` = ? ";

        try(PreparedStatement pstmt = con.prepareStatement(q)) {
            pstmt.setLong(2, getAppId(ep));
            pstmt.setLong(3, id);

            TracklayoutLock.LOGGER.log(Level.INFO, pstmt.toString());

            if(pstmt.executeUpdate() == 0) {
                throw new ErrorException(ErrorId.DATASET_MISSING, "no layout found with id <" + Long.toString(id) + ">");
            }
        }
    }

    protected long getAppId(Endpoint ep) {
        if(ep == null) {
            return TracklayoutLock.APP_SERVER_ID;
        }
        return ep.getAppId();
    }
}
