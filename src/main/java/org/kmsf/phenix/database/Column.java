package org.kmsf.phenix.database;

import org.kmsf.phenix.sql.PrintResult;
import org.kmsf.phenix.sql.Scope;
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
    public Optional<String> getSystemName() {
        return Optional.ofNullable(name);
    }

    @Override
    public View getView() {
        return table;
    }

    public Optional<String> getName() {
        return Optional.ofNullable(name);
    }

    public PrintResult print(Scope scope, PrintResult result) {
        try {
            String alias = scope.get(table).getAlias();
            result.append(alias).append(".").appendIdentifier(name, table.isQuoteIdentifier());
        } catch (ScopeException e) {
            result.error(new ScopeException(e.getMessage() + " at position " + result.size()));
            result.appendIdentifier(name, table.isQuoteIdentifier());
        }
        return result;
    }

    @Override
    public FunctionType getSource() {
        return new FunctionType(table);
    }

    @Override
    public String toString() {
        return "[Column '" + table.getName() + "'.'" + name + "']";
    }
}
