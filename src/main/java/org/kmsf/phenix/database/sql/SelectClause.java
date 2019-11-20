package org.kmsf.phenix.database.sql;

import org.kmsf.phenix.database.Column;
import org.kmsf.phenix.database.Select;
import org.kmsf.phenix.database.Selector;
import org.kmsf.phenix.function.Function;
import org.kmsf.phenix.function.Functions;

import java.util.Optional;

public class SelectClause implements Printer {

    private Function expr;
    private Optional<String> alias = Optional.empty();
    private Scope scope;

    public SelectClause(Scope scope, Function expr) {
        this.scope = scope;
        this.expr = expr;
    }

    public SelectClause(Scope scope, Function expr, String alias) {
        this(scope, expr);
        this.alias = Optional.ofNullable(alias);
    }

    public PrintResult print(PrintResult result) {
        expr.print(scope, result);
        if (alias.isPresent()) {
            if (expr instanceof Selector) {
                Selector selector = (Selector) expr;
                Optional<String> defaultAlias = selector.getDefaultAlias();
                if (!defaultAlias.equals(alias) && alias.isPresent() && !(defaultAlias.isPresent() && defaultAlias.get().equals(Functions._STAR))) {
                    appendAlias(result, alias.get());
                }
            } else
                appendAlias(result, alias.get());
        }
        return result;
    }

    private void appendAlias(PrintResult result, String alias) {
        result.space().append(Select.AS).space().append(alias);
    }

}
