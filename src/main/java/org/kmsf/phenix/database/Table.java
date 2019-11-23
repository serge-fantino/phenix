package org.kmsf.phenix.database;

import org.kmsf.phenix.database.sql.PrintResult;
import org.kmsf.phenix.database.sql.Scope;
import org.kmsf.phenix.function.Function;
import org.kmsf.phenix.function.FunctionType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A Table is a View that binds to an actual database Table
 */
public class Table extends View {

    private String name;
    private Scope scope = new Scope();

    // default to false because it is killing for testing
    private boolean quoteIdentifier = false;

    private Optional<List<Function>> primaryKey = Optional.empty();

    public Table(String name) {
        this.name = name;
    }

    public Table(String name, boolean quoteIdentifier) {
        this(name);
        this.quoteIdentifier = quoteIdentifier;
    }

    public Optional<String> getName() {
        return Optional.ofNullable(name);
    }

    @Override
    public Scope getScope() {
        return scope;
    }

    public boolean isQuoteIdentifier() {
        return quoteIdentifier;
    }

    // PK support

    public Table PK(String primaryKey) throws ScopeException {
        if (this.primaryKey.isPresent()) throw new ScopeException("PrimaryKey already defined");
        this.primaryKey = Optional.ofNullable(Collections.singletonList(column(primaryKey)));
        return this;
    }

    public Table PK(String... names) throws ScopeException {
        if (this.primaryKey.isPresent()) throw new ScopeException("PrimaryKey already defined");
        this.primaryKey = Optional.of(Stream.of(names).map(name -> column(name)).collect(Collectors.toList()));
        return this;
    }

    public Table PK(Function primaryKey) throws ScopeException {
        if (this.primaryKey.isPresent()) throw new ScopeException("PrimaryKey already defined");
        this.primaryKey = Optional.ofNullable(Collections.singletonList(primaryKey));
        return this;
    }

    public Table PK(List<Function> primaryKey) throws ScopeException {
        if (this.primaryKey.isPresent()) throw new ScopeException("PrimaryKey already defined");
        this.primaryKey = Optional.ofNullable(primaryKey);
        return this;
    }

    public List<Function> getPK() {
        if (primaryKey.isPresent())
            return Collections.unmodifiableList(primaryKey.get());
        return Collections.emptyList();
    }

    public Column column(String name) {
        return new Column(this, name);
    }

    public Selector selector(String name) {
        return column(name);
    }

    public PrintResult print(Scope scope, PrintResult result) {
        return result.appendLiteral(name, quoteIdentifier);
    }

    @Override
    public FunctionType getSource() {
        return null;
    }

    @Override
    public String toString() {
        return "[TABLE '" + name + "']";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Table) {
            Table t = (Table) obj;
            return t.name.equals(this.name);
        }
        if (obj instanceof View) {
            View v = (View) obj;
            return v.getSource().equals(this);
        }
        if (obj instanceof Join) {
            View target = ((Join) obj).getTarget();
            if (target.equals(this)) return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
