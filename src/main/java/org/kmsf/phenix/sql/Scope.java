package org.kmsf.phenix.sql;

import org.kmsf.phenix.algebra.Expression;
import org.kmsf.phenix.database.ScopeException;

import java.util.HashMap;
import java.util.Optional;

public class Scope {

    private HashMap<Expression, Mapping> scope = new HashMap<Expression, Mapping>();
    private Optional<Scope> parentScope = Optional.empty();

    public Scope() {
    }

    public Scope(Scope parent) {
        parentScope = Optional.ofNullable(parent);
    }

    public void add(Expression reference, String alias) {
        scope.put(reference, new Mapping(this, reference, alias));
    }

    public Mapping get(Expression reference) throws ScopeException {
        Mapping mapping = scope.get(reference);
        if (mapping != null) return mapping;
        if (parentScope.isPresent()) return parentScope.get().get(reference);
        throw new ScopeException("undefined reference to {" + reference + "} in scope " + this.toString());
    }

    public boolean contains(Expression reference) {
        return scope.containsKey(reference);
    }

    @Override
    public String toString() {
        StringBuffer result = new StringBuffer(parentScope.isPresent() ? parentScope.toString() + "+" : "[");
        scope.values().forEach(mapping -> result.append(mapping.toString()).append(","));
        result.append("]");
        return result.toString();
    }
}
