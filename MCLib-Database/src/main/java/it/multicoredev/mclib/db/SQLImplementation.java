package it.multicoredev.mclib.db;

import it.multicoredev.mclib.db.connectors.Connector;
import it.multicoredev.mclib.db.connectors.HikariConnector;
import org.jetbrains.annotations.NotNull;
import org.objenesis.Objenesis;
import org.objenesis.ObjenesisStd;
import org.objenesis.instantiator.ObjectInstantiator;

import java.lang.reflect.Field;
import java.sql.Date;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;

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
class SQLImplementation {
    private static final SimpleDateFormat TIMESTAMP = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss");
    protected boolean printQuery = false;
    protected Connector connector;
    private final boolean pool;

    SQLImplementation(Connector connector) {
        this.connector = connector;
        pool = connector instanceof HikariConnector;
    }

    private boolean restartConnection(Exception e) {
        return e.getMessage().contains("Connection is not available, request timed out after ") ||
                e.getMessage().contains("Too many connections") ||
                e.getMessage().contains("has been closed.");
    }

    protected String objectToString(Object obj) {
        if (obj instanceof Boolean) {
            if ((Boolean) obj) return "1";
            else return "0";
        } else if (obj instanceof java.util.Date) {
            return TIMESTAMP.format((java.util.Date) obj).replace("24:", "00:");
        } else if (obj instanceof String) {
            String str = (String) obj;
            if (str.contains("'")) str = str.replace("'", "''");
            obj = str;
        }

        return String.valueOf(obj);
    }

    protected Object castObjects(Object obj, Class<?> type) {
        if (obj == null) return null;

        if (type.getName().equals(UUID.class.getName())) {
            try {
                return UUID.fromString((String) obj);
            } catch (Exception ignored) {
                return null;
            }
        } else if (type.getName().equals(java.util.Date.class.getName())) {
            if (obj instanceof Timestamp) {
                return new Date(((Timestamp) obj).getTime());
            }
        }

        return obj;
    }

    protected HashMap<String, Object> parseObject(Object obj, boolean ignoreDefault) {
        HashMap<String, Object> map = new HashMap<>();
        Class<?> clazz = obj.getClass();

        for (Field field : clazz.getDeclaredFields()) {
            field.setAccessible(true);
            String name = field.getName();

            if (field.isAnnotationPresent(Exposed.class)) {
                Exposed annotation = field.getAnnotation(Exposed.class);
                if (!annotation.exposed()) continue;
                if (annotation.readOnly()) continue;
                if (ignoreDefault && annotation.hasDefault()) continue;
                if (!annotation.name().isEmpty()) name = annotation.name();
            }

            try {
                if (field.getType().getName().equals(java.util.Date.class.getName()) && field.get(obj) == null) {
                    map.put(name, "0000-00-00 00:00:00");
                    continue;
                }

                map.put(name, field.get(obj));
            } catch (IllegalAccessException ignored) {
            }
        }

        return map;
    }

    protected <T> T createObject(Class<T> type, CompositeResult result, int row) throws SQLException {
        if (type == null || result == null) throw new IllegalArgumentException("Arguments cannot be null");

        Objenesis objenesis = new ObjenesisStd();
        ObjectInstantiator<T> instantiator = objenesis.getInstantiatorOf(type);
        T obj = instantiator.newInstance();

        for (Field field : type.getDeclaredFields()) {
            field.setAccessible(true);
            String name = field.getName();

            if (field.isAnnotationPresent(Exposed.class)) {
                Exposed annotation = field.getAnnotation(Exposed.class);
                if (!annotation.exposed()) continue;
                if (!annotation.name().isEmpty()) name = annotation.name();
            }

            try {
                result.getResult().absolute(row);
                field.set(obj, castObjects(result.getObject(name), field.getType()));
            } catch (IllegalAccessException ignored) {
            }
        }

        return obj;
    }

    private <T> T createObject(Class<T> type, CompositeResult result) throws SQLException {
        if (type == null || result == null) throw new IllegalArgumentException("Arguments cannot be null");

        Objenesis objenesis = new ObjenesisStd();
        ObjectInstantiator<T> instantiator = objenesis.getInstantiatorOf(type);
        T obj = instantiator.newInstance();

        for (Field field : type.getDeclaredFields()) {
            field.setAccessible(true);
            String name = field.getName();

            if (field.isAnnotationPresent(Exposed.class)) {
                Exposed annotation = field.getAnnotation(Exposed.class);
                if (!annotation.exposed()) continue;
                if (!annotation.name().isEmpty()) name = annotation.name();
            }

            try {
                field.set(obj, castObjects(result.getObject(name), field.getType()));
            } catch (IllegalAccessException ignored) {
            }
        }

        return obj;
    }

    /**
     * Sets this to true to print queries.
     *
     * @param printQuery Set this to true to print queries
     */
    public void setPrintQuery(boolean printQuery) {
        this.printQuery = printQuery;
    }

    /**
     * Terminates the connection with the database.
     */
    public void shutdown() {
        connector.shutdown();
    }

    /**
     * Restart the connection with the database
     */
    public void reset() throws SQLException {
        connector.shutdown();
        connector.connect();
    }

    /**
     * Return the ping with the database.
     *
     * @return The ping in milliseconds with the database
     */
    public long ping() {
        long start = System.currentTimeMillis();
        Connection connection = null;
        PreparedStatement statement = null;

        try {
            connection = connector.connect();
            statement = connection.prepareStatement("SELECT 1");
            statement.execute();
            return System.currentTimeMillis() - start;
        } catch (SQLException ignored) {
            return -1;
        } finally {
            DBUtils.closeQuietly(statement);
            DBUtils.closeQuietly(connection);
        }
    }

    /**
     * Executes a given MySQL query.
     *
     * @param query the query to be executed
     * @param table the table to be used
     * @return the CompositeResult of the query
     * @throws SQLException SQLException
     */
    public CompositeResult executeQuery(@NotNull String query, String table) throws SQLException {
        if (query.trim().isEmpty()) throw new IllegalArgumentException("Query cannot be empty");
        query = query.replace("{table}", table);

        try {
            Connection connection = connector.connect();
            PreparedStatement statement = connection.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            ResultSet result = statement.executeQuery();

            if (printQuery) System.out.println(query);

            return new CompositeResult(connection, statement, result, query);
        } catch (SQLException e) {
            if (pool && restartConnection(e)) reset();
            throw e;
        }
    }

    /**
     * Executes an update given a MySQL query.
     *
     * @param query the query to be executed
     * @param table the table to be used
     * @throws SQLException SQLException
     */
    public void executeUpdate(@NotNull String query, String table) throws SQLException {
        if (query.trim().isEmpty()) throw new IllegalArgumentException("Query cannot be empty");
        query = query.replace("{table}", table);

        Connection connection = null;
        PreparedStatement statement = null;

        try {
            connection = connector.connect();
            statement = connection.prepareStatement(query);
            statement.executeUpdate();

            if (printQuery) System.out.println(query);
        } catch (SQLException e) {
            if (pool && restartConnection(e)) reset();
            throw e;
        } finally {
            DBUtils.closeQuietly(statement);
            DBUtils.closeQuietly(connection);
        }
    }

    /**
     * Creates a new table if it is not present in the database.
     *
     * @param args    the list of columns with their type (ex. `id` INT NOT NULL AUTO_INCREMENT PRIMARY KEY)
     * @param table   the table to create
     * @param charset the default character set
     * @throws SQLException SQLException
     */
    public void createTable(@NotNull String[] args, String table, String charset) throws SQLException {
        StringBuilder query = new StringBuilder();
        query.append("CREATE TABLE IF NOT EXISTS `")
                .append(table)
                .append("` (");
        for (int i = 0; i < args.length; i++) {
            query.append(args[i]);
            if (i != args.length - 1) query.append(", ");
        }
        if (charset == null || charset.isEmpty()) {
            query.append(");");
        } else {
            query.append(") DEFAULT CHARACTER SET ").append(charset).append(";");
        }

        executeUpdate(query.toString(), table);
    }

    /**
     * Creates a new table if it is not present in the database.
     *
     * @param args  the list of columns with their type (ex. `id` INT NOT NULL AUTO_INCREMENT PRIMARY KEY)
     * @param table the table to create
     * @throws SQLException SQLException
     */
    public void createTable(@NotNull String[] args, String table) throws SQLException {
        createTable(args, table, null);
    }

    /**
     * Adds a new row to the table assigning the given values to the given columns.
     *
     * @param columns the list of columns to edit
     * @param values  the list of values to be added to the columns
     * @param table   the table to be used
     * @throws SQLException SQLException
     */
    public void addRow(@NotNull String[] columns, Object[] values, String table) throws SQLException {
        if (columns.length != values.length)
            throw new IllegalArgumentException("Columns and values length must have the same value");

        StringBuilder query = new StringBuilder();
        query.append("INSERT INTO `")
                .append(table)
                .append("` (");
        for (int i = 0; i < columns.length; i++) {
            query.append("`")
                    .append(columns[i])
                    .append("`");
            if (i != columns.length - 1) query.append(", ");
        }
        query.append(") VALUES (");
        for (int i = 0; i < values.length; i++) {
            query.append("'")
                    .append(objectToString(values[i]))
                    .append("'");
            if (i != values.length - 1) query.append(", ");
        }
        query.append(");");

        executeUpdate(query.toString(), table);
    }

