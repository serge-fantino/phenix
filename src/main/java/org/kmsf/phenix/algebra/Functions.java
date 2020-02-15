package org.kmsf.phenix.algebra;

import org.kmsf.phenix.database.ScopeException;
import org.kmsf.phenix.database.View;
import org.kmsf.phenix.sql.PrintResult;
import org.kmsf.phenix.sql.Scope;

import java.util.ArrayList;
import java.util.List;

public class Functions {

    public static <T> Expression CONST(T value) {
        return new ConstExpression<T>(value);
    }

    public static Expression IN(Expression a, Expression b) {
        return new Function(Operators.IN, a, b) {
            public PrintResult print(Scope scope, PrintResult result) throws ScopeException {
                result.append(scope, a).space().append(Operators._IN).space().append("(").append(scope, b).append(")");
                return result;
            }
        };
    }

    public static Expression ADD(Expression a, Expression b) {
        return new Function(Operators.ADD, a, b);
    }

    public static Expression MULTIPLY(Expression a, Expression b) {
        return new Function(Operators.MULTIPLY, a, b);
    }

    public static Expression EQUALS(Expression a, Expression b) {
        return new Function(Operators.EQUALS, a, b);
    }

    public static Expression EQUALS(List<? extends Expression> a, List<? extends Expression> b) throws ScopeException {
        if (a.size() != b.size()) throw new ScopeException("different list size");
        if (a.isEmpty()) throw new ScopeException("empty list");
        if (a.size() == 1)
            return EQUALS(a.get(0), b.get(0));
        List<Expression> expressions = new ArrayList<>();
        for (int i = 0; i < a.size(); i++) {
            expressions.add(EQUALS(a.get(i), b.get(i)));
        }
        return AND(expressions);
    }

    public static Expression GREATER(Expression a, Expression b) {
        return new Function(Operators.GREATER, a, b);
    }

    public static Expression AND(List<Expression> args) {
        assert !args.isEmpty();
        if (args.size() == 1)
            return args.get(0);// if singleton, this is a noop
        return new Function(Operators.AND, args);
    }

    public static Expression CONCAT(List<Expression> args) {
        assert !args.isEmpty();
        if (args.size() == 1)
            return args.get(0);// if singleton, this is a noop
        return new Function(Operators.CONCAT, args);
    }

    public static Expression STAR(View v) {
        return new Expression() {
            @Override
            public PrintResult print(Scope scope, PrintResult result) {
                try {
                    String alias = scope.get(v).getAlias();
                    result.append(alias).dot().append(Operators._STAR);
                } catch (ScopeException e) {
                    result.error(new ScopeException(e.getMessage() + " at position " + result.size()));
                    result.append(Operators._STAR);
                }
                return result;
            }

            @Override
            public FunctionType getSource() {
                return new FunctionType(v);
            }

            @Override
            public Expression redux() {
                return this;
            }
        };
    }

    public static Expression SUM(Expression a) {
        return new Function(Operators._SUM, a);
    }

    public static Expression AVG(Expression a) {
        return new Function(Operators._AVG, a);
    }

    public static Expression COUNT(Expression arg) throws ScopeException {
        if (arg instanceof View) {
            View view = (View) arg;
            List<Expression> pk = view.getPK();
            if (pk.isEmpty()) throw new ScopeException("cannot COUNT on view without a primary-key");
            return COUNT(CONCAT(pk));
        }
        return new Function(Operators._COUNT, arg) {
            @Override
            public PrintResult print(Scope scope, PrintResult result) throws ScopeException {
                return result.append(Operators._COUNT).append("(").append(Operators._DISTINCT).space().append(scope, arg).append(")");
            }
        };
    }

}
