package org.robovm.samples.contactr.core.common;
import org.robovm.samples.contractr.core.common.RoboVMContext;
import org.robovm.samples.contractr.core.common.SQLiteException;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Created by kgalligan on 2/14/16.
 */
public class TestRoboVMContext extends RoboVMContext
{
    public TestRoboVMContext(boolean android, File rootDir)
    {
        super(android, rootDir);
    }

    @Override
    public File getDatabasePath(String dbName)
    {
        throw new UnsupportedOperationException("Not in tests");
    }

    @Override
    public Connection createDb(String name, boolean readOnly)
    {
        try
        {
//            if(name != null)
//                throw new RuntimeException("Memory only for tests");

            return DriverManager.getConnection("jdbc:sqlite::memory:");
        }
        catch(SQLException e)
        {
            throw new SQLiteException(e);
        }
    }
}
