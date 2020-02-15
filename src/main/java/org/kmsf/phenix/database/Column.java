package org.kmsf.phenix.database;

import org.kmsf.phenix.algebra.Expression;
import org.kmsf.phenix.algebra.Leaf;
import org.kmsf.phenix.sql.PrintResult;
import org.kmsf.phenix.sql.Scope;
import org.kmsf.phenix.algebra.FunctionType;

import java.util.Optional;

public class Column extends Selector implements Leaf {

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

    /**
     * a column reduction is the column itself
     *
     * @return
     */
    @Override
    public Expression redux() {
        return this;
    }

    @Override
    public boolean identity(Expression fun) {
        if (fun instanceof Column) {
            Column col = (Column) fun;
            return this.table.equals(col.table) && this.name.equals(col.name);
        }
        return false;
    }

    @Override
    public String toString() {
        return "[Column '" + table.getName().orElse("$") + "'.'" + name + "']";
    }
}
