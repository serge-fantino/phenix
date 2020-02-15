package org.kmsf.phenix.database;

public class DatabaseProperties {

    // default to false because it is killing for testing
    private boolean quoteIdentifier = false;

    public DatabaseProperties() {
    }

    public DatabaseProperties setQuoteIdentifier(boolean quoteIdentifier) {
        this.quoteIdentifier = quoteIdentifier;
        return this;
    }

    public boolean isQuoteIdentifier() {
        return quoteIdentifier;
    }
}
