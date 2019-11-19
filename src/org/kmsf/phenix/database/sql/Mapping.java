package org.kmsf.phenix.database.sql;

import org.kmsf.phenix.function.Function;

public class Mapping {

    private Function ref;
    private String alias;

    public Mapping(Function ref, String alias) {
        this.ref = ref;
        this.alias = alias;
    }

    public String getAlias() {
        return this.alias;
    }
}