    /**
     * Adds a new row to the table assigning the given value to the given column.
     *
     * @param column the column to edit
     * @param value  the value to be added to the column
     * @param table  the table to be used
     * @throws SQLException SQLException
     */
    public void addRow(@NotNull String column, Object value, String table) throws SQLException {
        addRow(new String[]{column}, new Object[]{value}, table);
    }

    /**
     * Adds a new row to the table assigning the field values to the given column.
     * Use {@link Exposed} annotation to expose or hide fields or change corresponding names.
     *
     * @param obj   Object to be inserted
     * @param table the table to be used
     * @throws SQLException SQLException
     */
    public void addRow(@NotNull Object obj, String table) throws SQLException {
        HashMap<String, Object> map = parseObject(obj, true);
        Object[] cols = map.keySet().toArray();
        String[] columns = Arrays.copyOf(cols, cols.length, String[].class);
        Object[] values = map.values().toArray();

        addRow(columns, values, table);
    }

    /**
     * Removes a row from the table where the given columns have the given values.
     *
     * @param columns the list of columns for the research
     * @param values  the values to be searched in the columns
     * @param table   the table to be used
     * @throws SQLException SQLException
     */
    public void removeRow(@NotNull String[] columns, Object[] values, String table) throws SQLException {
        if (columns.length != values.length)
            throw new IllegalArgumentException("Columns and values length must have the same value");

        StringBuilder query = new StringBuilder();
        query.append("DELETE FROM `")
                .append(table)
                .append("` WHERE (");
        for (int i = 0; i < columns.length; i++) {
            if (values[i] == null) query.append("`").append(columns[i]).append("`").append(" IS NULL");
            else
                query.append("`").append(columns[i]).append("`").append(" = '").append(objectToString(values[i])).append("'");
            if (i != columns.length - 1) query.append(" AND ");
        }
        query.append(");");

        executeUpdate(query.toString(), table);
    }

    /**
     * Removes a row from the table where the given columns have the given values.
     *
     * @param column the column for the research
     * @param value  the value to be searched in the column
     * @param table  the table to be used
     * @throws SQLException SQLException
     */
    public void removeRow(@NotNull String column, Object value, String table) throws SQLException {
        removeRow(new String[]{column}, new Object[]{value}, table);
    }

    /**
     * Checks if a row exists with the given values in the given columns.
     *
     * @param columns the list of columns for the research
     * @param values  the list of values to be searched in the columns
     * @param table   the table to be used
     * @throws SQLException SQLException
     */
    public boolean rowExists(@NotNull String[] columns, Object[] values, String table) throws SQLException {
        if (columns.length != values.length)
            throw new IllegalArgumentException("Columns and values length must have the same value");

        StringBuilder query = new StringBuilder();
        query.append("SELECT * FROM `")
                .append(table)
                .append("` WHERE (");
        for (int i = 0; i < columns.length; i++) {
            if (values[i] == null) query.append("`")
                    .append(columns[i])
                    .append("`")
                    .append(" IS NULL");
            else query.append("`")
                    .append(columns[i])
                    .append("`")
                    .append(" = '")
                    .append(objectToString(values[i]))
                    .append("'");
            if (i != columns.length - 1) query.append(" AND ");
        }
        query.append(");");

        CompositeResult result = executeQuery(query.toString(), table);
        boolean b = result.getResult().next();
        result.close();
        return b;
    }

    /**
     * Checks if a row exists with the given value in the given column.
     *
     * @param column the column for the research
     * @param value  the value to be searched in the column
     * @param table  the table to be used
     * @throws SQLException SQLException
     */
    public boolean rowExists(@NotNull String column, Object value, String table) throws SQLException {
        return rowExists(new String[]{column}, new Object[]{value}, table);
    }

    /**
     * Checks if a row exists where the columns have the fields values.
     * Use {@link Exposed} annotation to expose or hide fields or change corresponding names.
     *
     * @param obj   Object to search
     * @param table the table to be used
     * @throws SQLException SQLException
     */
    public boolean rowExists(@NotNull Object obj, String table) throws SQLException {
        HashMap<String, Object> map = parseObject(obj, false);
        Object[] cols = map.keySet().toArray();
        String[] columns = Arrays.copyOf(cols, cols.length, String[].class);
        Object[] values = map.values().toArray();

        return rowExists(columns, values, table);
    }

    /**
     * Gets a Byte from the database.
     *
     * @param columns the list of columns for the research
     * @param values  the list of values to be searched in the columns
     * @param search  the name of the column whose value is wanted
     * @param row     if the research has more than one results, this is the number of the result you want (starts from 1)
     * @param table   the table to be used
     * @throws SQLException SQLException
     */
    public Byte getByte(@NotNull String[] columns, Object[] values, @NotNull String search, int row, String table) throws SQLException {
        if (columns.length != values.length)
            throw new IllegalArgumentException("Columns and values length must have the same value");
        if (row < 1) throw new IllegalArgumentException("Rows values starts from 1");

        StringBuilder query = new StringBuilder();
        query.append("SELECT `")
                .append(search)
                .append("` FROM `")
                .append(table)
                .append("` WHERE (");
        for (int i = 0; i < columns.length; i++) {
            if (values[i] == null) query.append("`")
                    .append(columns[i])
                    .append("`")
                    .append(" IS NULL");
            else query.append("`")
                    .append(columns[i])
                    .append("`")
                    .append(" = '")
                    .append(objectToString(values[i]))
                    .append("'");
            if (i != columns.length - 1) query.append(" AND ");
        }
        query.append(");");

        CompositeResult result = executeQuery(query.toString(), table);
        Byte b = null;
        if (result.getResult().absolute(row)) {
            b = result.getResult().getByte(search);
        }
        result.close();
        return b;
    }

    /**
     * Gets a Byte from the database.
     *
     * @param columns the list of columns for the research
     * @param values  the list of values to be searched in the columns
     * @param search  the name of the column whose value is wanted
     * @param table   the table to be used
     * @throws SQLException SQLException
     */
    public Byte getByte(@NotNull String[] columns, Object[] values, @NotNull String search, String table) throws SQLException {
        return getByte(columns, values, search, 1, table);
    }

    /**
     * Gets a Byte from the database.
     *
     * @param column the column for the research
     * @param value  the value to be searched in the column
     * @param search the name of the column whose value is wanted
     * @param row    if the research has more than one results, this is the number of the result you want (starts from 1)
     * @param table  the table to be used
     * @throws SQLException SQLException
     */
    public Byte getByte(@NotNull String column, Object value, @NotNull String search, int row, String table) throws SQLException {
        return getByte(new String[]{column}, new Object[]{value}, search, row, table);
    }

    /**
     * Gets a Byte from the database.
     *
     * @param column the column for the research
     * @param value  the value to be searched in the column
     * @param search the name of the column whose value is wanted
     * @param table  the table to be used
     * @throws SQLException SQLException
     */
    public Byte getByte(@NotNull String column, Object value, @NotNull String search, String table) throws SQLException {
        return getByte(new String[]{column}, new Object[]{value}, search, 1, table);
    }

    /**
     * Gets a Short from the database.
     *
     * @param columns the list of columns for the research
     * @param values  the list of values to be searched in the columns
     * @param search  the name of the column whose value is wanted
     * @param row     if the research has more than one results, this is the number of the result you want (starts from 1)
     * @param table   the table to be used
     * @throws SQLException SQLException
     */
    public Short getShort(@NotNull String[] columns, Object[] values, @NotNull String search, int row, String table) throws SQLException {
        if (columns.length != values.length)
            throw new IllegalArgumentException("Columns and values length must have the same value");
        if (row < 1) throw new IllegalArgumentException("Rows values starts from 1");

        StringBuilder query = new StringBuilder();
        query.append("SELECT `")
                .append(search)
                .append("` FROM `")
                .append(table)
                .append("` WHERE (");
        for (int i = 0; i < columns.length; i++) {
            if (values[i] == null) query.append("`")
                    .append(columns[i])
                    .append("`")
                    .append(" IS NULL");
            else query.append("`")
                    .append(columns[i])
                    .append("`")
                    .append(" = '")
                    .append(objectToString(values[i]))
                    .append("'");
            if (i != columns.length - 1) query.append(" AND ");
        }
        query.append(");");

        CompositeResult result = executeQuery(query.toString(), table);
        Short s = null;
        if (result.getResult().absolute(row)) {
            s = result.getResult().getShort(search);
        }
        result.close();
        return s;
    }

    /**
     * Gets a Short from the database.
     *
     * @param columns the list of columns for the research
     * @param values  the list of values to be searched in the columns
     * @param search  the name of the column whose value is wanted
     * @param table   the table to be used
     * @throws SQLException SQLException
     */
    public Short getShort(@NotNull String[] columns, Object[] values, @NotNull String search, String table) throws SQLException {
        return getShort(columns, values, search, 1, table);
    }

