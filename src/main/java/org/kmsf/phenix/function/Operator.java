package org.kmsf.phenix.function;

import org.kmsf.phenix.database.ScopeException;
import org.kmsf.phenix.database.View;
import org.kmsf.phenix.sql.PrintResult;
import org.kmsf.phenix.sql.Scope;

import java.util.*;
import java.util.stream.Collectors;

/**
 * an Operator is function transformation that applies an operator to arguments
 */
public class Operator extends Function {

    private static final String OPERATOR_ALIAS = "x";

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

    public Operator(String operator, Position position, int precedence, List<? extends Function> arguments) {
        this(operator, position, precedence);
        this.arguments = new ArrayList<>(arguments);
    }

    protected Operator(Operator copy, List<Function> override) {
        this(copy.operator, copy.position, copy.precedence);
        this.arguments = new ArrayList<>(override);
    }

    @Override
    public Function copy() {
        return copy(this.arguments);
    }

    public Function copy(List<Function> override) {
        return new Operator(this, override);
    }

    @Override
    public int getPrecedence() {
        return precedence;
    }

    public List<Function> getArguments() {
        return Collections.unmodifiableList(arguments);
    }

    @Override
    public Optional<String> getName() {
        return Optional.of(OPERATOR_ALIAS);
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
    public FunctionType getType() {
        List<View> values = arguments.stream().flatMap(arg -> arg.getType().getValues().stream()).collect(Collectors.toList());
        return new FunctionType(values);
    }

    @Override
    public Function relinkTo(View target) {
        List<Function> override = getArguments().stream().map(fun -> fun.relinkTo(target)).collect(Collectors.toList());
        return copy(override);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Operator operator1 = (Operator) o;
        return operator.equals(operator1.operator) &&
                arguments.equals(operator1.arguments);
    }

    @Override
    public int hashCode() {
        return Objects.hash(operator, arguments);
    }

    @Override
    public String toString() {
        StringBuffer result = new StringBuffer(operator).append("[");
        for (int i = 0; i < arguments.size(); i++) {
            if (i > 0) result.append(",");
            Function arg = arguments.get(i);
            result.append("(").append(arg.toString()).append(")");
        }
        result.append("]");
        return result.toString();
    }
}
