package it.multicoredev.mclib.db.connectors;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.mariadb.jdbc.MariaDbDataSource;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

/**
 * Copyright © 2019-2020 by Lorenzo Magni
 * This file is part of MCLib.
 * MCLib is under "The 3-Clause BSD License", you can find a copy <a href="https://opensource.org/licenses/BSD-3-Clause">here</a>.
 * <p>
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 * <p>
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING,
 * BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY,
 * OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */
public class HikariConnector implements Connector {
    private final HikariDataSource dataSource;

    public HikariConnector(String host, int port, String database, String user, String password, String driver, PoolSettings poolSettings) {
        HikariConfig config = new HikariConfig();

        config.setDataSourceClassName(driver);
        config.addDataSourceProperty("serverName", host);
        config.addDataSourceProperty("port", port);
        config.addDataSourceProperty("databaseName", database);
        config.setUsername(user);
        config.setPassword(password);

        config.setPoolName(poolSettings.getPoolName());
        config.setMaximumPoolSize(poolSettings.getMaximumPoolSize());
        config.setMinimumIdle(poolSettings.getMinimumIdle());
        config.setInitializationFailTimeout(poolSettings.getInitializationFailTimeout());
        config.setConnectionTimeout(poolSettings.getConnectionTimeout());
        config.setIdleTimeout(poolSettings.getIdleTimeout());
        config.setMaxLifetime(poolSettings.getMaxLifetime());
        config.setLeakDetectionThreshold(poolSettings.getLeakDetectionThreshold());
        config.setConnectionTestQuery("SELECT 1");

        for (Map.Entry<String, String> property : poolSettings.getDataSourceProperties().entrySet()) {
            config.addDataSourceProperty(property.getKey(), property.getValue());
        }

        for (Map.Entry<String, String> property : poolSettings.getDataSourceProperties().entrySet()) {
            config.addHealthCheckProperty(property.getKey(), property.getValue());
        }

        dataSource = new HikariDataSource(config);
    }

    public HikariConnector(String host, int port, String database, String user, String password, PoolSettings poolSettings) {
        HikariConfig config = new HikariConfig();

        try {
            MariaDbDataSource ds = new MariaDbDataSource();
            ds.setUrl("jdbc:mysql://" + host + ":" + port + "/" + database + "?zeroDateTimeBehavior=convertToNull");
            ds.setUser(user);
            ds.setPassword(password);

            config.setDataSource(ds);
        } catch (SQLException e) {
            throw new IllegalArgumentException(e);
        }

        config.setPoolName(poolSettings.getPoolName());
        config.setMaximumPoolSize(poolSettings.getMaximumPoolSize());
        config.setMinimumIdle(poolSettings.getMinimumIdle());
        config.setInitializationFailTimeout(poolSettings.getInitializationFailTimeout());
        config.setConnectionTimeout(poolSettings.getConnectionTimeout());
        config.setIdleTimeout(poolSettings.getIdleTimeout());
        config.setMaxLifetime(poolSettings.getMaxLifetime());
        config.setLeakDetectionThreshold(poolSettings.getLeakDetectionThreshold());
        config.setConnectionTestQuery("SELECT 1");

        for (Map.Entry<String, String> property : poolSettings.getDataSourceProperties().entrySet()) {
            config.addDataSourceProperty(property.getKey(), property.getValue());
        }

        for (Map.Entry<String, String> property : poolSettings.getDataSourceProperties().entrySet()) {
            config.addHealthCheckProperty(property.getKey(), property.getValue());
        }

        dataSource = new HikariDataSource(config);
    }

    @Override
    public Connection getConnection() throws SQLException {
        if (dataSource == null) {
            throw new SQLException("Unable to get a connection from the pool. (dataSource is null)");
        }

        Connection connection = dataSource.getConnection();

        if (connection == null) {
            throw new SQLException("Unable to get a connection from the pool. (connection is null)");
        }

        return connection;
    }

    @Override
    public void shutdown() {
        if (dataSource != null) {
            dataSource.close();
        }
    }
}