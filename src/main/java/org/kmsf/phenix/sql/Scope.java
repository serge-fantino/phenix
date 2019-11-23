package org.kmsf.phenix.sql;

import org.kmsf.phenix.function.Function;
import org.kmsf.phenix.database.ScopeException;

import java.util.HashMap;
import java.util.Optional;

public class Scope {

    private HashMap<Function, Mapping> scope = new HashMap<Function, Mapping>();
    private Optional<Scope> parentScope = Optional.empty();

    public Scope() {
    }

    public Scope(Scope parent) {
        parentScope = Optional.ofNullable(parent);
    }

    public void add(Function reference, String alias) {
        scope.put(reference, new Mapping(this, reference, alias));
    }

    public Mapping get(Function reference) throws ScopeException {
        Mapping mapping = scope.get(reference);
        if (mapping != null) return mapping;
        if (parentScope.isPresent()) return parentScope.get().get(reference);
        throw new ScopeException("undefined reference to {" + reference + "} in scope");
    }

    public boolean contains(Function reference) {
        return scope.containsKey(reference);
    }

}
