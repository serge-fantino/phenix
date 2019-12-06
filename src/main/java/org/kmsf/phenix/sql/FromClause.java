package org.kmsf.phenix.sql;

import org.kmsf.phenix.database.ScopeException;
import org.kmsf.phenix.database.View;
import org.kmsf.phenix.function.Function;

public class FromClause implements Printer {

    private Scope scope;
    private View view;
    private String alias;

    public FromClause(Scope scope, View view, String alias) {
        this.scope = scope;
        this.view = view;
        this.alias = alias;
    }

    public Scope getScope() {
        return scope;
    }

    public View getValue() {
        return view;
    }

    public PrintResult print(PrintResult result) throws ScopeException {
        if (view.getPrecedence() < Function.PRECEDENCE_ORDER_VIEW) result.append("(");
        view.print(scope, result);
        if (view.getPrecedence() < Function.PRECEDENCE_ORDER_VIEW) result.append(")");
        result.space().append(alias);
        return result;
    }

    @Override
    public String toString() {
        return view.toString()+" AS "+alias;
    }
}
