package org.kmsf.phenix.database;

import org.kmsf.phenix.function.Function;

import java.util.Optional;

/**
 * a statement is a view that can be computed with SQL statement
 */
public abstract class Statement extends View {

    public abstract Optional<Function> accept(Function expr);

    public abstract String print() throws ScopeException;

    @Override
    public int getPrecedence() {
        return PRECEDENCE_ORDER_STATEMENT;
    }

}
