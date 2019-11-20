package org.kmsf.phenix.function;

import org.kmsf.phenix.database.Column;
import org.kmsf.phenix.database.ScopeException;
import org.kmsf.phenix.database.View;
import org.kmsf.phenix.database.sql.PrintResult;
import org.kmsf.phenix.database.sql.Scope;

public class Functions {

    public static final String _EQUALS = "equals";
    public static final String _SUM = "SUM";
    public static final String _STAR = "*";

    public static Function EQUALS(Function a, Function b) {
        return new BinaryFunction(_EQUALS, a, b) {
            public PrintResult print(Scope scope, PrintResult result) {
                a.print(scope, result).append("=");
                b.print(scope, result);
                return result;
            }
        };
    }

    public static Function STAR(View v) {
        return new Function() {
            @Override
            public PrintResult print(Scope scope, PrintResult result) {
                try {
                    String alias = scope.get(v).getAlias();
                    result.append(alias).dot().append(_STAR);
                } catch (ScopeException e) {
                    result.error(new ScopeException(e.getMessage() + " at position " + result.size()));
                    result.append(_STAR);
                }
                return result;
            }

            @Override
            public FunctionType getSource() {
                return new FunctionType(v);
            }
        };
    }

    public static Function SUM(Function a) {
        return new UnaryFunction(_SUM, a);
    }

}
