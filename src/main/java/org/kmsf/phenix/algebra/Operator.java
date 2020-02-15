package org.kmsf.phenix.algebra;

import org.kmsf.phenix.database.ScopeException;
import org.kmsf.phenix.sql.PrintResult;
import org.kmsf.phenix.sql.Scope;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * An {@link Operator} defines a specific operation with name, operator {@link Position} and {@link PrecedenceOrder}.
 * <p>
 * The {@link Operator} also handles printing a function based on this operator definition with the {@link #print(PrintResult, Function)} method.
 */
public class Operator {

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

    protected Operator(String operator, Position position, int precedence) {
        this.operator = operator;
        this.position = position;
        this.precedence = precedence;
    }

    public int getPrecedence() {
        return precedence;
    }

    public PrintResult print(PrintResult result, Function<String, Optional<ScopeException>> listPrinter) throws ScopeException {
        if (position == Position.PREFIX) result.append(operator);
        if (position == Position.FUNCTION) result.append(operator).append("(");
        Optional<ScopeException> error = listPrinter.apply(getSeparator());
        if (error.isPresent()) throw error.get();
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
}
