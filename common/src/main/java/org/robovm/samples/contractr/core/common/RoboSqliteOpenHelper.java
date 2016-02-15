package org.robovm.samples.contractr.core.common;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by kgalligan on 2/13/16.
 */
public abstract class RoboSqliteOpenHelper {
        private static final String TAG = RoboSqliteOpenHelper.class.getSimpleName();

        // When true, getReadableDatabase returns a read-only database if it is just being opened.
        // The database handle is reopened in read/write mode when getWritableDatabase is called.
        // We leave this behavior disabled in production because it is inefficient and breaks
        // many applications.  For debugging purposes it can be useful to turn on strict
        // read-only semantics to catch applications that call getReadableDatabase when they really
        // wanted getWritableDatabase.
        private static final boolean DEBUG_STRICT_READONLY = false;

        private final RoboVMContext mContext;
        private final String mName;
//        private final CursorFactory mFactory;
        private final int mNewVersion;

        private Connection mDatabase;
        private boolean mIsInitializing;
        private boolean mEnableWriteAheadLogging;

        /**
         * Create a helper object to create, open, and/or manage a database.
         * This method always returns very quickly.  The database is not actually
         * created or opened until one of {@link #getWritableDatabase} or
         * {@link #getReadableDatabase} is called.
         *
         * @param context to use to open or create the database
         * @param name of the database file, or null for an in-memory database
         * @param factory to use for creating cursor objects, or null for the default
         * @param version number of the database (starting at 1); if the database is older,
         *     {@link #onUpgrade} will be used to upgrade the database; if the database is
         *     newer, {@link #onDowngrade} will be used to downgrade the database
         */
        public RoboSqliteOpenHelper(RoboVMContext context, String name, /*CursorFactory factory, */int version) {
            if (version < 1) throw new IllegalArgumentException("Version must be >= 1, was " + version);

            mContext = context;
            mName = name;
            mNewVersion = version;
        }

        /**
         * Return the name of the SQLite database being opened, as given to
         * the constructor.
         */
        public String getDatabaseName() {
            return mName;
        }

        /**
         * Create and/or open a database that will be used for reading and writing.
         * The first time this is called, the database will be opened and
         * {@link #onCreate}, {@link #onUpgrade} and/or {@link #onOpen} will be
         * called.
         *
         * <p>Once opened successfully, the database is cached, so you can
         * call this method every time you need to write to the database.
         * (Make sure to call {@link #close} when you no longer need the database.)
         * Errors such as bad permissions or a full disk may cause this method
         * to fail, but future attempts may succeed if the problem is fixed.</p>
         *
         * <p class="caution">Database upgrade may take a long time, you
         * should not call this method from the application main thread, including
         * from {@link android.content.ContentProvider#onCreate ContentProvider.onCreate()}.
         *
         * @throws SQLiteException if the database cannot be opened for writing
         * @return a read/write database object valid until {@link #close} is called
         */
        public Connection getWritableDatabase() {
            synchronized (this) {
                return getDatabaseLocked(true);
            }
        }

        /**
         * Create and/or open a database.  This will be the same object returned by
         * {@link #getWritableDatabase} unless some problem, such as a full disk,
         * requires the database to be opened read-only.  In that case, a read-only
         * database object will be returned.  If the problem is fixed, a future call
         * to {@link #getWritableDatabase} may succeed, in which case the read-only
         * database object will be closed and the read/write object will be returned
         * in the future.
         *
         * <p class="caution">Like {@link #getWritableDatabase}, this method may
         * take a long time to return, so you should not call it from the
         * application main thread, including from
         * {@link android.content.ContentProvider#onCreate ContentProvider.onCreate()}.
         *
         * @throws SQLiteException if the database cannot be opened
         * @return a database object valid until {@link #getWritableDatabase}
         *     or {@link #close} is called.
         */
        public Connection getReadableDatabase() {
            synchronized (this) {
                return getDatabaseLocked(false);
            }
        }

    private int getVersion(Connection db) {
        try {
            ResultSet resultSet = db.prepareStatement("PRAGMA user_version;").executeQuery();
            resultSet.next();
            return resultSet.getInt(1);
        } catch (SQLException e) {
            throw new SQLiteException(e);
        }
    }

    private void setVersion(Connection db, int version) {
        execSQL(db, "PRAGMA user_version = " + version);
        }

    private void execSQL(Connection db, String sql)
    {
        try {
            db.prepareStatement(sql).execute();
        } catch (SQLException e) {
            throw new SQLiteException(e);
        }
    }
    public void execSQL(String sql)
    {
        execSQL(mDatabase, sql);
    }

