package org.kmsf.phenix.function;

import org.kmsf.phenix.database.ScopeException;
import org.kmsf.phenix.sql.PrintResult;
import org.kmsf.phenix.sql.Scope;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * an Operator is function transformation that applies an operator to arguments
 */
public class Operator extends Function implements Leaf {

    enum Position {
        PREFIX,
        INFIX,
        POSTFIX,
        FUNCTION,
        INFIX_FUNCTION
    }

    private String operator;
    private Position position;
    private int precedence;
    private List<Function> arguments;

    protected Operator(String operator, Position position, int precedence) {
        this.operator = operator;
        this.position = position;
        this.precedence = precedence;
    }

    public Operator(String operator, Position position, int precedence, Function a) {
        this(operator, position, precedence);
        this.arguments = Collections.singletonList(a);
    }

    public Operator(String function, Function a) {
        this(function, Operator.Position.FUNCTION, Function.PRECEDENCE_LEVEL_1, a);
    }

    public Operator(String operator, Position position, int precedence, Function... args) {
        this(operator, position, precedence);
        this.arguments = Arrays.asList(args);
    }

    public Operator(String operator, Position position, int precedence, List<Function> arguments) {
        this(operator, position, precedence);
        this.arguments = new ArrayList<>(arguments);
    }

    @Override
    public int getPrecedence() {
        return precedence;
    }

    @Override
    public PrintResult print(Scope scope, PrintResult result) throws ScopeException {
        if (position == Position.PREFIX) result.append(operator);
        if (position == Position.FUNCTION) result.append(operator).append("(");
        printList(getSeparator(), scope, result);
        if (position == Position.FUNCTION) result.append(")");
        if (position == Position.POSTFIX) result.append(operator);
        return result;
    }

    protected String getSeparator() {
        switch (position) {
            case INFIX:
                return operator;
            case INFIX_FUNCTION:
                return " " + operator + " ";
            default:
                return ",";
        }
    }

    protected PrintResult printList(String separator, Scope scope, PrintResult result) throws ScopeException {
        for (int i = 0; i < arguments.size(); i++) {
            if (i > 0) result.append(separator);
            Function arg = arguments.get(i);
            result.append(scope, arg, arg.getPrecedence() > precedence);
        }
        return result;
    }

    @Override
    public FunctionType getSource() {
        List<Function> values = arguments.stream().flatMap(arg -> arg.getSource().getValues().stream()).collect(Collectors.toList());
        return new FunctionType(values);
    }

    @Override
    public boolean identity(Function fun) {
        if (fun instanceof Operator) {
            if (!operator.equals(((Operator) fun).operator)) return false;
            if (arguments.size() != ((Operator) fun).arguments.size()) return false;
            for (int i = 0; i < arguments.size(); i++) {
                if (!arguments.get(i).equals(((Operator) fun).arguments.get(i))) return false;
            }
            return true;// yes!!!
        }
        return false;
    }

}
