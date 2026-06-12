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

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.logging.Logger;

public final class Database implements AutoCloseable {

    private final HikariDataSource dataSource;

    public enum ConnectorType {
        MARIADB_CONNECTOR,
        MYSQL_CONNECTOR,
        SQLITE_CONNECTOR
    }

    public Database(HashMap<String, Object> map, Logger logger)
    throws SQLException {
        if(map == null) {
            throw new SQLException("invalid connection-data");
        }

        String usr = (String)map.get("usr");
        String pwd = (String)map.get("pwd");
        String url;

        // @formatter:off
        switch(ConnectorType.valueOf((String)map.get("connectorType"))) {
            case MARIADB_CONNECTOR -> url = "jdbc:mariadb://" + map.get("host") + "/" + map.get("db");
            case MYSQL_CONNECTOR   -> url = "jdbc:mysql://" + map.get("host") + "/" + map.get("db");
            case SQLITE_CONNECTOR  -> url = "jdbc:sqlite:" + map.get("db") + ".db";
            default                -> throw new SQLException("unsupported connector-type");
        }
        // @formatter:on

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(url);
        config.setUsername(usr);
        config.setPassword(pwd);
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.setMaximumPoolSize(10);
        dataSource = new HikariDataSource(config);

        logger.info("connected to database <" + map.get("db") + "> as user <" + usr + ">");
    }

    public Connection getConnection()
    throws SQLException {
        return dataSource.getConnection();
    }

    @Override
    public void close() {
        dataSource.close();
    }
}