        private Connection getDatabaseLocked(boolean writable)  {
            try {
                if (mDatabase != null) {
                    if (mDatabase.isClosed()) {
                        // Darn!  The user closed the database by calling mDatabase.close().
                        mDatabase = null;
                    } else if (!writable || !mDatabase.isReadOnly()) {
                        // The database is already open for business.
                        return mDatabase;
                    }
                }

                if (mIsInitializing) {
                    throw new IllegalStateException("getDatabase called recursively");
                }

                Connection db = mDatabase;
                try {
                    mIsInitializing = true;

                    if (db != null) {
                        if (writable && db.isReadOnly()) {
                            db = mContext.createDb(mName, false);
                        }
                    }
                    //todo evaluate what to do here
                    /*else if (mName == null) {
                        db = Connection.create(null);
                    }*/
                    else {
                        try {
                            if (DEBUG_STRICT_READONLY && !writable) {
                                db = mContext.createDb(mName, false);
                            } else {
                                db = mContext.createDb(mName, false);
                            }
                        } catch (SQLiteException ex) {
                            if (writable) {
                                throw ex;
                            }
                            log(TAG, "Couldn't open " + mName
                                    + " for writing (will try read-only):", ex);
                            db = mContext.createDb(mName, true);
                        }
                    }

                    onConfigure(db);

                    final int version = getVersion(db);
                    if (version != mNewVersion) {
                        if (db.isReadOnly()) {
                            throw new SQLiteException("Can't upgrade read-only database from version " +
                                    getVersion(db) + " to " + mNewVersion + ": " + mName);
                        }

                        db.setAutoCommit(false);
//                        db.beginTransaction();
                        try {
                            if (version == 0) {
                                onCreate(db);
                            } else {
                                if (version > mNewVersion) {
                                    onDowngrade(db, version, mNewVersion);
                                } else {
                                    onUpgrade(db, version, mNewVersion);
                                }
                            }
                            setVersion(db, mNewVersion);
                            db.commit();
                        }
                        catch (SQLException e)
                        {
                            db.rollback();
                        }
                        finally {
                            db.setAutoCommit(true);
                        }
                    }

                    onOpen(db);

                    if (db.isReadOnly()) {

                        log(TAG, "Opened " + mName + " in read-only mode");
                    }

                    mDatabase = db;
                    return db;
                } finally {
                    mIsInitializing = false;
                    if (db != null && db != mDatabase) {
                        db.close();
                    }
                }
            } catch (SQLException e) {
                throw new SQLiteException(e);
            }
        }

    private void log(String tag, String message)
    {
        //?
    }

    private void log(String tag, String message, Throwable t)
    {
        //?
    }
        /**
         * Close any open database object.
         */
        public synchronized void close()  {
            if (mIsInitializing) throw new IllegalStateException("Closed during initialization");

            try {
                if (mDatabase != null && !mDatabase.isClosed()) {
                    mDatabase.close();
                    mDatabase = null;
                }
            } catch (SQLException e) {
                throw new SQLiteException(e);
            }
        }

        /**
         * Called when the database connection is being configured, to enable features
         * such as write-ahead logging or foreign key support.
         * <p>
         * This method is called before {@link #onCreate}, {@link #onUpgrade},
         * {@link #onDowngrade}, or {@link #onOpen} are called.  It should not modify
         * the database except to configure the database connection as required.
         * </p><p>
         * This method should only call methods that configure the parameters of the
         * database connection, such as {@link Connection#enableWriteAheadLogging}
         * {@link Connection#setForeignKeyConstraintsEnabled},
         * {@link Connection#setLocale}, {@link Connection#setMaximumSize},
         * or executing PRAGMA statements.
         * </p>
         *
         * @param db The database.
         */
        public void onConfigure(Connection db) {}

        /**
         * Called when the database is created for the first time. This is where the
         * creation of tables and the initial population of the tables should happen.
         *
         * @param db The database.
         */
        public abstract void onCreate(Connection db);

        /**
         * Called when the database needs to be upgraded. The implementation
         * should use this method to drop tables, add tables, or do anything else it
         * needs to upgrade to the new schema version.
         *
         * <p>
         * The SQLite ALTER TABLE documentation can be found
         * <a href="http://sqlite.org/lang_altertable.html">here</a>. If you add new columns
         * you can use ALTER TABLE to insert them into a live table. If you rename or remove columns
         * you can use ALTER TABLE to rename the old table, then create the new table and then
         * populate the new table with the contents of the old table.
         * </p><p>
         * This method executes within a transaction.  If an exception is thrown, all changes
         * will automatically be rolled back.
         * </p>
         *
         * @param db The database.
         * @param oldVersion The old database version.
         * @param newVersion The new database version.
         */
        public abstract void onUpgrade(Connection db, int oldVersion, int newVersion);

        /**
         * Called when the database needs to be downgraded. This is strictly similar to
         * {@link #onUpgrade} method, but is called whenever current version is newer than requested one.
         * However, this method is not abstract, so it is not mandatory for a customer to
         * implement it. If not overridden, default implementation will reject downgrade and
         * throws SQLiteException
         *
         * <p>
         * This method executes within a transaction.  If an exception is thrown, all changes
         * will automatically be rolled back.
         * </p>
         *
         * @param db The database.
         * @param oldVersion The old database version.
         * @param newVersion The new database version.
         */
        public void onDowngrade(Connection db, int oldVersion, int newVersion) {
            throw new SQLiteException("Can't downgrade database from version " +
                    oldVersion + " to " + newVersion);
        }

        /**
         * Called when the database has been opened.  The implementation
         * should check {@link Connection#isReadOnly} before updating the
         * database.
         * <p>
         * This method is called after the database connection has been configured
         * and after the database schema has been created, upgraded or downgraded as necessary.
         * If the database connection must be configured in some way before the schema
         * is created, upgraded, or downgraded, do it in {@link #onConfigure} instead.
         * </p>
         *
         * @param db The database.
         */
        public void onOpen(Connection db) {}
}
