package org.kmsf.phenix.sql;

import org.kmsf.phenix.database.ScopeException;
import org.kmsf.phenix.algebra.Expression;

public class GroupByClause implements Printer {

    private Expression expr;
    private Scope scope;

    public GroupByClause(Scope scope, Expression expr) {
        this.scope = scope;
        this.expr = expr;
    }

    public Expression getValue() {
        return expr;
    }

    public PrintResult print(PrintResult result) throws ScopeException {
        return expr.print(scope, result);
    }

}