    /**
     * Gets a Short from the database.
     *
     * @param column the column for the research
     * @param value  the value to be searched in the column
     * @param search the name of the column whose value is wanted
     * @param row    if the research has more than one results, this is the number of the result you want (starts from 1)
     * @param table  the table to be used
     * @throws SQLException SQLException
     */
    public Short getShort(@NotNull String column, Object value, @NotNull String search, int row, String table) throws SQLException {
        return getShort(new String[]{column}, new Object[]{value}, search, row, table);
    }

    /**
     * Gets a Short from the database.
     *
     * @param column the column for the research
     * @param value  the value to be searched in the column
     * @param search the name of the column whose value is wanted
     * @param table  the table to be used
     * @throws SQLException SQLException
     */
    public Short getShort(@NotNull String column, Object value, @NotNull String search, String table) throws SQLException {
        return getShort(new String[]{column}, new Object[]{value}, search, 1, table);
    }

    /**
     * Gets a Integer from the database.
     *
     * @param columns the list of columns for the research
     * @param values  the list of values to be searched in the columns
     * @param search  the name of the column whose value is wanted
     * @param row     if the research has more than one results, this is the number of the result you want (starts from 1)
     * @param table   the table to be used
     * @throws SQLException SQLException
     */
    public Integer getInteger(@NotNull String[] columns, Object[] values, @NotNull String search, int row, String table) throws SQLException {
        if (columns.length != values.length)
            throw new IllegalArgumentException("Columns and values length must have the same value");
        if (row < 1) throw new IllegalArgumentException("Rows values starts from 1");

        StringBuilder query = new StringBuilder();
        query.append("SELECT `")
                .append(search)
                .append("` FROM `")
                .append(table)
                .append("` WHERE (");
        for (int i = 0; i < columns.length; i++) {
            if (values[i] == null) query.append("`")
                    .append(columns[i])
                    .append("`")
                    .append(" IS NULL");
            else query.append("`")
                    .append(columns[i])
                    .append("`")
                    .append(" = '")
                    .append(objectToString(values[i]))
                    .append("'");
            if (i != columns.length - 1) query.append(" AND ");
        }
        query.append(");");

        CompositeResult result = executeQuery(query.toString(), table);
        Integer i = null;
        if (result.getResult().absolute(row)) {
            i = result.getResult().getInt(search);
        }
        result.close();
        return i;
    }

    /**
     * Gets a Integer from the database.
     *
     * @param columns the list of columns for the research
     * @param values  the list of values to be searched in the columns
     * @param search  the name of the column whose value is wanted
     * @param table   the table to be used
     * @throws SQLException SQLException
     */
    public Integer getInteger(@NotNull String[] columns, Object[] values, @NotNull String search, String table) throws SQLException {
        return getInteger(columns, values, search, 1, table);
    }

    /**
     * Gets a Integer from the database.
     *
     * @param column the column for the research
     * @param value  the value to be searched in the column
     * @param search the name of the column whose value is wanted
     * @param row    if the research has more than one results, this is the number of the result you want (starts from 1)
     * @param table  the table to be used
     * @throws SQLException SQLException
     */
    public Integer getInteger(@NotNull String column, Object value, @NotNull String search, int row, String table) throws SQLException {
        return getInteger(new String[]{column}, new Object[]{value}, search, row, table);
    }

    /**
     * Gets a Integer from the database.
     *
     * @param column the column for the research
     * @param value  the value to be searched in the column
     * @param search the name of the column whose value is wanted
     * @param table  the table to be used
     * @throws SQLException SQLException
     */
    public Integer getInteger(@NotNull String column, Object value, @NotNull String search, String table) throws SQLException {
        return getInteger(new String[]{column}, new Object[]{value}, search, 1, table);
    }

    /**
     * Gets a Long from the database.
     *
     * @param columns the list of columns for the research
     * @param values  the list of values to be searched in the columns
     * @param search  the name of the column whose value is wanted
     * @param row     if the research has more than one results, this is the number of the result you want (starts from 1)
     * @param table   the table to be used
     * @throws SQLException SQLException
     */
    public Long getLong(@NotNull String[] columns, Object[] values, @NotNull String search, int row, String table) throws SQLException {
        if (columns.length != values.length)
            throw new IllegalArgumentException("Columns and values length must have the same value");
        if (row < 1) throw new IllegalArgumentException("Rows values starts from 1");

        StringBuilder query = new StringBuilder();
        query.append("SELECT `")
                .append(search)
                .append("` FROM `")
                .append(table)
                .append("` WHERE (");
        for (int i = 0; i < columns.length; i++) {
            if (values[i] == null) query.append("`")
                    .append(columns[i])
                    .append("`")
                    .append(" IS NULL");
            else query.append("`")
                    .append(columns[i])
                    .append("`")
                    .append(" = '")
                    .append(objectToString(values[i]))
                    .append("'");
            if (i != columns.length - 1) query.append(" AND ");
        }
        query.append(");");

        CompositeResult result = executeQuery(query.toString(), table);
        Long l = null;
        if (result.getResult().absolute(row)) {
            l = result.getResult().getLong(search);
        }
        result.close();
        return l;
    }

    /**
     * Gets a Long from the database.
     *
     * @param columns the list of columns for the research
     * @param values  the list of values to be searched in the columns
     * @param search  the name of the column whose value is wanted
     * @param table   the table to be used
     * @throws SQLException SQLException
     */
    public Long getLong(@NotNull String[] columns, Object[] values, @NotNull String search, String table) throws SQLException {
        return getLong(columns, values, search, 1, table);
    }

    /**
     * Gets a Long from the database.
     *
     * @param column the column for the research
     * @param value  the value to be searched in the column
     * @param search the name of the column whose value is wanted
     * @param row    if the research has more than one results, this is the number of the result you want (starts from 1)
     * @param table  the table to be used
     * @throws SQLException SQLException
     */
    public Long getLong(@NotNull String column, Object value, @NotNull String search, int row, String table) throws SQLException {
        return getLong(new String[]{column}, new Object[]{value}, search, row, table);
    }

    /**
     * Gets a Long from the database.
     *
     * @param column the column for the research
     * @param value  the value to be searched in the column
     * @param search the name of the column whose value is wanted
     * @param table  the table to be used
     * @throws SQLException SQLException
     */
    public Long getLong(@NotNull String column, Object value, @NotNull String search, String table) throws SQLException {
        return getLong(new String[]{column}, new Object[]{value}, search, 1, table);
    }

    /**
     * Gets a Float from the database.
     *
     * @param columns the list of columns for the research
     * @param values  the list of values to be searched in the columns
     * @param search  the name of the column whose value is wanted
     * @param row     if the research has more than one results, this is the number of the result you want (starts from 1)
     * @param table   the table to be used
     * @throws SQLException SQLException
     */
    public Float getFloat(@NotNull String[] columns, Object[] values, @NotNull String search, int row, String table) throws SQLException {
        if (columns.length != values.length)
            throw new IllegalArgumentException("Columns and values length must have the same value");
        if (row < 1) throw new IllegalArgumentException("Rows values starts from 1");

        StringBuilder query = new StringBuilder();
        query.append("SELECT `")
                .append(search)
                .append("` FROM `")
                .append(table)
                .append("` WHERE (");
        for (int i = 0; i < columns.length; i++) {
            if (values[i] == null) query.append("`")
                    .append(columns[i])
                    .append("`")
                    .append(" IS NULL");
            else query.append("`")
                    .append(columns[i])
                    .append("`")
                    .append(" = '")
                    .append(objectToString(values[i]))
                    .append("'");
            if (i != columns.length - 1) query.append(" AND ");
        }
        query.append(");");

        CompositeResult result = executeQuery(query.toString(), table);
        Float f = null;
        if (result.getResult().absolute(row)) {
            f = result.getResult().getFloat(search);
        }
        result.close();
        return f;
    }

    /**
     * Gets a Float from the database.
     *
     * @param columns the list of columns for the research
     * @param values  the list of values to be searched in the columns
     * @param search  the name of the column whose value is wanted
     * @param table   the table to be used
     * @throws SQLException SQLException
     */
    public Float getFloat(@NotNull String[] columns, Object[] values, @NotNull String search, String table) throws SQLException {
        return getFloat(columns, values, search, 1, table);
    }

    /**
     * Gets a Float from the database.
     *
     * @param column the column for the research
     * @param value  the value to be searched in the column
     * @param search the name of the column whose value is wanted
     * @param row    if the research has more than one results, this is the number of the result you want (starts from 1)
     * @param table  the table to be used
     * @throws SQLException SQLException
     */
    public Float getFloat(@NotNull String column, Object value, @NotNull String search, int row, String table) throws SQLException {
        return getFloat(new String[]{column}, new Object[]{value}, search, row, table);
    }

    /**
     * Gets a Float from the database.
     *
     * @param column the column for the research
     * @param value  the value to be searched in the column
     * @param search the name of the column whose value is wanted
     * @param table  the table to be used
     * @throws SQLException SQLException
     */
    public Float getFloat(@NotNull String column, Object value, @NotNull String search, String table) throws SQLException {
        return getFloat(new String[]{column}, new Object[]{value}, search, 1, table);
    }

