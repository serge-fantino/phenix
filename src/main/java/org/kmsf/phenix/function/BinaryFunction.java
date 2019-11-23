package org.kmsf.phenix.function;

import org.kmsf.phenix.database.ScopeException;
import org.kmsf.phenix.sql.PrintResult;
import org.kmsf.phenix.sql.Scope;

public class BinaryFunction extends Function {

    private String name;
    private Function a;
    private Function b;

    public BinaryFunction(String name, Function a, Function b) {
        this.name = name;
        this.a = a;
        this.b = b;
    }

    public PrintResult print(Scope scope, PrintResult result) throws ScopeException {
        result.append(name + "(");
        a.print(scope, result).append(",");
        b.print(scope, result).append(")");
        return result;
    }

    @Override
    public FunctionType getSource() {
        FunctionType aa = a.getSource();
        FunctionType bb = b.getSource();
        if (aa.equals(bb)) return aa;
        return new FunctionType(aa, bb);
    }

}
