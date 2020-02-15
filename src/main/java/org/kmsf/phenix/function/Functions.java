package org.kmsf.phenix.function;

import org.kmsf.phenix.database.ScopeException;
import org.kmsf.phenix.database.View;
import org.kmsf.phenix.sql.PrintResult;
import org.kmsf.phenix.sql.Scope;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class Functions {

    public static final String _CONCAT = "|";
    public static final String _EQUALS = "=";
    public static final String _NOTEQUALS = "!=";
    public static final String _GREATER = ">";
    public static final String _MULTIPLY = "*";
    public static final String _ADD = "+";
    public static final String _SUM = "SUM";
    public static final String _AVG = "AVG";
    public static final String _AND = "AND";
    public static final String _COUNT = "COUNT";
    public static final String _DISTINCT = "DISTINCT";
    public static final String _IN = "IN";
    public static final String _STAR = "*";

    public static <T> Function CONST(T value) {
        return new ConstFunction<T>(value);
    }

    public static Function IN(Function a, Function b) {
        return new InOperator(a, b);
    }

    public static Function ADD(Function a, Function b) {
        return infixOperator(_ADD, Function.PRECEDENCE_LEVEL_4, a, b);
    }

    public static Function MULTIPLY(Function a, Function b) {
        return infixOperator(_MULTIPLY, Function.PRECEDENCE_LEVEL_3, a, b);
    }

    public static Function EQUALS(Function a, Function b) {
        return infixOperator(_EQUALS, Function.PRECEDENCE_LEVEL_7, a, b);
    }

    public static Function NOTEQUALS(Function a, Function b) {
        return infixOperator(_NOTEQUALS, Function.PRECEDENCE_LEVEL_7, a, b);
    }

    public static Function EQUALS(List<? extends Function> a, List<? extends Function> b) throws ScopeException {
        if (a.size() != b.size()) throw new ScopeException("EQUALS: different list size");
        if (a.isEmpty()) throw new ScopeException("EQUALS: empty list");
        if (a.size() == 1)
            return EQUALS(a.get(0), b.get(0));
        List<Function> functions = new ArrayList<>();
        for (int i = 0; i < a.size(); i++) {
            functions.add(EQUALS(a.get(i), b.get(i)));
        }
        return AND(functions);
    }

    public static Function GREATER(Function a, Function b) {
        return infixOperator(_GREATER, Function.PRECEDENCE_LEVEL_6, a, b);
    }

    public static Function AND(List<? extends Function> args) {
        assert !args.isEmpty();
        if (args.size() == 1)
            return args.get(0);// if singleton, this is a noop
        return new Operator(_AND, Operator.Position.INFIX_FUNCTION, Function.PRECEDENCE_LEVEL_11, args);
    }

    public static Function CONCAT(List<Function> args) {
        assert !args.isEmpty();
        if (args.size() == 1)
            return args.get(0);// if singleton, this is a noop
        return new Operator(_CONCAT, Operator.Position.INFIX, Function.PRECEDENCE_LEVEL_4, args);
    }

    public static Function STAR(View v) {
        return new StarOperator(v);
    }

    public static Function SUM(Function a) {
        return new Operator(_SUM, a);
    }

    public static Function AVG(Function a) {
        return new Operator(_AVG, a);
    }

    public static Function COUNT(Function arg) throws ScopeException {
        if (arg instanceof View) {
            View view = (View) arg;
            List<Function> pk = view.getPK().getKeys();
            if (pk.isEmpty()) throw new ScopeException("cannot COUNT on view without a primary-key");
            return new CountOperator(CONCAT(pk));
        }
        return new CountOperator(arg);
    }

    private static Function infixOperator(String operator, int precedence, Function a, Function b) {
        return new Operator(operator, Operator.Position.INFIX, precedence, a, b);
    }

    static class CountOperator extends Operator {

        private Function distinct;

        public CountOperator(Function distinct) {
            super(_COUNT, distinct);
            this.distinct = distinct;
        }

        @Override
        public Function copy(List<Function> override) {
            assert override.size()==1;
            return new CountOperator(override.get(0));
        }

        @Override
        public PrintResult print(Scope scope, PrintResult result) throws ScopeException {
            return result.append(_COUNT).append("(").append(_DISTINCT).space().append(scope, distinct).append(")");
        }

    }

    static class InOperator extends Operator {

        public InOperator(Function a, Function b) {
            super(_IN, Operator.Position.INFIX_FUNCTION, Function.PRECEDENCE_ORDER_STATEMENT, a, b);
        }

        public PrintResult print(Scope scope, PrintResult result) throws ScopeException {
            result.append(scope, getArguments().get(0)).space().append(_IN).space().append("(").append(scope, getArguments().get(1)).append(")");
            return result;
        }

        @Override
        public Function copy(List<Function> override) {
            assert override.size()==2;
            return new InOperator(override.get(0), override.get(1));
        }

    }

    static class StarOperator extends Operator {

        private View view;

        public StarOperator(View view) {
            super(_STAR, view);
            this.view = view;
        }

        @Override
        public Optional<String> getName() {
            return Optional.empty();
        }

        @Override
        public Function copy(List<Function> override) {
            return new StarOperator(view);
        }

        @Override
        public PrintResult print(Scope scope, PrintResult result) {
            try {
                String alias = scope.resolves(view).getAlias();
                result.append(alias).dot().append(_STAR);
            } catch (ScopeException e) {
                result.error(new ScopeException(e.getMessage() + " at position " + result.size()));
                result.append(_STAR);
            }
            return result;
        }

        @Override
        public FunctionType getType() {
            return new FunctionType(view);
        }

    }

}
