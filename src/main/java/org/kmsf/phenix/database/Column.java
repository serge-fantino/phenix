package org.kmsf.phenix.database;

import org.kmsf.phenix.function.Function;
import org.kmsf.phenix.sql.PrintResult;
import org.kmsf.phenix.sql.Scope;
import org.kmsf.phenix.function.FunctionType;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Logger;

public class Column extends Selector {

    private static Logger logger = Logger.getLogger(Column.class.getName());

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

    @Override
    public Optional<String> getName() {
        return Optional.ofNullable(name);
    }

    @Override
    public Optional<Function> asSelectorValue() {
        return Optional.of(this);
    }

    @Override
    public List<Selector> getSelectors() {
        return Collections.singletonList(this);
    }

    public PrintResult print(Scope scope, PrintResult result) {
        try {
            logger.info("printing "+this+" from "+scope);
            String alias = scope.resolves(table).getAlias();
            result.append(alias).append(".").appendIdentifier(name, table.isQuoteIdentifier());
        } catch (ScopeException e) {
            result.error(
                    new ScopeException(e.getMessage() + " at position " + result.size() + " while looking for "+this));
            result.appendIdentifier(name, table.isQuoteIdentifier());
        }
        return result;
    }

    @Override
    public FunctionType getType() {
        return new FunctionType(table);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Column column = (Column) o;
        return table.equals(column.table) &&
                name.equals(column.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(table, name);
    }

    @Override
    public String toString() {
        return "[Column '" + table.getName().orElse("$") + "'.'" + name + "']";
    }
}
