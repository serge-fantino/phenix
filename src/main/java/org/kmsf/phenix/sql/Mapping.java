package org.kmsf.phenix.sql;

import org.kmsf.phenix.database.View;
import org.kmsf.phenix.function.Function;

public class Mapping {

    private Scope scope;
    private View view;
    private String alias;

    public Mapping(Scope scope, View view, String alias) {
        this.scope = scope;
        this.view = view;
        this.alias = alias;
    }

    public Scope getScope() {
        return this.scope;
    }

    public String getAlias() {
        return this.alias;
    }

    public View getReference() { return this.view; }

    @Override
    public String toString() {
        return "(" + alias + "=" + view.toString() + ")";
    }
}