    /**
     * Gets a Double from the database.
     *
     * @param columns the list of columns for the research
     * @param values  the list of values to be searched in the columns
     * @param search  the name of the column whose value is wanted
     * @param row     if the research has more than one results, this is the number of the result you want (starts from 1)
     * @param table   the table to be used
     * @throws SQLException SQLException
     */
    public Double getDouble(@NotNull String[] columns, Object[] values, @NotNull String search, int row, String table) throws SQLException {
        if (columns.length != values.length)
            throw new IllegalArgumentException("Columns and values length must have the same value");
        if (row < 1) throw new IllegalArgumentException("Rows values starts from 1");

        StringBuilder query = new StringBuilder();
        query.append("SELECT `")
                .append(search)
                .append("` FROM `")
                .append(table)
                .append("` WHERE (");
        for (int i = 0; i < columns.length; i++) {
            if (values[i] == null) query.append("`")
                    .append(columns[i])
                    .append("`")
                    .append(" IS NULL");
            else query.append("`")
                    .append(columns[i])
                    .append("`")
                    .append(" = '")
                    .append(objectToString(values[i]))
                    .append("'");
            if (i != columns.length - 1) query.append(" AND ");
        }
        query.append(");");

        CompositeResult result = executeQuery(query.toString(), table);
        Double d = null;
        if (result.getResult().absolute(row)) {
            d = result.getResult().getDouble(search);
        }
        result.close();
        return d;
    }

    /**
     * Gets a Double from the database.
     *
     * @param columns the list of columns for the research
     * @param values  the list of values to be searched in the columns
     * @param search  the name of the column whose value is wanted
     * @param table   the table to be used
     * @throws SQLException SQLException
     */
    public Double getDouble(@NotNull String[] columns, Object[] values, @NotNull String search, String table) throws SQLException {
        return getDouble(columns, values, search, 1, table);
    }

    /**
     * Gets a Double from the database.
     *
     * @param column the column for the research
     * @param value  the value to be searched in the column
     * @param search the name of the column whose value is wanted
     * @param row    if the research has more than one results, this is the number of the result you want (starts from 1)
     * @param table  the table to be used
     * @throws SQLException SQLException
     */
    public Double getDouble(@NotNull String column, Object value, @NotNull String search, int row, String table) throws SQLException {
        return getDouble(new String[]{column}, new Object[]{value}, search, row, table);
    }

    /**
     * Gets a Double from the database.
     *
     * @param column the column for the research
     * @param value  the value to be searched in the column
     * @param search the name of the column whose value is wanted
     * @param table  the table to be used
     * @throws SQLException SQLException
     */
    public Double getDouble(@NotNull String column, Object value, @NotNull String search, String table) throws SQLException {
        return getDouble(new String[]{column}, new Object[]{value}, search, 1, table);
    }

    /**
     * Gets a String from the database.
     *
     * @param columns the list of columns for the research
     * @param values  the list of values to be searched in the columns
     * @param search  the name of the column whose value is wanted
     * @param row     if the research has more than one results, this is the number of the result you want (starts from 1)
     * @param table   the table to be used
     * @throws SQLException SQLException
     */
    public String getString(@NotNull String[] columns, Object[] values, @NotNull String search, int row, String table) throws SQLException {
        if (columns.length != values.length)
            throw new IllegalArgumentException("Columns and values length must have the same value");
        if (row < 1) throw new IllegalArgumentException("Rows values starts from 1");

        StringBuilder query = new StringBuilder();
        query.append("SELECT `")
                .append(search)
                .append("` FROM `")
                .append(table)
                .append("` WHERE (");
        for (int i = 0; i < columns.length; i++) {
            if (values[i] == null) query.append("`")
                    .append(columns[i])
                    .append("`")
                    .append(" IS NULL");
            else query.append("`")
                    .append(columns[i])
                    .append("`")
                    .append(" = '")
                    .append(objectToString(values[i]))
                    .append("'");
            if (i != columns.length - 1) query.append(" AND ");
        }
        query.append(");");

        CompositeResult result = executeQuery(query.toString(), table);
        String s = null;
        if (result.getResult().absolute(row)) {
            s = result.getResult().getString(search);
        }
        result.close();
        return s;
    }

    /**
     * Gets a String from the database.
     *
     * @param columns the list of columns for the research
     * @param values  the list of values to be searched in the columns
     * @param search  the name of the column whose value is wanted
     * @param table   the table to be used
     * @throws SQLException SQLException
     */
    public String getString(@NotNull String[] columns, Object[] values, @NotNull String search, String table) throws SQLException {
        return getString(columns, values, search, 1, table);
    }

    /**
     * Gets a String from the database.
     *
     * @param column the column for the research
     * @param value  the value to be searched in the column
     * @param search the name of the column whose value is wanted
     * @param row    if the research has more than one results, this is the number of the result you want (starts from 1)
     * @param table  the table to be used
     * @throws SQLException SQLException
     */
    public String getString(@NotNull String column, Object value, @NotNull String search, int row, String table) throws SQLException {
        return getString(new String[]{column}, new Object[]{value}, search, row, table);
    }

    /**
     * Gets a String from the database.
     *
     * @param column the column for the research
     * @param value  the value to be searched in the column
     * @param search the name of the column whose value is wanted
     * @param table  the table to be used
     * @throws SQLException SQLException
     */
    public String getString(@NotNull String column, Object value, @NotNull String search, String table) throws SQLException {
        return getString(new String[]{column}, new Object[]{value}, search, 1, table);
    }

    /**
     * Gets a Boolean from the database.
     *
     * @param columns the list of columns for the research
     * @param values  the list of values to be searched in the columns
     * @param search  the name of the column whose value is wanted
     * @param row     if the research has more than one results, this is the number of the result you want (starts from 1)
     * @param table   the table to be used
     * @throws SQLException SQLException
     */
    public Boolean getBoolean(@NotNull String[] columns, Object[] values, @NotNull String search, int row, String table) throws SQLException {
        if (columns.length != values.length)
            throw new IllegalArgumentException("Columns and values length must have the same value");
        if (row < 1) throw new IllegalArgumentException("Rows values starts from 1");

        StringBuilder query = new StringBuilder();
        query.append("SELECT `")
                .append(search)
                .append("` FROM `")
                .append(table)
                .append("` WHERE (");
        for (int i = 0; i < columns.length; i++) {
            if (values[i] == null) query.append("`")
                    .append(columns[i])
                    .append("`")
                    .append(" IS NULL");
            else query.append("`")
                    .append(columns[i])
                    .append("`")
                    .append(" = '")
                    .append(objectToString(values[i]))
                    .append("'");
            if (i != columns.length - 1) query.append(" AND ");
        }
        query.append(");");

        CompositeResult result = executeQuery(query.toString(), table);
        Boolean b = null;
        if (result.getResult().absolute(row)) {
            b = result.getResult().getBoolean(search);
        }
        result.close();
        return b;
    }

    /**
     * Gets a Boolean from the database.
     *
     * @param columns the list of columns for the research
     * @param values  the list of values to be searched in the columns
     * @param search  the name of the column whose value is wanted
     * @param table   the table to be used
     * @throws SQLException SQLException
     */
    public Boolean getBoolean(@NotNull String[] columns, Object[] values, @NotNull String search, String table) throws SQLException {
        return getBoolean(columns, values, search, 1, table);
    }

    /**
     * Gets a Boolean from the database.
     *
     * @param column the column for the research
     * @param value  the value to be searched in the column
     * @param search the name of the column whose value is wanted
     * @param row    if the research has more than one results, this is the number of the result you want (starts from 1)
     * @param table  the table to be used
     * @throws SQLException SQLException
     */
    public Boolean getBoolean(@NotNull String column, Object value, @NotNull String search, int row, String table) throws SQLException {
        return getBoolean(new String[]{column}, new Object[]{value}, search, row, table);
    }

    /**
     * Gets a Boolean from the database.
     *
     * @param column the column for the research
     * @param value  the value to be searched in the column
     * @param search the name of the column whose value is wanted
     * @param table  the table to be used
     * @throws SQLException SQLException
     */
    public Boolean getBoolean(@NotNull String column, Object value, @NotNull String search, String table) throws SQLException {
        return getBoolean(new String[]{column}, new Object[]{value}, search, 1, table);
    }

    /**
     * Gets a Timestamp from the database.
     *
     * @param columns the list of columns for the research
     * @param values  the list of values to be searched in the columns
     * @param search  the name of the column whose value is wanted
     * @param row     if the research has more than one results, this is the number of the result you want (starts from 1)
     * @param table   the table to be used
     * @throws SQLException SQLException
     */
    public Timestamp getTimestamp(@NotNull String[] columns, Object[] values, @NotNull String search, int row, String table) throws SQLException {
        if (columns.length != values.length)
            throw new IllegalArgumentException("Columns and values length must have the same value");
        if (row < 1) throw new IllegalArgumentException("Rows values starts from 1");

        StringBuilder query = new StringBuilder();
        query.append("SELECT `")
                .append(search)
                .append("` FROM `")
                .append(table)
                .append("` WHERE (");
        for (int i = 0; i < columns.length; i++) {
            if (values[i] == null) query.append("`")
                    .append(columns[i])
                    .append("`")
                    .append(" IS NULL");
            else query.append("`")
                    .append(columns[i])
                    .append("`")
                    .append(" = '")
                    .append(objectToString(values[i]))
                    .append("'");
            if (i != columns.length - 1) query.append(" AND ");
        }
        query.append(");");

        CompositeResult result = executeQuery(query.toString(), table);
        Timestamp t = null;
        if (result.getResult().absolute(row)) {
            t = result.getResult().getTimestamp(search);
        }
        result.close();
        return t;
    }

