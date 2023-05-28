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

package moba.server.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;

public class Database {

    private Connection con = null;

    public enum ConnectorType {
        MARIADB_CONNECTOR
    }

    public Database(HashMap<String, Object> map)
    throws DatabaseException {

        if(map == null) {
            throw new DatabaseException("invalid connection-data");
        }

        switch(ConnectorType.valueOf((String)map.get("connectorType"))) {
            case MARIADB_CONNECTOR:
                String url = "jdbc:mariadb://" + (String)map.get("host") + "/" + (String)map.get("db");
                String usr = (String)map.get("usr");
                String pwd = (String)map.get("pwd");
                connect(url, usr, pwd);
                break;

            default:
                throw new DatabaseException("unsuported connector-type");
        }
    }

    private void connect(String url, String usr, String pwd)
    throws DatabaseException {

        try {
            con = DriverManager.getConnection(url, usr, pwd);
        } catch(SQLException e) {
            throw new DatabaseException("unable to connect url: <" + url + ">", e);
        }
    }

    public ResultSet query(String query)
    throws SQLException {
        Statement stmt = con.createStatement();
        return stmt.executeQuery(query);
    }

    public Connection getConnection() {
        return con;
    }
}