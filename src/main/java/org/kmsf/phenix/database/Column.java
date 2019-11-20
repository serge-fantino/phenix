package org.kmsf.phenix.database;

import org.kmsf.phenix.database.sql.PrintResult;
import org.kmsf.phenix.database.sql.Scope;
import org.kmsf.phenix.function.FunctionType;

import java.util.Optional;

public class Column extends Selector {

    private Table table;
    private String name;

    public Column(Table table, String name) {
        this.table = table;
        this.name = name;
    }

    @Override
    public Optional<String> getDefaultAlias() {
        return Optional.ofNullable(name);
    }

    public Optional<String> getName() {
        return Optional.ofNullable(name);
    }

    public PrintResult print(Scope scope, PrintResult result) {
        try {
            String alias = scope.get(table).getAlias();
            result.append(alias).append(".").appendLiteral(name);
        } catch (ScopeException e) {
            result.error(new ScopeException(e.getMessage() + " at position " + result.size()));
            result.appendLiteral(name);
        }
        return result;
    }

    @Override
    public FunctionType getSource() {
        return new FunctionType(table);
    }

}