    /**
     * Gets a Timestamp from the database.
     *
     * @param columns the list of columns for the research
     * @param values  the list of values to be searched in the columns
     * @param search  the name of the column whose value is wanted
     * @param table   the table to be used
     * @throws SQLException SQLException
     */
    public Timestamp getTimestamp(@NotNull String[] columns, Object[] values, @NotNull String search, String table) throws SQLException {
        return getTimestamp(columns, values, search, 1, table);
    }

    /**
     * Gets a Timestamp from the database.
     *
     * @param column the column for the research
     * @param value  the value to be searched in the column
     * @param search the name of the column whose value is wanted
     * @param row    if the research has more than one results, this is the number of the result you want (starts from 1)
     * @param table  the table to be used
     * @throws SQLException SQLException
     */
    public Timestamp getTimestamp(@NotNull String column, Object value, @NotNull String search, int row, String table) throws SQLException {
        return getTimestamp(new String[]{column}, new Object[]{value}, search, row, table);
    }

    /**
     * Gets a Timestamp from the database.
     *
     * @param column the column for the research
     * @param value  the value to be searched in the column
     * @param search the name of the column whose value is wanted
     * @param table  the table to be used
     * @throws SQLException SQLException
     */
    public Timestamp getTimestamp(@NotNull String column, Object value, @NotNull String search, String table) throws SQLException {
        return getTimestamp(new String[]{column}, new Object[]{value}, search, 1, table);
    }

    /**
     * Gets a Date from the database.
     *
     * @param columns the list of columns for the research
     * @param values  the list of values to be searched in the columns
     * @param search  the name of the column whose value is wanted
     * @param row     if the research has more than one results, this is the number of the result you want (starts from 1)
     * @param table   the table to be used
     * @throws SQLException SQLException
     */
    public Date getDate(@NotNull String[] columns, Object[] values, @NotNull String search, int row, String table) throws SQLException {
        if (columns.length != values.length)
            throw new IllegalArgumentException("Columns and values length must have the same value");
        if (row < 1) throw new IllegalArgumentException("Rows values starts from 1");

        StringBuilder query = new StringBuilder();
        query.append("SELECT `")
                .append(search)
                .append("` FROM `")
                .append(table)
                .append("` WHERE (");
        for (int i = 0; i < columns.length; i++) {
            if (values[i] == null) query.append("`")
                    .append(columns[i])
                    .append("`")
                    .append(" IS NULL");
            else query.append("`")
                    .append(columns[i])
                    .append("`")
                    .append(" = '")
                    .append(objectToString(values[i]))
                    .append("'");
            if (i != columns.length - 1) query.append(" AND ");
        }
        query.append(");");

        CompositeResult result = executeQuery(query.toString(), table);
        Date d = null;
        if (result.getResult().absolute(row)) {
            d = result.getResult().getDate(search);
        }
        result.close();
        return d;
    }

    /**
     * Gets a Date from the database.
     *
     * @param columns the list of columns for the research
     * @param values  the list of values to be searched in the columns
     * @param search  the name of the column whose value is wanted
     * @param table   the table to be used
     * @throws SQLException SQLException
     */
    public Date getDate(@NotNull String[] columns, Object[] values, @NotNull String search, String table) throws SQLException {
        return getDate(columns, values, search, 1, table);
    }

    /**
     * Gets a Date from the database.
     *
     * @param column the column for the research
     * @param value  the value to be searched in the column
     * @param search the name of the column whose value is wanted
     * @param row    if the research has more than one results, this is the number of the result you want (starts from 1)
     * @param table  the table to be used
     * @throws SQLException SQLException
     */
    public Date getDate(@NotNull String column, Object value, @NotNull String search, int row, String table) throws SQLException {
        return getDate(new String[]{column}, new Object[]{value}, search, row, table);
    }

    /**
     * Gets a Date from the database.
     *
     * @param column the column for the research
     * @param value  the value to be searched in the column
     * @param search the name of the column whose value is wanted
     * @param table  the table to be used
     * @throws SQLException SQLException
     */
    public Date getDate(@NotNull String column, Object value, @NotNull String search, String table) throws SQLException {
        return getDate(new String[]{column}, new Object[]{value}, search, 1, table);
    }

    /**
     * Gets a Time from the database.
     *
     * @param columns the list of columns for the research
     * @param values  the list of values to be searched in the columns
     * @param search  the name of the column whose value is wanted
     * @param row     if the research has more than one results, this is the number of the result you want (starts from 1)
     * @param table   the table to be used
     * @throws SQLException SQLException
     */
    public Time getTime(@NotNull String[] columns, Object[] values, @NotNull String search, int row, String table) throws SQLException {
        if (columns.length != values.length)
            throw new IllegalArgumentException("Columns and values length must have the same value");
        if (row < 1) throw new IllegalArgumentException("Rows values starts from 1");

        StringBuilder query = new StringBuilder();
        query.append("SELECT `")
                .append(search)
                .append("` FROM `")
                .append(table)
                .append("` WHERE (");
        for (int i = 0; i < columns.length; i++) {
            if (values[i] == null) query.append("`")
                    .append(columns[i])
                    .append("`")
                    .append(" IS NULL");
            else query.append("`")
                    .append(columns[i])
                    .append("`")
                    .append(" = '")
                    .append(objectToString(values[i]))
                    .append("'");
            if (i != columns.length - 1) query.append(" AND ");
        }
        query.append(");");

        CompositeResult result = executeQuery(query.toString(), table);
        Time t = null;
        if (result.getResult().absolute(row)) {
            t = result.getResult().getTime(search);
        }
        result.close();
        return t;
    }

    /**
     * Gets a Time from the database.
     *
     * @param columns the list of columns for the research
     * @param values  the list of values to be searched in the columns
     * @param search  the name of the column whose value is wanted
     * @param table   the table to be used
     * @throws SQLException SQLException
     */
    public Time getTime(@NotNull String[] columns, Object[] values, @NotNull String search, String table) throws SQLException {
        return getTime(columns, values, search, 1, table);
    }

    /**
     * Gets a Time from the database.
     *
     * @param column the column for the research
     * @param value  the value to be searched in the column
     * @param search the name of the column whose value is wanted
     * @param row    if the research has more than one results, this is the number of the result you want (starts from 1)
     * @param table  the table to be used
     * @throws SQLException SQLException
     */
    public Time getTime(@NotNull String column, Object value, @NotNull String search, int row, String table) throws SQLException {
        return getTime(new String[]{column}, new Object[]{value}, search, row, table);
    }

    /**
     * Gets a Time from the database.
     *
     * @param column the column for the research
     * @param value  the value to be searched in the column
     * @param search the name of the column whose value is wanted
     * @param table  the table to be used
     * @throws SQLException SQLException
     */
    public Time getTime(@NotNull String column, Object value, @NotNull String search, String table) throws SQLException {
        return getTime(new String[]{column}, new Object[]{value}, search, 1, table);
    }

    /**
     * Gets an UUID from the database.
     *
     * @param columns the list of columns for the research
     * @param values  the list of values to be searched in the columns
     * @param search  the name of the column whose value is wanted
     * @param row     if the research has more than one results, this is the number of the result you want (starts from 1)
     * @param table   the table to be used
     * @throws SQLException SQLException
     */
    public UUID getUUID(@NotNull String[] columns, Object[] values, @NotNull String search, int row, String table) throws SQLException {
        if (columns.length != values.length)
            throw new IllegalArgumentException("Columns and values length must have the same value");
        if (row < 1) throw new IllegalArgumentException("Rows values starts from 1");

        StringBuilder query = new StringBuilder();
        query.append("SELECT `")
                .append(search)
                .append("` FROM `")
                .append(table)
                .append("` WHERE (");
        for (int i = 0; i < columns.length; i++) {
            if (values[i] == null) query.append("`")
                    .append(columns[i])
                    .append("`")
                    .append(" IS NULL");
            else query.append("`")
                    .append(columns[i])
                    .append("`")
                    .append(" = '")
                    .append(objectToString(values[i]))
                    .append("'");
            if (i != columns.length - 1) query.append(" AND ");
        }
        query.append(");");

        CompositeResult result = executeQuery(query.toString(), table);
        UUID u = null;
        if (result.getResult().absolute(row)) {
            try {
                u = UUID.fromString(result.getResult().getString(search));
            } catch (Exception e) {
            }
        }
        result.close();
        return u;
    }

    /**
     * Gets an UUID from the database.
     *
     * @param columns the list of columns for the research
     * @param values  the list of values to be searched in the columns
     * @param search  the name of the column whose value is wanted
     * @param table   the table to be used
     * @throws SQLException SQLException
     */
    public UUID getUUID(@NotNull String[] columns, Object[] values, @NotNull String search, String table) throws SQLException {
        return getUUID(columns, values, search, 1, table);
    }

