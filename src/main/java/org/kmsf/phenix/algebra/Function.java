package org.kmsf.phenix.algebra;

import org.kmsf.phenix.database.ScopeException;
import org.kmsf.phenix.sql.PrintResult;
import org.kmsf.phenix.sql.Scope;

import java.util.*;
import java.util.stream.Collectors;

/**
 * an {@link Function} is an {@link Expression} that applies an {@link Operator} to some {@link Expression arguments}
 */
public class Function extends Expression implements Leaf {

    private Operator operator;
    private List<Expression> arguments;

    public Function(Operator operator) {
        this.operator = operator;
    }

    public Function(Operator operator, Expression a) {
        this(operator);
        this.arguments = Collections.singletonList(a);
    }

    public Function(String function, Expression a) {
        this(new Operator(function, Operator.Position.FUNCTION, PrecedenceOrder.PRECEDENCE_LEVEL_1), a);
    }

    public Function(Operator operator, Expression... args) {
        this(operator);
        this.arguments = Arrays.asList(args);
    }

    public Function(Operator operator, List<Expression> arguments) {
        this(operator);
        this.arguments = new ArrayList<>(arguments);
    }

    @Override
    public int getPrecedence() {
        return operator.getPrecedence();
    }

    @Override
    public PrintResult print(Scope scope, PrintResult result) throws ScopeException {
        return operator.print(result, (String separator) -> {
            try {
                printList(separator, scope, result);
                return Optional.empty();
            } catch (ScopeException e) {
                return Optional.of(e);
            }
        });
    }

    protected PrintResult printList(String separator, Scope scope, PrintResult result) throws ScopeException {
        for (int i = 0; i < arguments.size(); i++) {
            if (i > 0) result.append(separator);
            Expression arg = arguments.get(i);
            result.append(scope, arg, arg.getPrecedence() > getPrecedence());
        }
        return result;
    }

    @Override
    public FunctionType getSource() {
        List<Expression> values = arguments.stream().flatMap(arg -> arg.getSource().getValues().stream()).collect(Collectors.toList());
        return new FunctionType(values);
    }

    @Override
    public boolean identity(Expression fun) {
        if (fun instanceof Function) {
            if (!operator.equals(((Function) fun).operator)) return false;
            if (arguments.size() != ((Function) fun).arguments.size()) return false;
            for (int i = 0; i < arguments.size(); i++) {
                if (!arguments.get(i).equals(((Function) fun).arguments.get(i))) return false;
            }
            return true;// yes!!!
        }
        return false;
    }

}
