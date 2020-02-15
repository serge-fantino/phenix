package org.kmsf.phenix.function;

import org.kmsf.phenix.database.Column;
import org.kmsf.phenix.database.ScopeException;
import org.kmsf.phenix.database.Selector;
import org.kmsf.phenix.database.View;
import org.kmsf.phenix.sql.PrintResult;
import org.kmsf.phenix.sql.Scope;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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

    public abstract FunctionType getType();

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

    public Function copy() {
        return this;
    }

    /**
     * return the selectors from this expression.
     * If this is a operator, that will be the selectors required.
     * If this is a view, that will be the defined selectors.
     * Default implementation is to return an empty list.
     *
     * @return
     */
    public List<Selector> getSelectors() {
        return Collections.emptyList();
    }

    /**
     * try to relink the expression to the new target.
     * @param target
     * @return
     */
    public Function relinkTo(View target) {
        return this;
    }

}
