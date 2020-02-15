package org.kmsf.phenix.algebra;

/**
 * The interface PrecedenceOrder defines constant values to manage precedence order in function, as describe in https://en.wikipedia.org/wiki/Order_of_operations
 */
public interface PrecedenceOrder {

    int PRECEDENCE_ORDER_DEFAULT = 0;
    /**
     * Function call, scope, array/member access
     */
    int PRECEDENCE_LEVEL_1 = 1;
    /**
     * Multiplication, division, modulo
     */
    int PRECEDENCE_LEVEL_3 = 3;
    /**
     * Addition and subtraction
     */
    int PRECEDENCE_LEVEL_4 = 4;
    /**
     * Comparisons: less-than and greater-than
     */
    int PRECEDENCE_LEVEL_6 = 6;
    /**
     * Comparisons: equal and not equal
     */
    int PRECEDENCE_LEVEL_7 = 7;
    /**
     * Logical AND
     */
    int PRECEDENCE_LEVEL_11 = 11;
    int PRECEDENCE_ORDER_STATEMENT = 16;
    int PRECEDENCE_ORDER_VIEW = 17;
}
