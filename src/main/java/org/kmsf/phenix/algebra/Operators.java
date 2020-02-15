package org.kmsf.phenix.algebra;

public class Operators {

    public static final String _CONCAT = "|";
    public static final String _EQUALS = "=";
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

    public static Operator IN = new Operator(Operators._IN, Operator.Position.INFIX_FUNCTION, PrecedenceOrder.PRECEDENCE_ORDER_STATEMENT);

    public static Operator ADD = infixOperator(Operators._ADD, PrecedenceOrder.PRECEDENCE_LEVEL_4);
    public static Operator MULTIPLY = infixOperator(Operators._MULTIPLY, PrecedenceOrder.PRECEDENCE_LEVEL_3);
    public static Operator EQUALS = infixOperator(Operators._EQUALS, PrecedenceOrder.PRECEDENCE_LEVEL_7);
    public static Operator GREATER = infixOperator(Operators._GREATER, PrecedenceOrder.PRECEDENCE_LEVEL_6);

    public static Operator AND = new Operator(Operators._AND, Operator.Position.INFIX_FUNCTION, PrecedenceOrder.PRECEDENCE_LEVEL_11);

    public static Operator CONCAT = infixOperator(Operators._CONCAT, PrecedenceOrder.PRECEDENCE_LEVEL_4);

    private static Operator infixOperator(String operator, int precedence) {
        return new Operator(operator, Operator.Position.INFIX, precedence);
    }
}
