package org.kmsf.phenix.algebra;

import org.kmsf.phenix.sql.PrintResult;
import org.kmsf.phenix.sql.Scope;

/**
 * A {@link ConstExpression} is a constant expression with a parametrized type T.
 * <p>
 * The source domain for a constant expression is NULL constant.
 *
 * @param <T>
 */
public class ConstExpression<T> extends Expression {

    private T value;

    public ConstExpression(T value) {
        this.value = value;
    }

    @Override
    public PrintResult print(Scope scope, PrintResult result) {
        result.appendConstant(value);
        return result;
    }

    @Override
    public FunctionType getSource() {
        return null;
    }

    @Override
    public Expression redux() {
        return this;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj instanceof ConstExpression) {
            ConstExpression<Object> c = (ConstExpression) obj;
            return c.value.equals(this.value);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }
}
