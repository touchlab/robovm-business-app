package org.robovm.samples.contractr.core.common;

/**
 * Created by kgalligan on 2/13/16.
 */
public class SQLiteException extends RuntimeException {
    public SQLiteException() {
    }

    public SQLiteException(String s) {
        super(s);
    }

    public SQLiteException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public SQLiteException(Throwable throwable) {
        super(throwable);
    }
}
