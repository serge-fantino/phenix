package org.kmsf.phenix.function;

import org.kmsf.phenix.database.sql.PrintResult;
import org.kmsf.phenix.database.sql.Scope;

import java.util.Optional;

public abstract class Function {

    public static final int PRECEDENCE_ORDER_DEFAULT = 0;

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

    public static final int PRECEDENCE_ORDER_STATEMENT = 16;

    public static final int PRECEDENCE_ORDER_VIEW = 17;

    public abstract PrintResult print(Scope scope, PrintResult result);

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

}