    /**
     * Gets an UUID from the database.
     *
     * @param column the column for the research
     * @param value  the value to be searched in the column
     * @param search the name of the column whose value is wanted
     * @param row    if the research has more than one results, this is the number of the result you want (starts from 1)
     * @param table  the table to be used
     * @throws SQLException SQLException
     */
    public UUID getUUID(@NotNull String column, Object value, @NotNull String search, int row, String table) throws SQLException {
        return getUUID(new String[]{column}, new Object[]{value}, search, row, table);
    }

    /**
     * Gets an UUID from the database.
     *
     * @param column the column for the research
     * @param value  the value to be searched in the column
     * @param search the name of the column whose value is wanted
     * @param table  the table to be used
     * @throws SQLException SQLException
     */
    public UUID getUUID(@NotNull String column, Object value, @NotNull String search, String table) throws SQLException {
        return getUUID(new String[]{column}, new Object[]{value}, search, 1, table);
    }

    /**
     * Gets a Object from the database.
     *
     * @param columns the list of columns for the research
     * @param values  the list of values to be searched in the columns
     * @param search  the name of the column whose value is wanted
     * @param row     if the research has more than one results, this is the number of the result you want (starts from 1)
     * @param table   the table to be used
     * @throws SQLException SQLException
     */
    public Object getObject(@NotNull String[] columns, Object[] values, @NotNull String search, int row, String table) throws SQLException {
        if (columns.length != values.length)
            throw new IllegalArgumentException("Columns and values length must have the same value");
        if (row < 1) throw new IllegalArgumentException("Rows values starts from 1");

        StringBuilder query = new StringBuilder();
        query.append("SELECT `")
                .append(search)
                .append("` FROM `")
                .append(table)
                .append("` WHERE (");
        for (int i = 0; i < columns.length; i++) {
            if (values[i] == null) query.append("`")
                    .append(columns[i])
                    .append("`")
                    .append(" IS NULL");
            else query.append("`")
                    .append(columns[i])
                    .append("`")
                    .append(" = '")
                    .append(objectToString(values[i]))
                    .append("'");
            if (i != columns.length - 1) query.append(" AND ");
        }
        query.append(");");

        CompositeResult result = executeQuery(query.toString(), table);
        Object o = null;
        if (result.getResult().absolute(row)) {
            o = result.getResult().getObject(search);
        }
        result.close();
        return o;
    }

    /**
     * Gets a Object from the database.
     *
     * @param columns the list of columns for the research
     * @param values  the list of values to be searched in the columns
     * @param search  the name of the column whose value is wanted
     * @param table   the table to be used
     * @throws SQLException SQLException
     */
    public Object getObject(@NotNull String[] columns, Object[] values, @NotNull String search, String table) throws SQLException {
        return getObject(columns, values, search, 1, table);
    }

    /**
     * Gets a Object from the database.
     *
     * @param column the column for the research
     * @param value  the value to be searched in the column
     * @param search the name of the column whose value is wanted
     * @param row    if the research has more than one results, this is the number of the result you want (starts from 1)
     * @param table  the table to be used
     * @throws SQLException SQLException
     */
    public Object getObject(@NotNull String column, Object value, @NotNull String search, int row, String table) throws SQLException {
        return getObject(new String[]{column}, new Object[]{value}, search, row, table);
    }

    /**
     * Gets a Object from the database.
     *
     * @param column the column for the research
     * @param value  the value to be searched in the column
     * @param search the name of the column whose value is wanted
     * @param table  the table to be used
     * @throws SQLException SQLException
     */
    public Object getObject(@NotNull String column, Object value, @NotNull String search, String table) throws SQLException {
        return getObject(new String[]{column}, new Object[]{value}, search, 1, table);
    }

    /**
     * Gets an object from the database.
     *
     * @param columns the list of columns for the research
     * @param values  the list of values to be searched in the columns
     * @param type    the type of the object you want to get
     * @param row     if the research has more than one results, this is the number of the result you want (starts from 1)
     * @param table   the table to be used
     * @throws SQLException SQLException
     */
    public <T> T getObject(@NotNull String[] columns, Object[] values, @NotNull Class<T> type, int row, String table) throws SQLException {
        if (columns.length != values.length)
            throw new IllegalArgumentException("Columns and values length must have the same value");
        if (row < 1) throw new IllegalArgumentException("Rows values starts from 1");

        StringBuilder query = new StringBuilder();
        query.append("SELECT * FROM `")
                .append(table)
                .append("` WHERE (");
        for (int i = 0; i < columns.length; i++) {
            if (values[i] == null) query.append("`")
                    .append(columns[i])
                    .append("`")
                    .append(" IS NULL");
            else query.append("`")
                    .append(columns[i])
                    .append("`")
                    .append(" = '")
                    .append(objectToString(values[i]))
                    .append("'");
            if (i != columns.length - 1) query.append(" AND ");
        }
        query.append(");");

        CompositeResult result = executeQuery(query.toString(), table);
        T t;
        if (result == null || result.getResult() == null) t = null;
        else t = createObject(type, result, row);

        if (result != null) result.close();
        return t;
    }

    /**
     * Gets a T object from the database.
     *
     * @param columns the list of columns for the research
     * @param values  the list of values to be searched in the columns
     * @param type    the type of the object you want to get
     * @param table   the table to be used
     * @throws SQLException SQLException
     */
    public <T> T getObject(@NotNull String[] columns, Object[] values, @NotNull Class<T> type, String table) throws SQLException {
        return getObject(columns, values, type, 1, table);
    }

    /**
     * Gets a T object from the database.
     *
     * @param column the column for the research
     * @param value  the value to be searched in the column
     * @param type   the type of the object you want to get
     * @param row    if the research has more than one results, this is the number of the result you want (starts from 1)
     * @param table  the table to be used
     * @throws SQLException SQLException
     */
    public <T> T getObject(@NotNull String column, Object value, @NotNull Class<T> type, int row, String table) throws SQLException {
        return getObject(new String[]{column}, new Object[]{value}, type, row, table);
    }

    /**
     * Gets a T object from the database.
     *
     * @param column the column for the research
     * @param value  the value to be searched in the column
     * @param type   the type of the object you want to get
     * @param table  the table to be used
     * @throws SQLException SQLException
     */
    public <T> T getObject(@NotNull String column, Object value, @NotNull Class<T> type, String table) throws SQLException {
        return getObject(new String[]{column}, new Object[]{value}, type, 1, table);
    }

    /**
     * Gets all contents of the database as a list of objects.
     *
     * @param type  the type of the ArrayList
     * @param table the table to be used
     * @throws SQLException SQLException | ClassCastException
     */
    public <T> List<T> getAll(@NotNull Class<T> type, String table) throws SQLException {
        StringBuilder query = new StringBuilder();
        query.append("SELECT * FROM `")
                .append(table)
                .append("` WHERE 1;");

        CompositeResult result = executeQuery(query.toString(), table);
        List<T> list = new ArrayList<>();
        while (result.getResult().next()) {
            list.add(createObject(type, result));
        }
        result.close();
        return list;
    }

    /**
     * Gets a ArrayList of Object from the database.
     *
     * @param columns the list of columns for the research
     * @param values  the list of values to be searched in the columns
     * @param type    the type of the ArrayList
     * @param table   the table to be used
     * @throws SQLException SQLException | ClassCastException
     */
    public <T> List<T> getList(@NotNull String[] columns, Object[] values, @NotNull Class<T> type, String table) throws SQLException {
        if (columns.length != values.length)
            throw new IllegalArgumentException("Columns and values length must have the same value");

        StringBuilder query = new StringBuilder();
        query.append("SELECT * FROM `")
                .append(table)
                .append("` WHERE (");
        for (int i = 0; i < columns.length; i++) {
            if (values[i] == null) query.append("`")
                    .append(columns[i])
                    .append("`")
                    .append(" IS NULL");
            else query.append("`")
                    .append(columns[i])
                    .append("`")
                    .append(" = '")
                    .append(objectToString(values[i]))
                    .append("'");
            if (i != columns.length - 1) query.append(" AND ");
        }
        query.append(");");

        CompositeResult result = executeQuery(query.toString(), table);
        List<T> list = new ArrayList<>();
        while (result.getResult().next()) {
            list.add(createObject(type, result));
        }
        result.close();
        return list;
    }

    /**
     * Gets a ArrayList of Object from the database.
     *
     * @param column the column for the research
     * @param value  the value to be searched in the column
     * @param search the name of the column whose value is wanted
     * @param type   the type of the ArrayList
     * @param table  the table to be used
     * @throws SQLException SQLException | ClassCastException
     */
    public <T> List<T> getList(@NotNull String column, Object value, @NotNull String search, @NotNull Class<T> type, String table) throws SQLException {
        return getList(new String[]{column}, new Object[]{value}, search, type, table);
    }

