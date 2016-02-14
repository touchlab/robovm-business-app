package org.robovm.samples.contractr.core.common;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Created by kgalligan on 2/13/16.
 */
public class RoboVMContext {
    public static final String IOS_DB_DIR = "Documents";
    public static final String ANDROID_DB_DIR = "databases";
    private final boolean android;
    private final File rootDir;

    public RoboVMContext(boolean android, File rootDir) {
        this.android = android;
        this.rootDir = rootDir;
        if(android)
        {

        }
        else
        {
//            rootDir = new File(System.getenv("HOME"));
        }

    }

    public File getDatabasePath(String dbName)
    {
        File databasesDir = new File(rootDir, android ? ANDROID_DB_DIR : IOS_DB_DIR);
        databasesDir.mkdirs();
        return new File(databasesDir, dbName);
    }

    public Connection createDb(String name, boolean readOnly)
    {
        File dbFile = getDatabasePath(name);

        String jdbcPrefix = android ? "jdbc:sqldroid:" : "jdbc:sqlite:";

        try {
            if(readOnly)
            {
                Properties config = new Properties();
                config.setProperty("open_mode", "1");  //1 == readonly

                return DriverManager.getConnection(jdbcPrefix + dbFile.getAbsolutePath(), config);
            }
            else {
                return DriverManager.getConnection(jdbcPrefix + dbFile.getAbsolutePath());
            }
        } catch (SQLException e) {
            throw new SQLiteException(e);
        }
    }
}
