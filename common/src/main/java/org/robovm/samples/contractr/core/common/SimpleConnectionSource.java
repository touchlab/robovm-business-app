package org.robovm.samples.contractr.core.common;

import com.j256.ormlite.db.DatabaseType;
import com.j256.ormlite.db.SqliteDatabaseType;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.support.DatabaseConnection;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Created by kgalligan on 2/14/16.
 */
public class SimpleConnectionSource implements ConnectionSource {
    private final Connection connection;
    private final DatabaseConnection databaseConnection;

    public SimpleConnectionSource(Connection connection) {
        this.connection = connection;
        this.databaseConnection = new SQLDroidJdbcDatabaseConnection(connection);
    }

    @Override
    public DatabaseConnection getReadOnlyConnection() throws SQLException {
        return databaseConnection;
    }

    @Override
    public DatabaseConnection getReadWriteConnection() throws SQLException {
        return databaseConnection;
    }

    @Override
    public void releaseConnection(DatabaseConnection connection) throws SQLException {

    }

    @Override
    public boolean saveSpecialConnection(DatabaseConnection connection) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clearSpecialConnection(DatabaseConnection connection) {
        throw new UnsupportedOperationException();
    }

    @Override
    public DatabaseConnection getSpecialConnection() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void close() throws SQLException {
        //Ehh
    }

    @Override
    public void closeQuietly() {
        try {
            connection.close();
        } catch (SQLException e) {
            //Heyo
        }
    }

    @Override
    public DatabaseType getDatabaseType() {
        return new SqliteAndroidDatabaseType();
    }

    @Override
    public boolean isOpen() {
        try {
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            throw new SQLiteException(e);
        }
    }
}
