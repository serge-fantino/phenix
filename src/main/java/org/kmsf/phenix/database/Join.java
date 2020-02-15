package org.kmsf.phenix.database;

import org.kmsf.phenix.sql.PrintResult;
import org.kmsf.phenix.sql.Scope;
import org.kmsf.phenix.algebra.FunctionType;
import org.kmsf.phenix.algebra.Expression;

public class Join extends Expression {

    private View target;
    private Expression definition;

    public Join(View target, Expression definition) {
        this.target = target;
        this.definition = definition;
    }

    public Expression getDefinition() {
        return definition;
    }

    @Override
    public PrintResult print(Scope scope, PrintResult result) throws ScopeException {
        result.append("INNER JOIN ");
        target.print(scope, result);
        result.append(" ON ");
        definition.print(scope, result);
        return result;
    }

    @Override
    public FunctionType getSource() {
        return definition.getSource();
    }

    public View getTarget() {
        return target;
    }

    @Override
    public Expression redux() {
        return target.redux();
    }

    @Override
    public int hashCode() {
        return target.hashCode();
    }

    @Override
    public String toString() {
        return "INNER JOIN " + target + " ON " + definition;
    }
}
