package org.kmsf.phenix.sql;

import org.kmsf.phenix.database.ScopeException;
import org.kmsf.phenix.database.Select;
import org.kmsf.phenix.algebra.Expression;
import org.kmsf.phenix.database.View;

public class JoinClause extends FromClause {

    private Expression join;

    public JoinClause(Scope scope, View view, Expression join, String alias) {
        super(scope, view, alias);
        this.join = join;
    }

    public PrintResult print(PrintResult result) throws ScopeException {
        result.append(Select.INNERJOIN).space();
        super.print(result).space().append(Select.ON).space();
        join.print(getScope(), result);
        return result;
    }

}
