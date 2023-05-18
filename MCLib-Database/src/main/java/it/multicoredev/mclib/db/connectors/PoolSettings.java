package it.multicoredev.mclib.db.connectors;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

/**
 * Copyright © 2019-2023 by Lorenzo Magni
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
public class PoolSettings {
    private String poolName = "mclib-hikari";
    private int maximumPoolSize = 10;
    private int minimumIdle = 5;
    private long initializationFailTimeout = 10000;
    private long connectionTimeout = 30000;
    private long idleTimeout = 600000;
    private long maxLifetime = 1800000;
    private long leakDetectionThreshold = 0;
    private HashMap<String, String> dataSourceProperties = new HashMap<>();
    private HashMap<String, String> healthCheckProperties = new HashMap<>();
    
    public PoolSettings() {
        dataSourceProperties.put("cachePrepStmts", "true");
        dataSourceProperties.put("alwaysSendSetIsolation", "false");
        dataSourceProperties.put("cacheServerConfiguration", "true");
        dataSourceProperties.put("elideSetAutoCommits", "true");
        dataSourceProperties.put("maintainTimeStats", "false");
        dataSourceProperties.put("useLocalSessionState", "true");
        dataSourceProperties.put("useServerPrepStmts", "true");
        dataSourceProperties.put("prepStmtCacheSize", "500");
        dataSourceProperties.put("rewriteBatchedStatements", "true");
        dataSourceProperties.put("prepStmtCacheSqlLimit", "2048");
        dataSourceProperties.put("cacheCallableStmts", "true");
        dataSourceProperties.put("cacheResultSetMetadata", "true");
        dataSourceProperties.put("characterEncoding", "utf8");
        dataSourceProperties.put("zeroDateTimeBehavior", "CONVERT_TO_NULL");
    }

    public String getPoolName() {
        return poolName;
    }

    public PoolSettings setPoolName(String poolName) {
        this.poolName = poolName;
        return this;
    }

    public int getMaximumPoolSize() {
        return maximumPoolSize;
    }

    public PoolSettings setMaximumPoolSize(int maximumPoolSize) {
        this.maximumPoolSize = maximumPoolSize;
        return this;
    }

    public int getMinimumIdle() {
        return minimumIdle;
    }

    public PoolSettings setMinimumIdle(int minimumIdle) {
        this.minimumIdle = minimumIdle;
        return this;
    }

    public long getInitializationFailTimeout() {
        return initializationFailTimeout;
    }

    public PoolSettings setInitializationFailTimeout(long initializationFailTimeout) {
        this.initializationFailTimeout = initializationFailTimeout;
        return this;
    }

    public long getConnectionTimeout() {
        return connectionTimeout;
    }

    public PoolSettings setConnectionTimeout(long connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
        return this;
    }

    public long getIdleTimeout() {
        return idleTimeout;
    }

    public PoolSettings setIdleTimeout(long idleTimeout) {
        this.idleTimeout = idleTimeout;
        return this;
    }

    public long getMaxLifetime() {
        return maxLifetime;
    }

    public PoolSettings setMaxLifetime(long maxLifetime) {
        this.maxLifetime = maxLifetime;
        return this;
    }

    public long getLeakDetectionThreshold() {
        return leakDetectionThreshold;
    }

    public PoolSettings setLeakDetectionThreshold(long leakDetectionThreshold) {
        this.leakDetectionThreshold = leakDetectionThreshold;
        return this;
    }

    public HashMap<String, String> getDataSourceProperties() {
        return dataSourceProperties;
    }

    public PoolSettings setDataSourceProperty(@NotNull String key, @NotNull String value) {
        dataSourceProperties.put(key, value);
        return this;
    }

    public PoolSettings removeDataSourceProperty(@NotNull String key) {
        dataSourceProperties.remove(key);
        return this;
    }

    public HashMap<String, String> getHealthCheckProperties() {
        return healthCheckProperties;
    }

    public PoolSettings setHealthCheckProperty(@NotNull String key, @NotNull String value) {
        healthCheckProperties.put(key, value);
        return this;
    }

    public PoolSettings removeHealthCheckProperty(@NotNull String key) {
        healthCheckProperties.remove(key);
        return this;
    }
}
