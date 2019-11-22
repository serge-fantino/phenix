package org.kmsf.phenix.function;

import org.kmsf.phenix.database.Column;
import org.kmsf.phenix.database.ScopeException;
import org.kmsf.phenix.database.View;
import org.kmsf.phenix.database.sql.PrintResult;
import org.kmsf.phenix.database.sql.Scope;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Functions {

    public static final String _EQUALS = "equals";
    public static final String _SUM = "SUM";
    public static final String _AND = "AND";
    public static final String _COUNT = "COUNT";
    public static final String _STAR = "*";

    public static Function EQUALS(Function a, Function b) {
        return new BinaryFunction(_EQUALS, a, b) {
            public PrintResult print(Scope scope, PrintResult result) {
                result.append(scope, a).append("=").append(scope, b);
                return result;
            }
        };
    }

    public static Function EQUALS(List<? extends Function> a, List<? extends Function> b) throws ScopeException {
        if (a.size() != b.size()) throw new ScopeException("different list size");
        if (a.isEmpty()) throw new ScopeException("empty list");
        if (a.size() == 1)
            return EQUALS(a.get(0), b.get(0));
        List<Function> functions = new ArrayList<>();
        for (int i = 0; i < a.size(); i++) {
            functions.add(EQUALS(a.get(i), b.get(i)));
        }
        return AND(functions);
    }

    public static Function AND(List<? extends Function> args) throws ScopeException {
        if (args.size() < 2) throw new ScopeException("invalid list size");
        return new Function() {
            @Override
            public PrintResult print(Scope scope, PrintResult result) {
                for (int i = 0; i < args.size(); i++) {
                    if (i > 0) result.space().append(_AND).space();
                    result.append(scope, args.get(i));
                }
                return result;
            }

            @Override
            public FunctionType getSource() {
                return new FunctionType(args);
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

    public static Function COUNT(Function a) {
        return new UnaryFunction(_COUNT, a) {
            @Override
            public PrintResult print(Scope scope, PrintResult result) {
                return result.append(_COUNT).append("(").append(scope, a).append(")");
            }
        };
    }

}