    /**
     * Gets a ArrayList of Object from the database.
     *
     * @param columns the list of columns for the research
     * @param values  the list of values to be searched in the columns
     * @param search  the name of the column whose value is wanted
     * @param type    the type of the ArrayList
     * @param table   the table to be used
     * @throws SQLException SQLException | ClassCastException
     */
    public <T> List<T> getList(@NotNull String[] columns, Object[] values, @NotNull String search, @NotNull Class<T> type, String table) throws SQLException {
        if (columns.length != values.length)
            throw new IllegalArgumentException("Columns and values length must have the same value");

        StringBuilder query = new StringBuilder();
        query.append("SELECT `")
                .append(search)
                .append("` FROM `")
                .append(table)
                .append("` WHERE (");
        for (int i = 0; i < columns.length; i++) {
            if (values[i] == null) query.append("`")
                    .append(columns[i])
                    .append("`")
                    .append(" IS NULL");
            else query.append("`")
                    .append(columns[i])
                    .append("`")
                    .append(" = '")
                    .append(objectToString(values[i]))
                    .append("'");
            if (i != columns.length - 1) query.append(" AND ");
        }
        query.append(");");

        CompositeResult result = executeQuery(query.toString(), table);
        List<T> list = new ArrayList<>();
        while (result.getResult().next()) {
            list.add(type.cast(result.getResult().getObject(search)));
        }
        result.close();
        return list;
    }

    /**
     * Gets a ArrayList of Object from the database.
     *
     * @param column the column for the research
     * @param value  the value to be searched in the column
     * @param type   the type of the ArrayList
     * @param table  the table to be used
     * @throws SQLException SQLException | ClassCastException
     */
    public <T> List<T> getList(@NotNull String column, Object value, @NotNull Class<T> type, String table) throws SQLException {
        return getList(new String[]{column}, new Object[]{value}, type, table);
    }

    /**
     * Gets the last Byte from the database.
     *
     * @param columns the list of columns for the research
     * @param values  the list of values to be searched in the columns
     * @param search  the name of the column whose value is wanted
     * @param table   the table to be used
     * @throws SQLException SQLException | ClassCastException
     */
    public Byte getLastByte(@NotNull String[] columns, Object[] values, @NotNull String search, String table) throws SQLException {
        List<Byte> list = getList(columns, values, search, Byte.class, table);
        return list.get(list.size() - 1);
    }

    /**
     * Gets the last Byte from the database.
     *
     * @param column the column for the research
     * @param value  the value to be searched in the column
     * @param search the name of the column whose value is wanted
     * @param table  the table to be used
     * @throws SQLException SQLException
     */
    public Byte getLastByte(@NotNull String column, Object value, @NotNull String search, String table) throws SQLException {
        return getLastByte(new String[]{column}, new Object[]{value}, search, table);
    }

    /**
     * Gets the last Short from the database.
     *
     * @param columns the list of columns for the research
     * @param values  the list of values to be searched in the columns
     * @param search  the name of the column whose value is wanted
     * @param table   the table to be used
     * @throws SQLException SQLException | ClassCastException
     */
    public Short getLastShort(@NotNull String[] columns, Object[] values, @NotNull String search, String table) throws SQLException {
        List<Short> list = getList(columns, values, search, Short.class, table);
        return list.get(list.size() - 1);
    }

    /**
     * Gets the last Short from the database.
     *
     * @param column the column for the research
     * @param value  the value to be searched in the column
     * @param search the name of the column whose value is wanted
     * @param table  the table to be used
     * @throws SQLException SQLException
     */
    public Short getLastShort(@NotNull String column, Object value, @NotNull String search, String table) throws SQLException {
        return getLastShort(new String[]{column}, new Object[]{value}, search, table);
    }

    /**
     * Gets the last Integer from the database.
     *
     * @param columns the list of columns for the research
     * @param values  the list of values to be searched in the columns
     * @param search  the name of the column whose value is wanted
     * @param table   the table to be used
     * @throws SQLException SQLException
     */
    public Integer getLastInteger(@NotNull String[] columns, Object[] values, @NotNull String search, String table) throws SQLException {
        List<Integer> list = getList(columns, values, search, Integer.class, table);
        return list.get(list.size() - 1);
    }

    /**
     * Gets the last Integer from the database.
     *
     * @param column the column for the research
     * @param value  the value to be searched in the column
     * @param search the name of the column whose value is wanted
     * @param table  the table to be used
     * @throws SQLException SQLException
     */
    public Integer getLastInteger(@NotNull String column, Object value, @NotNull String search, String table) throws SQLException {
        return getLastInteger(new String[]{column}, new Object[]{value}, search, table);
    }

    /**
     * Gets the last Long from the database.
     *
     * @param columns the list of columns for the research
     * @param values  the list of values to be searched in the columns
     * @param search  the name of the column whose value is wanted
     * @param table   the table to be used
     * @throws SQLException SQLException
     */
    public Long getLastLong(@NotNull String[] columns, Object[] values, @NotNull String search, String table) throws SQLException {
        List<Long> list = getList(columns, values, search, Long.class, table);
        return list.get(list.size() - 1);
    }

    /**
     * Gets the last Long from the database.
     *
     * @param column the column for the research
     * @param value  the value to be searched in the column
     * @param search the name of the column whose value is wanted
     * @param table  the table to be used
     * @throws SQLException SQLException
     */
    public Long getLastLong(@NotNull String column, Object value, @NotNull String search, String table) throws SQLException {
        return getLastLong(new String[]{column}, new Object[]{value}, search, table);
    }

    /**
     * Gets the last Float from the database.
     *
     * @param columns the list of columns for the research
     * @param values  the list of values to be searched in the columns
     * @param search  the name of the column whose value is wanted
     * @param table   the table to be used
     * @throws SQLException SQLException
     */
    public Float getLastFloat(@NotNull String[] columns, Object[] values, @NotNull String search, String table) throws SQLException {
        List<Float> list = getList(columns, values, search, Float.class, table);
        return list.get(list.size() - 1);
    }

    /**
     * Gets the last Float from the database.
     *
     * @param column the column for the research
     * @param value  the value to be searched in the column
     * @param search the name of the column whose value is wanted
     * @param table  the table to be used
     * @throws SQLException SQLException
     */
    public Float getLastFloat(@NotNull String column, Object value, @NotNull String search, String table) throws SQLException {
        return getLastFloat(new String[]{column}, new Object[]{value}, search, table);
    }

    /**
     * Gets the last Double from the database.
     *
     * @param columns the list of columns for the research
     * @param values  the list of values to be searched in the columns
     * @param search  the name of the column whose value is wanted
     * @param table   the table to be used
     * @throws SQLException SQLException
     */
    public Double getLastDouble(@NotNull String[] columns, Object[] values, @NotNull String search, String table) throws SQLException {
        List<Double> list = getList(columns, values, search, Double.class, table);
        return list.get(list.size() - 1);
    }

    /**
     * Gets the last Double from the database.
     *
     * @param column the column for the research
     * @param value  the value to be searched in the column
     * @param search the name of the column whose value is wanted
     * @param table  the table to be used
     * @throws SQLException SQLException
     */
    public Double getLastDouble(@NotNull String column, Object value, @NotNull String search, String table) throws SQLException {
        return getLastDouble(new String[]{column}, new Object[]{value}, search, table);
    }

    /**
     * Gets the last String from the database.
     *
     * @param columns the list of columns for the research
     * @param values  the list of values to be searched in the columns
     * @param search  the name of the column whose value is wanted
     * @param table   the table to be used
     * @throws SQLException SQLException
     */
    public String getLastString(@NotNull String[] columns, Object[] values, @NotNull String search, String table) throws SQLException {
        List<String> list = getList(columns, values, search, String.class, table);
        return list.get(list.size() - 1);
    }

    /**
     * Gets the last String from the database.
     *
     * @param column the column for the research
     * @param value  the value to be searched in the column
     * @param search the name of the column whose value is wanted
     * @param table  the table to be used
     * @throws SQLException SQLException
     */
    public String getLastString(@NotNull String column, Object value, @NotNull String search, String table) throws SQLException {
        return getLastString(new String[]{column}, new Object[]{value}, search, table);
    }

    /**
     * Gets the last Boolean from the database.
     *
     * @param columns the list of columns for the research
     * @param values  the list of values to be searched in the columns
     * @param search  the name of the column whose value is wanted
     * @param table   the table to be used
     * @throws SQLException SQLException
     */
    public Boolean getLastBoolean(@NotNull String[] columns, Object[] values, @NotNull String search, String table) throws SQLException {
        List<Boolean> list = getList(columns, values, search, Boolean.class, table);
        return list.get(list.size() - 1);
    }

    /**
     * Gets the last Boolean from the database.
     *
     * @param column the column for the research
     * @param value  the value to be searched in the column
     * @param search the name of the column whose value is wanted
     * @param table  the table to be used
     * @throws SQLException SQLException
     */
    public Boolean getLastBoolean(@NotNull String column, Object value, @NotNull String search, String table) throws SQLException {
        return getLastBoolean(new String[]{column}, new Object[]{value}, search, table);
    }

    /**
     * Gets the last Timestamp from the database.
     *
     * @param columns the list of columns for the research
     * @param values  the list of values to be searched in the columns
     * @param search  the name of the column whose value is wanted
     * @param table   the table to be used
     * @throws SQLException SQLException
     */
    public Timestamp getLastTimestamp(@NotNull String[] columns, Object[] values, @NotNull String search, String table) throws SQLException {
        List<Timestamp> list = getList(columns, values, search, Timestamp.class, table);
        return list.get(list.size() - 1);
    }

