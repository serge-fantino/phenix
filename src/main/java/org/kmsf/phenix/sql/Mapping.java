package org.kmsf.phenix.sql;

import org.kmsf.phenix.algebra.Expression;

public class Mapping {

    private Scope scope;
    private Expression ref;
    private String alias;

    public Mapping(Scope scope, Expression ref, String alias) {
        this.scope = scope;
        this.ref = ref;
        this.alias = alias;
    }

    public Scope getScope() {
        return this.scope;
    }

    public String getAlias() {
        return this.alias;
    }

    @Override
    public String toString() {
        return "(" + alias + "=" + ref.toString() + ")";
    }
}
