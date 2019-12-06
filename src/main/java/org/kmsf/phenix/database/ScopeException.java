package org.kmsf.phenix.database;

public class ScopeException extends Exception {

    public ScopeException(String message) {
        super(message);
    }

    public ScopeException(String message, Throwable e) {
        super(message, e);
    }

}
