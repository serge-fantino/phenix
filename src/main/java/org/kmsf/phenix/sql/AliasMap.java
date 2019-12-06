package org.kmsf.phenix.sql;

import java.util.HashSet;
import java.util.Optional;

public class AliasMap {

    private Character alias = 'a';
    private HashSet<String> aliases = new HashSet<>();

    public AliasMap() {
        //
    }

    public AliasMap(AliasMap copy) {
        this.alias = copy.alias;
        this.aliases = new HashSet<>(copy.aliases);
    }

    public String getViewAlias(Optional<String> name) {
        if (name.isPresent())
            return getAlias(name.get().substring(0, 1));
        else
            return getAlias((alias++).toString());
    }

    public Optional<String> getAlias(Optional<String> name) {
        if (name.isPresent())
            return Optional.of(getAlias(name.get()));
        else
            return name;
    }

    private String getAlias(String name) {
        int idx = 1;
        String alias = name;
        while (aliases.contains(alias)) {
            alias = name + (idx++);
        }
        aliases.add(alias);
        return alias;
    }
}
