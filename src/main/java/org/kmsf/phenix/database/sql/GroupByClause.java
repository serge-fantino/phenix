package org.kmsf.phenix.database.sql;

import org.kmsf.phenix.function.Function;

import java.util.Optional;

public class GroupByClause implements Printer {

    private Function expr;
    private Scope scope;

    public GroupByClause(Scope scope, Function expr) {
        this.scope = scope;
        this.expr = expr;
    }

    public PrintResult print(PrintResult result) {
        return expr.print(scope, result);
    }

}
