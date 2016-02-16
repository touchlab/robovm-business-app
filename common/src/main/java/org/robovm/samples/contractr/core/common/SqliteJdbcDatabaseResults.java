package org.robovm.samples.contractr.core.common;
import com.j256.ormlite.dao.ObjectCache;
import com.j256.ormlite.db.DatabaseType;
import com.j256.ormlite.jdbc.JdbcDatabaseResults;
import com.j256.ormlite.support.DatabaseResults;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Map;

/**
 * Created by kgalligan on 2/15/16.
 */
public class SqliteJdbcDatabaseResults extends JdbcDatabaseResults
{
    public SqliteJdbcDatabaseResults(PreparedStatement preparedStmt, ResultSet resultSet, ObjectCache objectCache)
            throws SQLException
    {
        super(preparedStmt, resultSet, objectCache);
    }

    public boolean getBoolean(int columnIndex) throws SQLException
    {
        return getShort(columnIndex) != 0;
    }

    public byte getByte(int columnIndex) throws SQLException
    {
        return (byte)getShort(columnIndex);
    }

    /*public int getColumnCount() throws SQLException {
        return metaData.getColumnCount();
    }

    public String[] getColumnNames() throws SQLException {
        int colN = metaData.getColumnCount();
        String[] columnNames = new String[colN];
        for (int colC = 0; colC < colN; colC++) {
            columnNames[colC] = metaData.getColumnName(colC + 1);
        }
        return columnNames;
    }

    public boolean first() throws SQLException {
        if (first) {
			*//*
			 * We have to do this because some databases do not like us calling first() if we are only moving forward
			 * through the results. We do this here because Android has no such issues.
			 *//*
            first = false;
            return next();
        } else {
            return resultSet.first();
        }
    }

    public boolean next() throws SQLException {
        // NOTE: we should not auto-close here, even if there are no more results
        if (resultSet.next()) {
            return true;
        } else if (!preparedStmt.getMoreResults()) {
            return false;
        } else {
            return resultSet.next();
        }
    }

    public boolean last() throws SQLException {
        return resultSet.last();
    }

    public boolean previous() throws SQLException {
        return resultSet.previous();
    }

    public boolean moveRelative(int offset) throws SQLException {
        return resultSet.relative(offset);
    }

    public boolean moveAbsolute(int position) throws SQLException {
        return resultSet.absolute(position);
    }

    public int findColumn(String columnName) throws SQLException {
        return resultSet.findColumn(columnName) - 1;
    }

    public String getString(int columnIndex) throws SQLException {
        return resultSet.getString(columnIndex + 1);
    }

    public char getChar(int columnIndex) throws SQLException {
        String string = resultSet.getString(columnIndex + 1);
        if (string == null || string.length() == 0) {
            return 0;
        } else if (string.length() == 1) {
            return string.charAt(0);
        } else {
            throw new SQLException("More than 1 character stored in database column: " + columnIndex);
        }
    }

    public byte[] getBytes(int columnIndex) throws SQLException
    {
        return resultSet.getBytes(columnIndex + 1);
    }

    public short getShort(int columnIndex) throws SQLException
    {
        return resultSet.getShort(columnIndex + 1);
    }

    public int getInt(int columnIndex) throws SQLException
    {
        return resultSet.getInt(columnIndex + 1);
    }

    public long getLong(int columnIndex) throws SQLException
    {
        return resultSet.getLong(columnIndex + 1);
    }

    public float getFloat(int columnIndex) throws SQLException
    {
        return resultSet.getFloat(columnIndex + 1);
    }

    public double getDouble(int columnIndex) throws SQLException
    {
        return resultSet.getDouble(columnIndex + 1);
    }

    public Timestamp getTimestamp(int columnIndex) throws SQLException {
        throw new SQLException("Android does not support timestamp.  Use JAVA_DATE_LONG or JAVA_DATE_STRING types");
    }

    public InputStream getBlobStream(int columnIndex) throws SQLException
    {
        Blob blob = resultSet.getBlob(columnIndex + 1);
        if (blob == null) {
            return null;
        } else {
            return blob.getBinaryStream();
        }
    }

    public BigDecimal getBigDecimal(int columnIndex) throws SQLException {
        throw new SQLException("Android does not support BigDecimal type.  Use BIG_DECIMAL or BIG_DECIMAL_STRING types");
    }

    public boolean wasNull(int columnIndex) throws SQLException
    {
        return resultSet.wasNull();
    }

    public ObjectCache getObjectCache() {
        return objectCache;
    }

    public void close() throws SQLException
    {
        resultSet.close();
    }

    public void closeQuietly() {
        try
        {
            close();
        }
        catch(SQLException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "@" + Integer.toHexString(super.hashCode());
    }*/

}
