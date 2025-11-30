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
 *  along with this program. If not, see <https://www.gnu.org/licenses/agpl.txt>.
 *
 */

package moba.server.utilities.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.logging.Logger;

public final class Database {

    private Connection connection;

    private final String usr;
    private final String pwd;
    private final String url;

    private final Logger logger;

    public enum ConnectorType {
        MARIADB_CONNECTOR,
        MYSQL_CONNECTOR,
        SQLITE_CONNECTOR
    }

    public Database(HashMap<String, Object> map, Logger logger)
    throws SQLException {
        this.logger = logger;

        if(map == null) {
            throw new SQLException("invalid connection-data");
        }

        usr = (String)map.get("usr");
        pwd = (String)map.get("pwd");

        switch(ConnectorType.valueOf((String)map.get("connectorType"))) {
            case MARIADB_CONNECTOR -> url = "jdbc:mariadb://" + map.get("host") + "/" + map.get("db");
            case MYSQL_CONNECTOR   -> url = "jdbc:mysql://" + map.get("host") + "/" + map.get("db");
            case SQLITE_CONNECTOR  -> url = "jdbc:sqlite:" + map.get("db") + ".db";
            default                -> throw new SQLException("unsupported connector-type");
        }
        connection = DriverManager.getConnection(url, usr, pwd);
        this.logger.info("connected to database <" + map.get("db") + "> as user <" + usr + ">");
    }

    public Connection getConnection()
    throws SQLException {
        if(!connection.isValid(0)) {
            logger.warning("connection to database lost, reconnecting...");
            connection.close();
            connection = DriverManager.getConnection(url, usr, pwd);
        }
        return connection;
    }
}