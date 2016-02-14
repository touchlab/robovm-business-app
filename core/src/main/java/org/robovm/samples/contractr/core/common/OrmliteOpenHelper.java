package org.robovm.samples.contractr.core.common;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.DatabaseTableConfigLoader;

import java.io.*;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Created by kgalligan on 2/13/16.
 */
public abstract class OrmliteOpenHelper extends RoboSqliteOpenHelper {

    protected boolean cancelQueriesEnabled;

    public OrmliteOpenHelper(RoboVMContext context, String databaseName, int databaseVersion)
    {
        super(context, databaseName, databaseVersion);
    }

    public OrmliteOpenHelper(RoboVMContext context, String databaseName, int databaseVersion, Class... classes)
    {
        super(context, databaseName, databaseVersion);

        File dbFile = context.getDatabasePath(databaseName);
        File dbDir = dbFile.getParentFile();
        File configFile = new File(dbDir, dbFile.getName() + "." + databaseVersion + ".ormlite.config");

        try
        {
            if (!configFile.exists())
            {
                dbDir.mkdirs();
                File tempFile = new File(dbDir, "temp_" + System.currentTimeMillis());
                OrmLiteConfigUtil.writeConfigFile(tempFile, classes);
                tempFile.renameTo(configFile);
            }

            initConfigStream(new FileInputStream(configFile));

        } catch (SQLException e)
        {
            throw new RuntimeException(e);
        } catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    public OrmliteOpenHelper(RoboVMContext context, String databaseName, int databaseVersion,
                             File configFile) {
        this(context, databaseName, databaseVersion, openFile(configFile));
    }

    public OrmliteOpenHelper(RoboVMContext context, String databaseName, int databaseVersion,
                             InputStream stream) {
        super(context, databaseName, databaseVersion);
        initConfigStream(stream);
    }

    private void initConfigStream(InputStream stream)
    {
        if (stream == null) {
            return;
        }

        // if a config file-id was specified then load it into the DaoManager
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(stream), 4096);
            stream = null;
            DaoManager.addCachedDatabaseConfigs(DatabaseTableConfigLoader.loadDatabaseConfigFromReader(reader));
        } catch (SQLException e) {
            throw new IllegalStateException("Could not load object config file", e);
        } finally {
            IOUtils.closeQuietly(reader);
            IOUtils.closeQuietly(stream);
        }
    }

    public abstract void onCreate(Connection db, ConnectionSource connectionSource);

    public abstract void onUpgrade(Connection db, ConnectionSource connectionSource, int oldVersion,
                                   int newVersion);

    @Override
    public final void onCreate(Connection db) {
        ConnectionSource cs = new SimpleConnectionSource(db);
        onCreate(db, cs);
    }

    @Override
    public final void onUpgrade(Connection db, int oldVersion, int newVersion) {
        ConnectionSource cs = new SimpleConnectionSource(db);
        onUpgrade(db, cs, oldVersion, newVersion);
    }

    public <D extends Dao<T, ?>, T> D getDao(Class<T> clazz) throws SQLException {
        // special reflection fu is now handled internally by create dao calling the database type
        ConnectionSource cs = new SimpleConnectionSource(getWritableDatabase());
        Dao<T, ?> dao = DaoManager.createDao(cs, clazz);
        @SuppressWarnings("unchecked")
        D castDao = (D) dao;
        return castDao;
    }

    public <D extends RuntimeExceptionDao<T, ?>, T> D getRuntimeExceptionDao(Class<T> clazz) {
        try {
            Dao<T, ?> dao = getDao(clazz);
            @SuppressWarnings({ "unchecked", "rawtypes" })
            D castDao = (D) new RuntimeExceptionDao(dao);
            return castDao;
        } catch (SQLException e) {
            throw new RuntimeException("Could not create RuntimeExcepitionDao for class " + clazz, e);
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "@" + Integer.toHexString(super.hashCode());
    }

    private static InputStream openFile(File configFile) {
        try {
            if (configFile == null) {
                return null;
            } else {
                return new FileInputStream(configFile);
            }
        } catch (FileNotFoundException e) {
            throw new IllegalArgumentException("Could not open config file " + configFile, e);
        }
    }
}
