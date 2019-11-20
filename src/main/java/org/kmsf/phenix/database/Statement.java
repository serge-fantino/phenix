package org.kmsf.phenix.database;

/**
 * a statement is a view that can be computed with SQL statement
 */
public abstract class Statement extends View {

    public abstract String print() throws ScopeException;

    @Override
    public int getPrecedence() {
        return PRECEDENCE_ORDER_STATEMENT;
    }

}
