package org.kmsf.phenix.database;

import org.kmsf.phenix.database.sql.PrintResult;
import org.kmsf.phenix.database.sql.Scope;
import org.kmsf.phenix.function.FunctionType;
import org.kmsf.phenix.function.Function;

public class Join extends Function {

    private View target;
    private Function definition;

    public Join(View target, Function definition) {
        this.target = target;
        this.definition = definition;
    }

    public Function getDefinition() {
        return definition;
    }

    @Override
    public PrintResult print(Scope scope, PrintResult result) {
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
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj instanceof View) {
            return obj.equals(target);
        }
        return false;
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
