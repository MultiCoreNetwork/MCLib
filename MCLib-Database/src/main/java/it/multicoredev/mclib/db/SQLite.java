package it.multicoredev.mclib.db;

import it.multicoredev.mclib.db.connectors.SQLiteConnector;
import org.jetbrains.annotations.NotNull;
import org.objenesis.Objenesis;
import org.objenesis.ObjenesisStd;
import org.objenesis.instantiator.ObjectInstantiator;

import java.io.File;
import java.lang.reflect.Field;
import java.sql.*;
import java.util.UUID;

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
public class SQLite extends SQLImplementation {
    private File database;

    public SQLite(@NotNull File database) {
        super(new SQLiteConnector(database));
        this.database = database;
    }

    public File getDatabase() {
        return database;
    }

    /**
     * Executes a given MySQL query.
     *
     * @param query the query to be executed
     * @param table   the table to be used
     * @return the CompositeResult of the query
     * @throws SQLException SQLException
     */
    @Override
    public CompositeResult executeQuery(@NotNull String query, String table) throws SQLException {
        if (query.trim().isEmpty()) throw new IllegalArgumentException("Query cannot be empty");
        query = query.replace("{table}", table);

        Connection connection = connector.getConnection();
        PreparedStatement statement = connection.prepareStatement(query, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
        ResultSet result = statement.executeQuery();

        if (printQuery) System.out.println(query);

        return new CompositeResult(connection, statement, result, query);
    }

    /**
     * Executes an update given a MySQL query.
     *
     * @param query the query to be executed
     * @param table   the table to be used
     * @throws SQLException SQLException
     */
    @Override
    public void executeUpdate(@NotNull String query, String table) throws SQLException {
        if (query.trim().isEmpty()) throw new IllegalArgumentException("Query cannot be empty");
        query = query.replace("{table}", table);

        Connection connection = null;
        PreparedStatement statement = null;

        try {
            connection = connector.getConnection();
            statement = connection.prepareStatement(query);
            statement.executeUpdate();

            if (printQuery) System.out.println(query);
        } finally {
            DBUtils.closeQuietly(statement);
            DBUtils.closeQuietly(connection);
        }
    }

    @Override
    protected  <T> T createObject(Class<T> type, CompositeResult result, int row) throws SQLException {
        if (type == null || result == null) throw new IllegalArgumentException("Arguments cannot be null");

        Objenesis objenesis = new ObjenesisStd();
        ObjectInstantiator<T> instantiator = objenesis.getInstantiatorOf(type);
        T obj = instantiator.newInstance();

        if (result.getResult().next()) {
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
        }

        return obj;
    }

    /**
     * Gets a Byte from the database.
     *
     * @param columns the list of columns for the research
     * @param values  the list of values to be searched in the columns
     * @param search  the name of the column whose value is wanted
     * @param row     ignored (not supported by SQLite)
     * @param table   the table to be used
     * @throws SQLException SQLException
     */
    @Override
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
        if (result.getResult().next()) {
            b = result.getResult().getByte(search);
        }
        result.close();
        return b;
    }

