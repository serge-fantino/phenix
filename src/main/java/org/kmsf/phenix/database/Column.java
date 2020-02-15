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
        return print(getView(), scope, result);
    }

    protected PrintResult print(View view, Scope scope, PrintResult result) {
        try {
            //logger.info("printing "+this+" from "+scope);
            String alias = scope.resolves(view).getAlias();
            result.append(alias).append(".").appendIdentifier(name, view.getDatabaseProperties().isQuoteIdentifier());
        } catch (ScopeException e) {
            result.error(
                    new ScopeException(e.getMessage() + " at position " + result.size() + " while looking for "+this));
            result.appendIdentifier(name, view.getDatabaseProperties().isQuoteIdentifier());
        }
        return result;
    }

    @Override
    public FunctionType getType() {
        return new FunctionType(table);
    }

    @Override
    public Function relinkTo(View target) {
        if (!target.equals(table) && target.inheritsFrom(table)) {
            // construct a wrapper that allow overriding the view definition
            return new RelinkWrapper(target, this);
        }
        // else
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o instanceof Selector) {
            o = ((Selector)o).unwrapReference();
        }
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

    class RelinkWrapper extends Selector {

        private View target;
        private Column column;

        public RelinkWrapper(View target, Column column) {
            this.target = target;
            this.column = column;
        }

        @Override
        public Optional<String> getName() {
            return column.getName();
        }

        @Override
        public Optional<String> getSystemName() {
            return column.getSystemName();
        }

        @Override
        public PrintResult print(Scope scope, PrintResult result) throws ScopeException {
            // override the underlying selector with the from view
            return column.print(target, scope, result);
        }

        @Override
        public FunctionType getType() {
            return new FunctionType(target);
        }

        @Override
        public View getView() {
            return target;
        }

        @Override
        public Optional<Function> asSelectorValue() {
            return column.asSelectorValue();
        }

        @Override
        public Function relinkTo(View target) {
            //Function relink = column.relinkTo(target);
            //return relink;
            //
            if (target.equals(column.getView())) return column;// unwrap
            //
            if (target.inheritsFrom(this.target)) {
                // construct a wrapper that allow overriding the view definition
                return new RelinkWrapper(target, column);
            }
            //
            // else
            return this;
            //
        }

        @Override
        public String toString() {
            return "[RELINK "+column+"]";
        }

        @Override
        public Function unwrapReference() {
            return column;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj==this) return true;
            return column.equals(obj);
        }

        @Override
        public int hashCode() {
            return column.hashCode();
        }

    }
}
