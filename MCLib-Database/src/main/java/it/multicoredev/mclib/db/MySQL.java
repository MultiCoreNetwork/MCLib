package it.multicoredev.mclib.db;

import it.multicoredev.mclib.db.connectors.HikariConnector;
import it.multicoredev.mclib.db.connectors.MySqlConnector;
import it.multicoredev.mclib.db.connectors.PoolSettings;
import org.jetbrains.annotations.NotNull;

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
public class MySQL extends SQLImplementation {
    private String host;
    private int port;
    private String database;
    private String user;

    public MySQL(@NotNull String host, int port, @NotNull String database, @NotNull String user, @NotNull String password, boolean pool, @NotNull PoolSettings poolSettings) {
        super(pool ? new HikariConnector(host, port, database, user, password, poolSettings) : new MySqlConnector(host, port, database, user, password));

        this.host = host;
        this.port = port;
        this.database = database;
        this.user = user;
    }

    public MySQL(@NotNull String host, int port, @NotNull String database, @NotNull String user, @NotNull String password, boolean usePool) {
        this(host, port, database, user, password, usePool, new PoolSettings());
    }

    public MySQL(@NotNull String host, int port, @NotNull String database, @NotNull String user, @NotNull String password) {
        this(host, port, database, user, password, true, new PoolSettings());
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getDatabase() {
        return database;
    }

    public String getUser() {
        return user;
    }

    public static class Builder {
        private String host;
        private int port = -1;
        private String database;
        private String user;
        private String password;
        private boolean pool = true;
        private PoolSettings poolSettings;

        public String getHost() {
            return host;
        }

        public Builder setHost(String host) {
            this.host = host;
            return this;
        }

        public int getPort() {
            return port;
        }

        public Builder setPort(int port) {
            this.port = port;
            return this;
        }

        public String getDatabase() {
            return database;
        }

        public Builder setDatabase(String database) {
            this.database = database;
            return this;
        }

        public String getUser() {
            return user;
        }

        public Builder setUser(String user) {
            this.user = user;
            return this;
        }

        public String getPassword() {
            return password;
        }

        public Builder setPassword(String password) {
            this.password = password;
            return this;
        }

        public boolean isPool() {
            return pool;
        }

        public Builder setPool(boolean pool) {
            this.pool = pool;
            return this;
        }

        public boolean usePool() {
            return pool;
        }

        public Builder usePool(boolean pool) {
            this.pool = pool;
            return this;
        }

        public PoolSettings getPoolSettings() {
            return poolSettings;
        }

        public Builder setPoolSettings(PoolSettings poolSettings) {
            this.poolSettings = poolSettings;
            return this;
        }

        public MySQL build() {
            if (host == null || host.trim().isEmpty() ||
                    port == -1 ||
                    database == null || database.trim().isEmpty() ||
                    user == null || user.trim().isEmpty() ||
                    password == null || password.trim().isEmpty())
                throw new IllegalArgumentException("Some required parameters are missing.");

            return new MySQL(host, port, database, user, password, pool, poolSettings);
        }
    }
}
