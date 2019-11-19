package org.kmsf.phenix.database.sql;

import org.kmsf.phenix.function.Function;
import org.kmsf.phenix.database.View;

public class JoinClause extends FromClause {

    private Function join;

    public JoinClause(Scope scope, View view, Function join, String alias) {
        super(scope, view, alias);
        this.join = join;
    }

    public PrintResult print(PrintResult result) {
        result.append("INNER JOIN ");
        super.print(result).append(" ON ");
        join.print(getScope(), result);
        return result;
    }

}
