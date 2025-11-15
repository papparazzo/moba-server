/*
 *  Project:    moba-server
 *
 *  Copyright (C) 2025 Stefan Paproth <pappi-@gmx.de>
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
 *  along with this program. If not, see <https://www.gnu.org/licenses/agpl.txt>.
 *
 */

package moba.server.actionhandler;

import moba.server.datatypes.enumerations.ClientError;
import moba.server.exceptions.ClientErrorException;
import moba.server.routing.typedefs.SwitchStateData;
import moba.server.utilities.Database;
import moba.server.utilities.logger.Loggable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Vector;
import java.util.logging.Level;

public class InterlockRoute  implements Loggable {

    public enum RouteStatus {
        BLOCKED_AND_SWITCHED,
        BLOCKED_AND_NOT_SWITCHED,
        BLOCKED_AND_NOT_SWITCHED_WAITING,
        NOT_BLOCKED
    }

    private final HashMap<Long, Boolean> routeStatusList = new HashMap<>();

    private final Database database;

    public InterlockRoute(Database database) {
        this.database = database;
    }

    public RouteStatus setRoute(long trainId, Vector<SwitchStateData> switches)
    throws SQLException {

        Boolean routeStatus = routeStatusList.get(trainId);

        if(routeStatus != null && routeStatus) {
            // Fahrstraße gesetzt und Weichen geschaltet
            routeStatusList.remove(trainId);
            return RouteStatus.BLOCKED_AND_SWITCHED;
        }

        if(routeStatus != null) {
            // Fahrstraße gesetzt und Weichen noch nicht geschaltet
            return RouteStatus.BLOCKED_AND_NOT_SWITCHED_WAITING;
        }

        Connection con = database.getConnection();
        con.setAutoCommit(false);

        String q =
            "UPDATE `SwitchDrives` " +
            "SET `TrainId` = ? " +
            "WHERE `TrainId` IS NULL AND `id` IN (" + getPlaceHolderString(switches.size()) + ")";

        try(PreparedStatement stmt = con.prepareStatement(q)) {
            stmt.setLong(1, trainId);
            int i = 1;
            for(SwitchStateData v : switches) {
                stmt.setLong(++i, v.id());
            }

            getLogger().log(Level.INFO, stmt.toString());

            if(stmt.executeUpdate() != switches.size()) {
                con.rollback();
                // Weichen bereits anderweitig geschaltet.
                return RouteStatus.NOT_BLOCKED;
            }
            con.commit();
        }
        // Fahrstraße reserviert. Warten, dass alle Weichen geschaltet sind …
        routeStatusList.put(trainId, false);
        return RouteStatus.BLOCKED_AND_NOT_SWITCHED;
    }

    /**
     * Alle Weichen wurden geschaltet. Methode wird vom Interface aufgerufen.
     */
    public void routeSet(long trainId) {
        routeStatusList.put(trainId, true);
    }

    /**
     * Eintrag entfernen, wenn keine Weichen geschaltet werden müssen. Methode wird vom TrainRunner aufgerufen.
     */
    public void removeRoute(long trainId) {
        routeStatusList.remove(trainId);
    }

    public void releaseRoute(long trainId)
    throws SQLException, ClientErrorException {

        Connection con = database.getConnection();

        String q = "UPDATE `SwitchDrives` SET `TrainId` = NULL WHERE `TrainId` = ? ";

        try(PreparedStatement stmt = con.prepareStatement(q)) {
            stmt.setLong(1, trainId);

            getLogger().log(Level.INFO, stmt.toString());

            if(stmt.executeUpdate() == 0) {
                throw new ClientErrorException(
                    ClientError.OPERATION_NOT_ALLOWED,
                    "block not set for train <" + trainId + ">"
                );
            }
        }
    }

    private String getPlaceHolderString(int size) {
        if(size < 1) {
            throw new IllegalArgumentException("size < 1");
        }

        StringBuilder builder = new StringBuilder();

        builder.append("?,".repeat(size));
        return builder.deleteCharAt(builder.length() - 1).toString();
    }
}
