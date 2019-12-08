package org.kmsf.phenix.function;

import org.kmsf.phenix.database.ScopeException;
import org.kmsf.phenix.database.Selector;
import org.kmsf.phenix.sql.PrintResult;
import org.kmsf.phenix.sql.Scope;

import java.util.List;
import java.util.Optional;

/**
 * A ScopeFunction is a Function with a Scope attached, so the printable value will be independent from the given scope
 */
public class ScopedFunction extends Function {

    private final Scope scope;
    private final Function definition;

    public ScopedFunction(Scope scope, Function definition) {
        this.scope = scope;
        this.definition = definition;
    }

    @Override
    public PrintResult print(Scope scope, PrintResult result) throws ScopeException {
        // override local scope with this scope
        return definition.print(this.scope, result);
    }

    @Override
    public int getPrecedence() {
        return definition.getPrecedence();
    }

    @Override
    public FunctionType getType() {
        return definition.getType();
    }

    @Override
    public Optional<String> getSystemName() {
        return definition.getSystemName();
    }

    @Override
    public Optional<String> getName() {
        return definition.getName();
    }

    @Override
    public Function copy() {
        return definition.copy();
    }

    @Override
    public List<Selector> getSelectors() {
        return definition.getSelectors();
    }
}
