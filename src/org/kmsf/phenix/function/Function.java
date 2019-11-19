package org.kmsf.phenix.function;

import org.kmsf.phenix.database.sql.PrintResult;
import org.kmsf.phenix.database.sql.Scope;

import java.util.Optional;

public abstract class Function {

    public static final int PRECEDENCE_ORDER_DEFAULT = 0;
    public static final int PRECEDENCE_ORDER_VIEW = 2;
    public static final int PRECEDENCE_ORDER_STATEMENT = 1;

    public abstract PrintResult print(Scope scope, PrintResult result);

    public abstract FunctionType getSource();

    public Optional<String> getName() {
        return Optional.empty();
    }

    public int getPrecedence() {
        return PRECEDENCE_ORDER_DEFAULT;
    }

}
