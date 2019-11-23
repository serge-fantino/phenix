package org.kmsf.phenix.function;

import org.kmsf.phenix.database.ScopeException;
import org.kmsf.phenix.sql.PrintResult;
import org.kmsf.phenix.sql.Scope;

public class UnaryFunction extends Function {

    private String name;
    private Function a;

    public UnaryFunction(String name, Function a) {
        this.name = name;
        this.a = a;
    }

    public PrintResult print(Scope scope, PrintResult result) throws ScopeException {
        result.append(name + "(");
        a.print(scope, result).append(")");
        return result;
    }

    @Override
    public FunctionType getSource() {
        FunctionType aa = a.getSource();
        return new FunctionType(aa);
    }

}
