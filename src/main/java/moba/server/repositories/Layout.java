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

import moba.server.routing.LayoutSymbol;
import moba.server.routing.Position;
import moba.server.routing.Symbol;
import moba.server.routing.typedefs.LayoutContainer;
import moba.server.utilities.Database;
import moba.server.utilities.exceptions.ClientErrorException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Layout {

    protected final Database database;

    public Layout(Database database) {
        this.database = database;
    }


    protected LayoutContainer getLayout()
    throws SQLException, ClientErrorException {
        long id = 10; //activeLayout.getActiveLayout(msg.getData());

        Connection con = database.getConnection();

        LayoutContainer map = new LayoutContainer();

        String q =
            "SELECT `Id`, `XPos`, `YPos`, `Symbol` " +
            "FROM `TrackLayoutSymbols` WHERE `TrackLayoutId` = ?";

        try (PreparedStatement stmt = con.prepareStatement(q)) {
            stmt.setLong(1, id);
          //  getLogger().log(Level.INFO, stmt.toString());

            ResultSet rs = stmt.executeQuery();
            while(rs.next()) {
                map.put(
                    new Position(
                        rs.getLong("XPos"),
                        rs.getLong("YPos")
                    ),
                    new LayoutSymbol(
                        rs.getLong("Id"),
                        new Symbol(rs.getByte("Symbol"))
                    )
                );
            }
        }
        return map;
    }
}
