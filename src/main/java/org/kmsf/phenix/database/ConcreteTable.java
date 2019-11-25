package org.kmsf.phenix.database;

import org.kmsf.phenix.function.Function;
import org.kmsf.phenix.function.FunctionType;
import org.kmsf.phenix.sql.PrintResult;
import org.kmsf.phenix.sql.Scope;

import java.util.List;
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
    public Scope getScope() {
        return model.getScope();
    }

    @Override
    public Selector selector(String name) throws ScopeException {
        return column(name);
    }

    public Column column(String name) throws ScopeException {
        for (Selector sel : model.getSelectors()) {
            if (sel.getName().equals(name)) return (Column) sel;
        }
        throw new ScopeException("column '" + name + "' is not defined in table '" + getName() + "'scope");
    }

    @Override
    public List<? extends Selector> getSelectors() {
        return model.getSelectors();
    }

    @Override
    public List<Function> getPK() {
        return model.getPK();
    }

    @Override
    public PrintResult print(Scope scope, PrintResult result) {
        return model.print(scope, result);
    }

    @Override
    public FunctionType getSource() {
        return model.getSource();
    }

    @Override
    public Function redux() {
        return model;
    }

    @Override
    public int hashCode() {
        return model.hashCode();
    }
}
