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

import moba.server.datatypes.enumerations.ClientError;
import moba.server.datatypes.objects.Position;
import moba.server.datatypes.objects.TrackLayoutInfoData;
import moba.server.datatypes.objects.TrackLayoutSymbolData;
import moba.server.exceptions.ClientErrorException;
import moba.server.datatypes.objects.Symbol;
import moba.server.datatypes.collections.LayoutContainer;
import moba.server.utilities.Database;
import moba.server.utilities.logger.Loggable;

import java.sql.*;
import java.util.*;
import java.util.Date;
import java.util.logging.Level;

public class TrackLayoutRepository implements Loggable {

    protected final Database database;

    public TrackLayoutRepository(Database database) {
        this.database = database;
    }

    public ArrayList<TrackLayoutInfoData> getLayouts(long activeLayoutId)
    throws SQLException {
        String q = "SELECT * FROM `TrackLayouts`;";

        ArrayList<TrackLayoutInfoData> arraylist;

        try(Statement stmt = database.getConnection().createStatement()) {
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
                    rs.getDate("ModificationDate"),
                    rs.getDate("CreationDate")
                ));
            }
        }
        return arraylist;
    }

    public void deleteLayout(long id, long appId)
    throws SQLException, ClientErrorException {
        Connection con = database.getConnection();
        String q = "DELETE FROM `TrackLayouts` WHERE (`locked` IS NULL OR `locked` = ?) AND `id` = ? ";

        try (PreparedStatement pstmt = con.prepareStatement(q)) {
            pstmt.setLong(1, appId);
            pstmt.setLong(2, id);
            if(pstmt.executeUpdate() == 0) {
                throw new ClientErrorException(ClientError.DATASET_MISSING, "could not delete <" + id + ">");
            }
        }
    }

    public long createLayout(TrackLayoutInfoData tl, long appId)
    throws SQLException {

        Connection con = database.getConnection();

        String q =
            "INSERT INTO `TrackLayouts` (`Name`, `Description`, `CreationDate`, `ModificationDate`, `Locked`) VALUES (?, ?, NOW(), NOW(), ?)";

        try(PreparedStatement stmt = con.prepareStatement(q, PreparedStatement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, tl.getName());
            stmt.setString(2, tl.getDescription());
            stmt.setLong(3, appId);
            stmt.executeUpdate();
            getLogger().log(Level.INFO, stmt.toString());
            try(ResultSet rs = stmt.getGeneratedKeys()) {
                rs.next();
                return rs.getLong(1);
            }
        }
    }

    public void updateLayout(TrackLayoutInfoData tl, long id, long appId)
    throws SQLException, ClientErrorException {

        Connection con = database.getConnection();

        String q = "UPDATE `TrackLayouts` SET `Name` = ?, `Description` = ?, `ModificationDate` = ? WHERE (`locked` IS NULL OR `locked` = ?) AND `id` = ? ";

        try (PreparedStatement stmt = con.prepareStatement(q)) {
            stmt.setString(1, tl.getName());
            stmt.setString(2, tl.getDescription());
            stmt.setDate(3, new java.sql.Date(tl.getModified().getTime()));
            stmt.setLong(4, appId);
            stmt.setLong(5, id);
            getLogger().log(Level.INFO, stmt.toString());
            if(stmt.executeUpdate() == 0) {
                throw new ClientErrorException(ClientError.DATASET_MISSING, "could not update <" + id + ">");
            }
        }
    }

    public LayoutContainer getLayout(long id)
    throws SQLException {

        Connection con = database.getConnection();

        LayoutContainer map = new LayoutContainer();

        String q =
            "SELECT `Id`, `XPos`, `YPos`, `Symbol` " +
            "FROM `TrackLayoutSymbols` WHERE `TrackLayoutId` = ?";

        try (PreparedStatement stmt = con.prepareStatement(q)) {
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

    public void saveLayout(long id, LayoutContainer container)
    throws SQLException, ClientErrorException {

        Connection con = database.getConnection();
        // FIXME: Transaction
        String stmt = "UPDATE `TrackLayouts` SET `ModificationDate` = NOW() WHERE `Id` = ? ";

        try (PreparedStatement pstmt = con.prepareStatement(stmt)) {
            pstmt.setLong(1, id);
            getLogger().log(Level.INFO, pstmt.toString());
            if(pstmt.executeUpdate() == 0) {
                throw new ClientErrorException(ClientError.DATASET_MISSING, "could not save <" + id + ">");
            }
        }

        stmt = "DELETE FROM `TrackLayoutSymbols` WHERE `TrackLayoutId` = ?";
        try(PreparedStatement pstmt = con.prepareStatement(stmt)) {
            pstmt.setLong(1, id);
            getLogger().log(Level.INFO, pstmt.toString());
            pstmt.executeUpdate();
        }

        for (Map.Entry<Position, TrackLayoutSymbolData> entry : container.entrySet()) {
            Position key = entry.getKey();
            TrackLayoutSymbolData value = entry.getValue();

            stmt =
                "INSERT INTO `TrackLayoutSymbols` (`Id`, `TrackLayoutId`, `XPos`, `YPos`, `Symbol`) " +
                "VALUES (?, ?, ?, ?, ?)";

            try(PreparedStatement pstmt = con.prepareStatement(stmt)) {
                if(value.id() == null) {
                    pstmt.setNull(1, java.sql.Types.INTEGER);
                } else {
                    pstmt.setLong(1, value.id());
                }

                pstmt.setLong(2, id);
                pstmt.setLong(3, key.getX());
                pstmt.setLong(4, key.getY());
                pstmt.setInt(5, value.symbol().toJson());
                getLogger().log(Level.INFO, pstmt.toString());
                pstmt.executeUpdate();
            }
        }
    }

    public Date getCreationDate(long id)
    throws SQLException {
        String q = "SELECT `CreationDate` FROM `TrackLayouts` WHERE `Id` = ?;";
        Connection con = database.getConnection();

        try (PreparedStatement pstmt = con.prepareStatement(q)) {
            pstmt.setLong(1, id);
            getLogger().log(Level.INFO, pstmt.toString());
            ResultSet rs = pstmt.executeQuery();
            if(!rs.next()) {
                throw new NoSuchElementException(String.format("no elements found for layout <%4d>", id));
            }
            return rs.getDate("CreationDate");
        }
    }
}
