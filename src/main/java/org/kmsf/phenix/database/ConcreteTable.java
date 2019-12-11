package org.kmsf.phenix.database;

import org.kmsf.phenix.function.FunctionType;
import org.kmsf.phenix.sql.PrintResult;
import org.kmsf.phenix.sql.Scope;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * A Concrete Table is a Table with a well define selectors that cannot be modified
 */
public class ConcreteTable extends View {

    private final Table model;

    public ConcreteTable(Table model) {
        this.model = model;
    }

    @Override
    public Optional<String> getSystemName() {
        return model.getSystemName();
    }

    @Override
    public Optional<String> getName() {
        return model.getName();
    }

    @Override
    public Optional<Selector> selector(String name) {
        try {
            return Optional.ofNullable(column(name));
        } catch (ScopeException e) {
            return Optional.empty();
        }
    }

    public Column column(String name) throws ScopeException {
        for (Selector sel : model.getSelectors()) {
            if (sel.getName().equals(name)) return (Column) sel;
        }
        throw new ScopeException("column '" + name + "' is not defined in table '" + getName() + "'scope");
    }

    @Override
    public List<Selector> getSelectors() {
        return model.getSelectors();
    }

    @Override
    public Optional<Selector> accept(View from, Selector selector) {
        // the concrete implementation only accept existing columns
        return getSelectors().contains(selector)?Optional.of(selector):Optional.empty();
    }

    @Override
    public boolean inheritsFrom(View parent) {
        return model.isCompatibleWith(parent);
    }

    @Override
    public Key getPK() {
        return model.getPK();
    }

    @Override
    public PrintResult print(Scope scope, PrintResult result) {
        return model.print(scope, result);
    }

    @Override
    public FunctionType getType() {
        return model.getType();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConcreteTable that = (ConcreteTable) o;
        return Objects.equals(model, that.model);
    }

    @Override
    public int hashCode() {
        return model.hashCode();
    }

    @Override
    public String toString() {
        return "[ConcreteTable "+model.getName().orElse("???")+"]";
    }
}
