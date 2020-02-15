package org.kmsf.phenix.sql;

import org.kmsf.phenix.database.Join;
import org.kmsf.phenix.database.ScopeException;
import org.kmsf.phenix.database.Select;
import org.kmsf.phenix.function.Function;
import org.kmsf.phenix.database.View;

public class JoinClause extends FromClause {

    private Join join;

    public JoinClause(Scope scope, Join join, String alias) {
        super(scope, join, alias);
        this.join = join;
    }

    public PrintResult print(PrintResult result) throws ScopeException {
        result.append(Select.INNERJOIN).space();
        super.print(getScope(), join.getTarget(), getAlias(), result).space().append(Select.ON).space();
        join.print(getScope(), result);
        return result;
    }

}
