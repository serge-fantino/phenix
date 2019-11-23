package org.kmsf.phenix.database;

import org.kmsf.phenix.sql.PrintResult;
import org.kmsf.phenix.sql.Scope;
import org.kmsf.phenix.function.Function;
import org.kmsf.phenix.function.FunctionType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * A Table is a View that binds to an actual database Table
 */
public class Table extends View {

    private String name;
    private Scope scope = new Scope();

    private List<Column> columns = new ArrayList<>();

    // default to false because it is killing for testing
    private boolean quoteIdentifier = false;

    private Optional<List<Selector>> primaryKey = Optional.empty();

    public Table(String name) {
        this.name = name;
    }

    public Table(String name, boolean quoteIdentifier) {
        this(name);
        this.quoteIdentifier = quoteIdentifier;
    }

    public boolean isQuoteIdentifier() {
        return quoteIdentifier;
    }

    @Override
    public Optional<String> getSystemName() {
        return Optional.ofNullable(name);
    }

    @Override
    public Optional<String> getName() {
        return Optional.ofNullable(name);
    }

    @Override
    public Scope getScope() {
        return scope;
    }

    protected Column register(Column column) throws ScopeException {
        if (!column.getView().equals(this)) throw new ScopeException("cannot register a column from a different table");
        if (columns.contains(column)) return columns.get(columns.indexOf(column));
        columns.add(column);
        return column;
    }

    public Column column(String name) throws ScopeException {
        return register(new Column(this, name));
    }

    @Override
    public Selector selector(String name) throws ScopeException {
        return column(name);
    }

    @Override
    public List<? extends Selector> getSelectors() {
        return this.columns;
    }

    // PK support

    public Table PK(String primaryKey) throws ScopeException {
        if (this.primaryKey.isPresent()) throw new ScopeException("PrimaryKey already defined");
        this.primaryKey = Optional.ofNullable(Collections.singletonList(column(primaryKey)));
        return this;
    }

    public Table PK(String... names) throws ScopeException {
        if (this.primaryKey.isPresent()) throw new ScopeException("PrimaryKey already defined");
        List<Selector> keys = new ArrayList<>();
        for (String name : names)
            keys.add(column(name));
        this.primaryKey = Optional.of(keys);
        return this;
    }

    public Table PK(Selector key) throws ScopeException {
        if (this.primaryKey.isPresent()) throw new ScopeException("PrimaryKey already defined");
        if (!key.getView().equals(this)) throw new ScopeException("the key doesn't belong to this table");
        this.primaryKey = Optional.ofNullable(Collections.singletonList(key));
        return this;
    }

    public Table PK(List<Selector> keys) throws ScopeException {
        if (this.primaryKey.isPresent()) throw new ScopeException("PrimaryKey already defined");
        for (Selector key : keys) {
            if (!key.getView().equals(this)) throw new ScopeException("a key doesn't belong to this table");
        }
        this.primaryKey = Optional.ofNullable(keys);
        return this;
    }

    public List<Function> getPK() {
        if (primaryKey.isPresent())
            return Collections.unmodifiableList(primaryKey.get());
        return Collections.emptyList();
    }

    public PrintResult print(Scope scope, PrintResult result) {
        return result.appendIdentifier(name, quoteIdentifier);
    }

    @Override
    public FunctionType getSource() {
        return new FunctionType();
    }

    @Override
    public String toString() {
        return "[TABLE '" + name + "']";
    }

    @Override
    public boolean equals(Object obj) {
        if (super.equals(obj)) return true;
        if (obj instanceof Table) {
            Table t = (Table) obj;
            return t.name.equals(this.name);
        }
        if (obj instanceof ConcreteTable) {
            ConcreteTable t = (ConcreteTable) obj;
            return t.equals(this);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
