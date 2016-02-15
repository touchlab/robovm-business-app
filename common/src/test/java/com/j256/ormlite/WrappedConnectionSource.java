package com.j256.ormlite;

import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import com.j256.ormlite.db.DatabaseType;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.support.DatabaseConnection;

/**
 * Wrapped connection source for testing purposes.
 * 
 * @author graywatson
 */
public class WrappedConnectionSource implements ConnectionSource {

	private AtomicInteger getReleaseCount = new AtomicInteger(0);
	protected boolean nextForceOkay = false;
	protected ConnectionSource cs;
	private final Map<DatabaseConnection, WrappedConnection> wrappedConnections =
			new HashMap<DatabaseConnection, WrappedConnection>();

	public WrappedConnectionSource(ConnectionSource cs) {
		this.cs = cs;
	}

	public DatabaseConnection getReadOnlyConnection() throws SQLException {
		DatabaseConnection connection = cs.getReadOnlyConnection();
		getReleaseCount.incrementAndGet();
		WrappedConnection wrapped = wrapConnection(connection);
		wrappedConnections.put(wrapped.getDatabaseConnectionProxy(), wrapped);
		// System.err.println("got wrapped " + wrapped.hashCode() + ", count = " + getReleaseCount);
		// new RuntimeException().printStackTrace();
		return wrapped.getDatabaseConnectionProxy();
	}

	public DatabaseConnection getReadWriteConnection() throws SQLException {
		DatabaseConnection connection = cs.getReadWriteConnection();
		getReleaseCount.incrementAndGet();
		WrappedConnection wrapped = wrapConnection(connection);
		connection = wrapped.getDatabaseConnectionProxy();
		wrappedConnections.put(connection, wrapped);
		// System.err.println("got wrapped " + wrapped.hashCode() + ", count = " + getReleaseCount);
		// new RuntimeException().printStackTrace();
		return connection;
	}

	public void releaseConnection(DatabaseConnection connection) throws SQLException {
		WrappedConnection wrapped = wrappedConnections.remove(connection);
		if (wrapped == null) {
			if (nextForceOkay) {
				return;
			} else {
				throw new SQLException("Tried to release unknown connection");
			}
		} else if (!wrapped.isOkay()) {
			throw new SQLException("Wrapped connection was not okay when released");
		}
		cs.releaseConnection(wrapped.getDatabaseConnectionProxy());
		getReleaseCount.decrementAndGet();
		// System.err.println("released wrapped " + wrapped.hashCode() + ", count = " + getReleaseCount);
	}

	public void close() throws SQLException {
		cs.close();
		if (!isOkay()) {
			throw new SQLException("Wrapped connection was not okay on close");
		}
		for (WrappedConnection wrapped : wrappedConnections.values()) {
			wrapped.close();
		}
		wrappedConnections.clear();
	}

	public void closeQuietly() {
		try {
			close();
		} catch (SQLException e) {
			// ignored
		}
	}

	protected WrappedConnection wrapConnection(DatabaseConnection connection) {
		WrappedDatabaseConnection wrapped = new WrappedDatabaseConnection(connection);
		return wrapped;
	}

	/**
	 * Used if we want to forcefully close a connection source
	 */
	public void forceOkay() {
		nextForceOkay = true;
	}

	public boolean isOkay() {
		if (nextForceOkay) {
			nextForceOkay = false;
			return true;
		} else if (getReleaseCount.get() != 0) {
			System.err.println("isOkay, get/release count is " + getReleaseCount.get());
			for (WrappedConnection wrapped : wrappedConnections.values()) {
				System.err.println("  still have wrapped " + wrapped.hashCode());
			}
			return false;
		} else {
			for (WrappedConnection wrapped : wrappedConnections.values()) {
				if (!wrapped.isOkay()) {
					return false;
				}
			}
			return true;
		}
	}

	public boolean saveSpecialConnection(DatabaseConnection connection) throws SQLException {
		return cs.saveSpecialConnection(connection);
	}

	public void clearSpecialConnection(DatabaseConnection connection) {
		cs.clearSpecialConnection(connection);
	}

	public DatabaseConnection getSpecialConnection() {
		return cs.getSpecialConnection();
	}

	public DatabaseType getDatabaseType() {
		return cs.getDatabaseType();
	}

	public boolean isOpen() {
		return cs.isOpen();
	}

	public void setDatabaseType(DatabaseType databaseType) {
		Method method;
		try {
			method = cs.getClass().getMethod("setDatabaseType", new Class[] { DatabaseType.class });
			method.invoke(cs, databaseType);
		} catch (Exception e) {
			throw new RuntimeException("Could not set database type", e);
		}
	}
}
