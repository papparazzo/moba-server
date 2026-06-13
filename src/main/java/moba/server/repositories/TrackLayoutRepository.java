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

package moba.server.repositories;

import moba.server.datatypes.base.DateTime;
import moba.server.datatypes.enumerations.ClientError;
import moba.server.datatypes.objects.Position;
import moba.server.datatypes.objects.TrackLayoutInfoData;
import moba.server.datatypes.objects.TrackLayoutSymbolData;
import moba.server.exceptions.ClientErrorException;
import moba.server.datatypes.objects.Symbol;
import moba.server.datatypes.collections.LayoutMap;
import moba.server.utilities.database.Database;

import java.sql.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class TrackLayoutRepository {

    private final Database database;

    private final Logger logger;

    public TrackLayoutRepository(Database database, Logger logger) {
        this.database = database;
        this.logger = logger;
    }

    public ArrayList<TrackLayoutInfoData> getLayouts(long activeLayoutId)
    throws SQLException {
        String q = "SELECT * FROM `TrackLayouts`;";

        ArrayList<TrackLayoutInfoData> arraylist;

        try(
            Connection con = database.getConnection();
            Statement stmt = con.createStatement()
        ) {
            ResultSet rs = stmt.executeQuery(q);
            arraylist = new ArrayList<>();
            while(rs.next()) {
                long id = rs.getLong("Id");
                arraylist.add(new TrackLayoutInfoData(
                    id,
                    rs.getString("Name"),
                    rs.getString("Description"),
                    rs.getInt("Locked"),
                    (id == activeLayoutId),
                    new DateTime(rs.getDate("ModificationDate")),
                    new DateTime(rs.getDate("CreationDate"))
                ));
            }
        }
        return arraylist;
    }

    public void deleteLayout(long id, long appId)
    throws SQLException, ClientErrorException {
        String q = "DELETE FROM `TrackLayouts` WHERE (`locked` IS NULL OR `locked` = ?) AND `id` = ? ";

        try(
            Connection con = database.getConnection();
            PreparedStatement stmt = con.prepareStatement(q)
        ) {
            stmt.setLong(1, appId);
            stmt.setLong(2, id);
            if(stmt.executeUpdate() == 0) {
                throw new ClientErrorException(ClientError.DATASET_MISSING, "could not delete <" + id + ">");
            }
        }
    }

    public long createLayout(TrackLayoutInfoData tl, long appId)
    throws SQLException {
        String q =
            "INSERT INTO `TrackLayouts` (`Name`, `Description`, `CreationDate`, `ModificationDate`, `Locked`) " +
            "VALUES (?, ?, NOW(), NOW(), ?)";

        try(
            Connection con = database.getConnection();
            PreparedStatement stmt = con.prepareStatement(q, PreparedStatement.RETURN_GENERATED_KEYS)
        ) {
            stmt.setString(1, tl.getName());
            stmt.setString(2, tl.getDescription());
            stmt.setLong(3, appId);
            stmt.executeUpdate();
            logger.log(Level.INFO, stmt.toString());
            try(ResultSet rs = stmt.getGeneratedKeys()) {
                rs.next();
                return rs.getLong(1);
            }
        }
    }

    public void updateLayout(TrackLayoutInfoData tl, long id, long appId)
    throws SQLException, ClientErrorException {
        String q =
            "UPDATE `TrackLayouts` SET `Name` = ?, `Description` = ?, `ModificationDate` = ? " +
            "WHERE (`locked` IS NULL OR `locked` = ?) AND `id` = ? ";

        try(
            Connection con = database.getConnection();
            PreparedStatement stmt = con.prepareStatement(q)
        ) {
            stmt.setString(1, tl.getName());
            stmt.setString(2, tl.getDescription());
            stmt.setDate(3, new java.sql.Date(tl.getModified().getTime()));
            stmt.setLong(4, appId);
            stmt.setLong(5, id);
            logger.log(Level.INFO, stmt.toString());
            if(stmt.executeUpdate() == 0) {
                throw new ClientErrorException(ClientError.DATASET_MISSING, "could not update <" + id + ">");
            }
        }
    }

    public LayoutMap getLayout(long id)
    throws SQLException {

        LayoutMap map = new LayoutMap();

        String q = "SELECT `Id`, `XPos`, `YPos`, `Symbol` FROM `TrackLayoutSymbols` WHERE `TrackLayoutId` = ?";

        try(
            Connection con = database.getConnection();
            PreparedStatement stmt = con.prepareStatement(q)
        ) {
            stmt.setLong(1, id);

            ResultSet rs = stmt.executeQuery();
            while(rs.next()) {
                map.put(
                    new Position(
                        rs.getLong("XPos"),
                        rs.getLong("YPos")
                    ),
                    new TrackLayoutSymbolData(
                        rs.getLong("Id"),
                        new Symbol(rs.getInt("Symbol"))
                    )
                );
            }
        }
        return map;
    }

    public void saveLayout(long id, LayoutMap container)
    throws SQLException, ClientErrorException {

        String q = "UPDATE `TrackLayouts` SET `ModificationDate` = NOW() WHERE `Id` = ? ";

        try(Connection con = database.getConnection()) {
            boolean autoCommit = con.getAutoCommit();
            con.setAutoCommit(false);
            try {
                try(PreparedStatement stmt = con.prepareStatement(q)) {
                    stmt.setLong(1, id);
                    logger.log(Level.INFO, stmt.toString());
                    if(stmt.executeUpdate() == 0) {
                        throw new ClientErrorException(ClientError.DATASET_MISSING, "could not save <" + id + ">");
                    }
                }

                q = "DELETE FROM `TrackLayoutSymbols` WHERE `TrackLayoutId` = ?";
                try(PreparedStatement pStmt = con.prepareStatement(q)) {
                    pStmt.setLong(1, id);
                    logger.log(Level.INFO, pStmt.toString());
                    pStmt.executeUpdate();
                }

                q =
                    "INSERT INTO `TrackLayoutSymbols` (`Id`, `TrackLayoutId`, `XPos`, `YPos`, `Symbol`) " +
                    "VALUES (?, ?, ?, ?, ?)";

                try(PreparedStatement pStmt = con.prepareStatement(q)) {
                    for(Map.Entry<Position, TrackLayoutSymbolData> entry : container.entrySet()) {
                        Position key = entry.getKey();
                        TrackLayoutSymbolData value = entry.getValue();

                        if(value.id() == null) {
                            pStmt.setNull(1, java.sql.Types.INTEGER);
                        } else {
                            pStmt.setLong(1, value.id());
                        }

                        pStmt.setLong(2, id);
                        pStmt.setLong(3, key.getX());
                        pStmt.setLong(4, key.getY());
                        pStmt.setInt(5, value.symbol().toJson());
                        logger.log(Level.INFO, pStmt.toString());
                        pStmt.executeUpdate();
                    }
                }
                con.commit();
            } catch(SQLException | ClientErrorException e) {
                con.rollback();
                throw e;
            } finally {
                con.setAutoCommit(autoCommit);
            }
        }
    }

    public DateTime getCreationDate(long id)
    throws SQLException {
        String q = "SELECT `CreationDate` FROM `TrackLayouts` WHERE `Id` = ?;";

        try(
            Connection con = database.getConnection();
            PreparedStatement stmt = con.prepareStatement(q)
        ) {
            stmt.setLong(1, id);
            logger.log(Level.INFO, stmt.toString());
            ResultSet rs = stmt.executeQuery();
            if(!rs.next()) {
                throw new NoSuchElementException(String.format("no elements found for layout <%4d>", id));
            }
            return new DateTime(rs.getDate("CreationDate"));
        }
    }
}
