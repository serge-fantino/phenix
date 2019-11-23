package org.kmsf.phenix.sql;

import org.kmsf.phenix.database.ScopeException;
import org.kmsf.phenix.function.Function;

public class GroupByClause implements Printer {

    private Function expr;
    private Scope scope;

    public GroupByClause(Scope scope, Function expr) {
        this.scope = scope;
        this.expr = expr;
    }

    public Function getValue() {
        return expr;
    }

    public PrintResult print(PrintResult result) throws ScopeException {
        return expr.print(scope, result);
    }

}