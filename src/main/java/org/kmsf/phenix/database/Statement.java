package org.kmsf.phenix.database;

import org.kmsf.phenix.algebra.PrecedenceOrder;

/**
 * a statement is a view that can be computed with SQL statement (Select for instance)
 */
public abstract class Statement extends View {

    public abstract String print() throws ScopeException;

    @Override
    public int getPrecedence() {
        return PrecedenceOrder.PRECEDENCE_ORDER_STATEMENT;
    }

}
