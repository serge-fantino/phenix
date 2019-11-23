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
    public FunctionType getSource() {
        return null;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj instanceof ConstFunction) {
            ConstFunction<Object> c = (ConstFunction) obj;
            return c.value.equals(this.value);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }
}
