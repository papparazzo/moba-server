/*
 *  common
 *
 *  Copyright (C) 2013 Stefan Paproth <pappi-@gmx.de>
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

package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;

public class Database {

    private Connection con = null;

    public enum ConnectorType {
        MYSQL_CONNECTOR
    }

    public Database(HashMap<String, Object> map)
    throws DatabaseException {

        if(map == null) {
            throw new DatabaseException("invalid connection-data");
        }

        switch(ConnectorType.valueOf((String)map.get("connectorType"))) {
            case MYSQL_CONNECTOR:
                String url = "jdbc:mysql://" + (String)map.get("host") + "/" + (String)map.get("db");
                String usr = (String)map.get("usr");
                String pwd = (String)map.get("pwd");
                this.connect(url, usr, pwd);
                break;

            default:
                throw new DatabaseException("unsuported connector-type");
        }
    }

    private void connect(String url, String usr, String pwd)
    throws DatabaseException {

        try {
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            this.con = DriverManager.getConnection(url, usr, pwd);
        } catch(ClassNotFoundException | InstantiationException | IllegalAccessException | SQLException e) {
            throw new DatabaseException("unable to connect url: <" + url + ">");
        }
    }

    public ResultSet query(String query)
    throws SQLException {
        Statement stmt = this.con.createStatement();
        return stmt.executeQuery(query);
    }

    public Connection getConnection() {
        return this.con;
    }
}