    /**
     * Gets the last Timestamp from the database.
     *
     * @param column the column for the research
     * @param value  the value to be searched in the column
     * @param search the name of the column whose value is wanted
     * @param table  the table to be used
     * @throws SQLException SQLException
     */
    public Timestamp getLastTimestamp(@NotNull String column, Object value, @NotNull String search, String table) throws SQLException {
        return getLastTimestamp(new String[]{column}, new Object[]{value}, search, table);
    }

    /**
     * Gets the last Date from the database.
     *
     * @param columns the list of columns for the research
     * @param values  the list of values to be searched in the columns
     * @param search  the name of the column whose value is wanted
     * @param table   the table to be used
     * @throws SQLException SQLException | ClassCastException
     */
    public Date getLastDate(@NotNull String[] columns, Object[] values, @NotNull String search, String table) throws SQLException {
        List<Date> list = getList(columns, values, search, Date.class, table);
        return list.get(list.size() - 1);
    }

    /**
     * Gets the last Date from the database.
     *
     * @param column the column for the research
     * @param value  the value to be searched in the column
     * @param search the name of the column whose value is wanted
     * @param table  the table to be used
     * @throws SQLException SQLException
     */
    public Date getLastDate(@NotNull String column, Object value, @NotNull String search, String table) throws SQLException {
        return getLastDate(new String[]{column}, new Object[]{value}, search, table);
    }

    /**
     * Gets the last Time from the database.
     *
     * @param columns the list of columns for the research
     * @param values  the list of values to be searched in the columns
     * @param search  the name of the column whose value is wanted
     * @param table   the table to be used
     * @throws SQLException SQLException | ClassCastException
     */
    public Time getLastTime(@NotNull String[] columns, Object[] values, @NotNull String search, String table) throws SQLException {
        List<Time> list = getList(columns, values, search, Time.class, table);
        return list.get(list.size() - 1);
    }

    /**
     * Gets the last Time from the database.
     *
     * @param column the column for the research
     * @param value  the value to be searched in the column
     * @param search the name of the column whose value is wanted
     * @param table  the table to be used
     * @throws SQLException SQLException
     */
    public Time getLastTime(@NotNull String column, Object value, @NotNull String search, String table) throws SQLException {
        return getLastTime(new String[]{column}, new Object[]{value}, search, table);
    }

    /**
     * Gets the last UUID from the database.
     *
     * @param columns the list of columns for the research
     * @param values  the list of values to be searched in the columns
     * @param search  the name of the column whose value is wanted
     * @param table   the table to be used
     * @throws SQLException SQLException | ClassCastException
     */
    public UUID getLastUUID(@NotNull String[] columns, Object[] values, @NotNull String search, String table) throws SQLException {
        List<UUID> list = getList(columns, values, search, UUID.class, table);
        return list.get(list.size() - 1);
    }

    /**
     * Gets the last UUID from the database.
     *
     * @param column the column for the research
     * @param value  the value to be searched in the column
     * @param search the name of the column whose value is wanted
     * @param table  the table to be used
     * @throws SQLException SQLException
     */
    public UUID getLastUUID(@NotNull String column, Object value, @NotNull String search, String table) throws SQLException {
        return getLastUUID(new String[]{column}, new Object[]{value}, search, table);
    }

    /**
     * Gets the last Object from the database.
     *
     * @param columns the list of columns for the research
     * @param values  the list of values to be searched in the columns
     * @param search  the name of the column whose value is wanted
     * @param table   the table to be used
     * @throws SQLException SQLException
     */
    public Object getLastObject(@NotNull String[] columns, Object[] values, @NotNull String search, String table) throws SQLException {
        List<Object> list = getList(columns, values, search, Object.class, table);
        return list.get(list.size() - 1);
    }

    /**
     * Gets the last Object from the database.
     *
     * @param column the column for the research
     * @param value  the value to be searched in the column
     * @param search the name of the column whose value is wanted
     * @param table  the table to be used
     * @throws SQLException SQLException
     */
    public Object getLastObject(@NotNull String column, Object value, @NotNull String search, String table) throws SQLException {
        return getLastObject(new String[]{column}, new Object[]{value}, search, table);
    }

    /**
     * Gets the last T object from the database.
     *
     * @param columns the list of columns for the research
     * @param values  the list of values to be searched in the columns
     * @param search  the name of the column whose value is wanted
     * @param type    the type of the object you want to get
     * @param table   the table to be used
     * @throws SQLException SQLException
     */
    public <T> T getLastObject(@NotNull String[] columns, Object[] values, @NotNull String search, @NotNull Class<T> type, String table) throws SQLException {
        List<Object> list = getList(columns, values, search, Object.class, table);
        return type.cast(list.get(list.size() - 1));
    }

    /**
     * Gets the last T object from the database.
     *
     * @param column the column for the research
     * @param value  the value to be searched in the column
     * @param search the name of the column whose value is wanted
     * @param type   the type of the object you want to get
     * @param table  the table to be used
     * @throws SQLException SQLException
     */
    public <T> T getLastObject(@NotNull String column, Object value, @NotNull String search, @NotNull Class<T> type, String table) throws SQLException {
        return getLastObject(new String[]{column}, new Object[]{value}, search, type, table);
    }

    /**
     * Update a list of columns with new values.
     *
     * @param columnsToEdit the list of columns to edit
     * @param newValues     the list of new values
     * @param columns       the list of columns for the research
     * @param values        the list of values to be searched in the columns
     * @param table         the table to be used
     * @throws SQLException SQLException
     */
    public void set(@NotNull String[] columnsToEdit, Object[] newValues, @NotNull String[] columns, Object[] values, String table) throws SQLException {
        if ((columns.length != values.length) || (columnsToEdit.length != newValues.length))
            throw new IllegalArgumentException("Columns and values length must have the same value");

        StringBuilder query = new StringBuilder();
        query.append("UPDATE `")
                .append(table)
                .append("` SET ");
        for (int i = 0; i < columnsToEdit.length; i++) {
            query.append("`")
                    .append(columnsToEdit[i])
                    .append("`")
                    .append(" ='");
            query.append(objectToString(newValues[i])).append("' ");
            if (i != columnsToEdit.length - 1) query.append(", ");
        }
        query.append("WHERE (");
        for (int i = 0; i < columns.length; i++) {
            query.append("`")
                    .append(columns[i])
                    .append("`")
                    .append(" = '")
                    .append(objectToString(values[i]))
                    .append("'");
            if (i != columns.length - 1) query.append(" AND ");
        }
        query.append(");");

        executeUpdate(query.toString(), table);
    }

    /**
     * Update a list of columns with new values.
     *
     * @param columnToEdit the column to edit
     * @param newValue     the new value
     * @param columns      the list of columns for the research
     * @param values       the list of values to be searched in the columns
     * @param table        the table to be used
     * @throws SQLException SQLException
     */
    public void set(@NotNull String columnToEdit, Object newValue, @NotNull String[] columns, Object[] values, String table) throws SQLException {
        set(new String[]{columnToEdit}, new Object[]{newValue}, columns, values, table);
    }

    /**
     * Update a list of columns with new values.
     *
     * @param columnsToEdit the list of columns to edit
     * @param newValues     the list of new values
     * @param column        the column for the research
     * @param value         the value to be searched in the column
     * @param table         the table to be used
     * @throws SQLException SQLException
     */
    public void set(@NotNull String[] columnsToEdit, Object[] newValues, @NotNull String column, Object value, String table) throws SQLException {
        set(columnsToEdit, newValues, new String[]{column}, new Object[]{value}, table);
    }

    /**
     * Update a list of columns with new values.
     *
     * @param columnToEdit the column to edit
     * @param newValues    the new value
     * @param column       the column for the research
     * @param value        the value to be searched in the column
     * @param table        the table to be used
     * @throws SQLException SQLException
     */
    public void set(@NotNull String columnToEdit, Object newValues, @NotNull String column, Object value, String table) throws SQLException {
        set(new String[]{columnToEdit}, new Object[]{newValues}, new String[]{column}, new Object[]{value}, table);
    }

    /**
     * Update object columns with new values.
     *
     * @param obj     the object to be inserted
     * @param columns the list of columns for the research
     * @param values  the list of values to be searched in the columns
     * @param table   the table to be used
     * @throws SQLException SQLException
     */
    public void set(@NotNull Object obj, String[] columns, Object[] values, String table) throws SQLException {
        HashMap<String, Object> map = parseObject(obj, false);
        Object[] c = map.keySet().toArray();
        String[] cols = Arrays.copyOf(c, c.length, String[].class);
        Object[] vals = map.values().toArray();

        set(cols, vals, columns, values, table);
    }

    /**
     * Update object columns with new values.
     *
     * @param obj    the object to be inserted
     * @param column the column for the research
     * @param value  the value to be searched in the column
     * @param table  the table to be used
     * @throws SQLException SQLException
     */
    public void set(@NotNull Object obj, String column, Object value, String table) throws SQLException {
        set(obj, new String[]{column}, new Object[]{value}, table);
    }
}