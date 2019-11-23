package org.kmsf.phenix.sql;

import org.kmsf.phenix.function.Function;

public class Mapping {

    private Scope scope;
    private Function ref;
    private String alias;

    public Mapping(Scope scope, Function ref, String alias) {
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
}
