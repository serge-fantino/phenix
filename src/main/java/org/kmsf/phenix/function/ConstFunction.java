package org.kmsf.phenix.function;

import org.kmsf.phenix.sql.PrintResult;
import org.kmsf.phenix.sql.Scope;

public class ConstFunction<T> extends Function {

    private T value;

    public ConstFunction(T value) {
        this.value = value;
    }

    @Override
    public PrintResult print(Scope scope, PrintResult result) {
        result.appendConstant(value);
        return result;
    }

    @Override
    public FunctionType getType() {
        return new FunctionType();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConstFunction<?> that = (ConstFunction<?>) o;
        return value.equals(that.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }
}
