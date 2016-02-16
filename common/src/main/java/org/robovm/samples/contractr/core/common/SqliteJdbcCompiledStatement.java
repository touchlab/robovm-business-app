package org.robovm.samples.contractr.core.common;
import com.j256.ormlite.dao.ObjectCache;
import com.j256.ormlite.field.SqlType;
import com.j256.ormlite.jdbc.JdbcDatabaseResults;
import com.j256.ormlite.jdbc.TypeValMapper;
import com.j256.ormlite.stmt.StatementBuilder;
import com.j256.ormlite.support.CompiledStatement;
import com.j256.ormlite.support.DatabaseResults;

import java.sql.PreparedStatement;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

/**
 * Created by kgalligan on 2/15/16.
 */
public class SqliteJdbcCompiledStatement implements CompiledStatement
{

    private final PreparedStatement              preparedStatement;
    private final StatementBuilder.StatementType type;
    private ResultSetMetaData metaData = null;

    public SqliteJdbcCompiledStatement(PreparedStatement preparedStatement, StatementBuilder.StatementType type) {
        this.preparedStatement = preparedStatement;
        this.type = type;
    }

    public int getColumnCount() throws SQLException
    {
        if (metaData == null) {
            metaData = preparedStatement.getMetaData();
        }
        return metaData.getColumnCount();
    }

    public String getColumnName(int column) throws SQLException {
        if (metaData == null) {
            metaData = preparedStatement.getMetaData();
        }
        return metaData.getColumnName(column + 1);
    }

    public int runUpdate() throws SQLException {
        // this can be a UPDATE, DELETE, or ... just not a SELECT
        if (!type.isOkForUpdate()) {
            throw new IllegalArgumentException("Cannot call update on a " + type + " statement");
        }
        return preparedStatement.executeUpdate();
    }

    public DatabaseResults runQuery(ObjectCache objectCache) throws SQLException {
        if (!type.isOkForQuery()) {
            throw new IllegalArgumentException("Cannot call query on a " + type + " statement");
        }
        return new SqliteJdbcDatabaseResults(preparedStatement, preparedStatement.executeQuery(), objectCache);
    }

    public int runExecute() throws SQLException {
        if (!type.isOkForExecute()) {
            throw new IllegalArgumentException("Cannot call execute on a " + type + " statement");
        }
        preparedStatement.execute();
        return preparedStatement.getUpdateCount();
    }

    public void close() throws SQLException {
        preparedStatement.close();
    }

    public void closeQuietly() {
        try {
            close();
        } catch (SQLException e) {
            // ignored
        }
    }

    public void cancel() throws SQLException {
        preparedStatement.cancel();
    }

    public void setObject(int parameterIndex, Object obj, SqlType sqlType) throws SQLException {
        if (obj == null) {
            preparedStatement.setNull(parameterIndex + 1, TypeValMapper.getTypeValForSqlType(sqlType));
        } else {
            preparedStatement.setObject(parameterIndex + 1, obj, TypeValMapper.getTypeValForSqlType(sqlType));
        }
    }

    public void setMaxRows(int max) throws SQLException {
        preparedStatement.setMaxRows(max);
    }

    public void setQueryTimeout(long millis) throws SQLException {
        preparedStatement.setQueryTimeout(Long.valueOf(millis).intValue() / 1000);
    }

    /**
     * Called by {@link JdbcDatabaseResults#next()} to get more results into the existing ResultSet.
     */
    boolean getMoreResults() throws SQLException {
        return preparedStatement.getMoreResults();
    }
}