    /**
     * Gets a Short from the database.
     *
     * @param columns the list of columns for the research
     * @param values  the list of values to be searched in the columns
     * @param search  the name of the column whose value is wanted
     * @param row     ignored (not supported by SQLite)
     * @param table   the table to be used
     * @throws SQLException SQLException
     */
    @Override
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
        if (result.getResult().next()) {
            s = result.getResult().getShort(search);
        }
        result.close();
        return s;
    }

    /**
     * Gets a Integer from the database.
     *
     * @param columns the list of columns for the research
     * @param values  the list of values to be searched in the columns
     * @param search  the name of the column whose value is wanted
     * @param row     ignored (not supported by SQLite)
     * @param table   the table to be used
     * @throws SQLException SQLException
     */
    @Override
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
        if (result.getResult().next()) {
            i = result.getResult().getInt(search);
        }
        result.close();
        return i;
    }

    /**
     * Gets a Long from the database.
     *
     * @param columns the list of columns for the research
     * @param values  the list of values to be searched in the columns
     * @param search  the name of the column whose value is wanted
     * @param row     ignored (not supported by SQLite)
     * @param table   the table to be used
     * @throws SQLException SQLException
     */
    @Override
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
        if (result.getResult().next()) {
            l = result.getResult().getLong(search);
        }
        result.close();
        return l;
    }

    /**
     * Gets a Float from the database.
     *
     * @param columns the list of columns for the research
     * @param values  the list of values to be searched in the columns
     * @param search  the name of the column whose value is wanted
     * @param row     ignored (not supported by SQLite)
     * @param table   the table to be used
     * @throws SQLException SQLException
     */
    @Override
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
        if (result.getResult().next()) {
            f = result.getResult().getFloat(search);
        }
        result.close();
        return f;
    }

    /**
     * Gets a Double from the database.
     *
     * @param columns the list of columns for the research
     * @param values  the list of values to be searched in the columns
     * @param search  the name of the column whose value is wanted
     * @param row     ignored (not supported by SQLite)
     * @param table   the table to be used
     * @throws SQLException SQLException
     */
    @Override
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
        if (result.getResult().next()) {
            d = result.getResult().getDouble(search);
        }
        result.close();
        return d;
    }

    /**
     * Gets a String from the database.
     *
     * @param columns the list of columns for the research
     * @param values  the list of values to be searched in the columns
     * @param search  the name of the column whose value is wanted
     * @param row     ignored (not supported by SQLite)
     * @param table   the table to be used
     * @throws SQLException SQLException
     */
    @Override
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
        if (result.getResult().next()) {
            s = result.getResult().getString(search);
        }
        result.close();
        return s;
    }

    /**
     * Gets a Boolean from the database.
     *
     * @param columns the list of columns for the research
     * @param values  the list of values to be searched in the columns
     * @param search  the name of the column whose value is wanted
     * @param row     ignored (not supported by SQLite)
     * @param table   the table to be used
     * @throws SQLException SQLException
     */
    @Override
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
        if (result.getResult().next()) {
            b = result.getResult().getBoolean(search);
        }
        result.close();
        return b;
    }

    /**
     * Gets a Timestamp from the database.
     *
     * @param columns the list of columns for the research
     * @param values  the list of values to be searched in the columns
     * @param search  the name of the column whose value is wanted
     * @param row     ignored (not supported by SQLite)
     * @param table   the table to be used
     * @throws SQLException SQLException
     */
    @Override
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
        if (result.getResult().next()) {
            t = result.getResult().getTimestamp(search);
        }
        result.close();
        return t;
    }

    /**
     * Gets a Date from the database.
     *
     * @param columns the list of columns for the research
     * @param values  the list of values to be searched in the columns
     * @param search  the name of the column whose value is wanted
     * @param row     ignored (not supported by SQLite)
     * @param table   the table to be used
     * @throws SQLException SQLException
     */
    @Override
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
        if (result.getResult().next()) {
            d = result.getResult().getDate(search);
        }
        result.close();
        return d;
    }

    /**
     * Gets a Time from the database.
     *
     * @param columns the list of columns for the research
     * @param values  the list of values to be searched in the columns
     * @param search  the name of the column whose value is wanted
     * @param row     ignored (not supported by SQLite)
     * @param table   the table to be used
     * @throws SQLException SQLException
     */
    @Override
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
        if (result.getResult().next()) {
            t = result.getResult().getTime(search);
        }
        result.close();
        return t;
    }

    /**
     * Gets an UUID from the database.
     *
     * @param columns the list of columns for the research
     * @param values  the list of values to be searched in the columns
     * @param search  the name of the column whose value is wanted
     * @param row     ignored (not supported by SQLite)
     * @param table   the table to be used
     * @throws SQLException SQLException
     */
    @Override
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
        if (result.getResult().next()) {
            try {
                u = UUID.fromString(result.getResult().getString(search));
            } catch (Exception e) {
            }
        }
        result.close();
        return u;
    }

    /**
     * Gets a Object from the database.
     *
     * @param columns the list of columns for the research
     * @param values  the list of values to be searched in the columns
     * @param search  the name of the column whose value is wanted
     * @param row     ignored (not supported by SQLite)
     * @param table   the table to be used
     * @throws SQLException SQLException
     */
    @Override
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
        if (result.getResult().next()) {
            o = result.getResult().getObject(search);
        }
        result.close();
        return o;
    }
    
    
}
