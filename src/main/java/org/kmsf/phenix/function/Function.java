package org.kmsf.phenix.function;

import org.kmsf.phenix.database.ScopeException;
import org.kmsf.phenix.sql.PrintResult;
import org.kmsf.phenix.sql.Scope;

import java.util.Optional;

public abstract class Function {

    // https://en.wikipedia.org/wiki/Order_of_operations

    public static final int PRECEDENCE_ORDER_DEFAULT = 0;

    /**
     * Function call, scope, array/member access
     */
    public static final int PRECEDENCE_LEVEL_1 = 1;

    /**
     * Multiplication, division, modulo
     */
    public static final int PRECEDENCE_LEVEL_3 = 3;

    /**
     * Addition and subtraction
     */
    public static final int PRECEDENCE_LEVEL_4 = 4;

    /**
     * Comparisons: less-than and greater-than
     */
    public static final int PRECEDENCE_LEVEL_6 = 6;

    /**
     * Comparisons: equal and not equal
     */
    public static final int PRECEDENCE_LEVEL_7 = 7;

    /**
     * Logical AND
     */
    public static final int PRECEDENCE_LEVEL_11 = 11;

    public static final int PRECEDENCE_ORDER_STATEMENT = 16;

    public static final int PRECEDENCE_ORDER_VIEW = 17;

    public abstract PrintResult print(Scope scope, PrintResult result) throws ScopeException;

    public abstract FunctionType getSource();

    /**
     * return the default name of the function, as defined by the system, that is without taking user modifiction into account
     *
     * @return
     */
    public Optional<String> getSystemName() {
        return Optional.empty();
    }

    public Optional<String> getName() {
        return Optional.empty();
    }

    public int getPrecedence() {
        return PRECEDENCE_ORDER_DEFAULT;
    }

    /**
     * return this function value reduction, i.e. for a Function f, return a Function fx that verifies f.equals(fx) && redux(fx)==fx
     *
     * @return
     */
    public Function redux() {
        return this;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj instanceof Function) {
            return redux().identity(((Function) obj).redux());
        }
        return false;
    }

    /**
     * this is the identity method that check if this can be replaced by fun in a expression.
     * Subclass may override to solve "leaf" cases, i.e. when redux(X).class==X.class
     *
     * @param fun
     * @return
     */
    public boolean identity(Function fun) {
        return this == fun;
    }
}
