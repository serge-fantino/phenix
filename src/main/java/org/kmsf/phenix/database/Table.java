package org.kmsf.phenix.database;

import org.kmsf.phenix.function.Functions;
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

    private DatabaseProperties databaseProperties = new DatabaseProperties();

    private List<Column> columns = new ArrayList<>();

    private Optional<Key> primaryKey = Optional.empty();

    public Table(String name) {
        this.name = name;
    }

    public Table(String name, boolean quoteIdentifier) {
        this(name);
        this.databaseProperties.setQuoteIdentifier(quoteIdentifier);
    }

    protected Table(Table copy) {
        this.databaseProperties = copy.databaseProperties;
        this.name = copy.name;
        this.columns = new ArrayList<>(copy.columns);
        this.primaryKey = copy.primaryKey;
    }

    // create a copy of the table to prevent further modification
    public Function copy() {
        return new Table(this);
    }

    @Override
    public DatabaseProperties getDatabaseProperties() {
        return databaseProperties;
    }

    @Override
    public Optional<String> getSystemName() {
        return Optional.ofNullable(name);
    }

    @Override
    public Optional<String> getName() {
        return Optional.ofNullable(name);
    }

    public Column column(String name) throws ScopeException {
        return register(new Column(this, name));
    }

    /**
     * create a join to this Table from the Foreign key's view argument
     * @param fk
     * @return
     * @throws ScopeException
     */
    @Deprecated
    public Join join(Key fk) throws ScopeException {
        if (getPK().getKeys().isEmpty()) throw new ScopeException(this.toString()+" has no PK defined, cannot create natural join");
        if (getPK().getKeys().size()!=fk.getKeys().size()) throw new ScopeException("cannot create natural join, keys size are different: "+this.getPK()+" versus "+fk);
        try {
            return new Join(fk.getView(), this, Functions.EQUALS(getPK().getKeys(), fk.getKeys()));
        } catch (ScopeException e) {
            throw new ScopeException("failed to create natural join from "+this+" to "+fk+": "+e.getMessage(), e);
        }
    }

    /**
     * Join this View using thew ithForeignKeys to the Target view PK
     * @param target
     * @param withForeignKeys
     * @return
     */
    public Join join(View target, String ... withForeignKeys) throws ScopeException {
        return join(target.getName().get(), target, withForeignKeys);
    }

    public Join join(String name, View target, String ... withForeignKeys) throws ScopeException {
        if (target.getPK().getKeys().isEmpty()) throw new ScopeException(target.toString()+" has no PK defined, cannot create natural join");
        if (target.getPK().getKeys().size()!=withForeignKeys.length) throw new ScopeException("cannot create natural join, keys size are different: "+target.getPK()+" versus "+withForeignKeys);
        ArrayList<Function> keys = new ArrayList<>();
        for (String key : withForeignKeys) {
            keys.add(column(key));
        }
        return new Join(this, keys, target, target.getPK().getKeys());
    }

    public Join oppositeJoin(View target, String ... withForeignKeys) throws ScopeException {
        if (this.getPK().getKeys().isEmpty()) throw new ScopeException(this.toString()+" has no PK defined, cannot create natural join");
        if (this.getPK().getKeys().size()!=withForeignKeys.length) throw new ScopeException("cannot create natural join, keys size are different: "+this.getPK()+" versus "+withForeignKeys);
        ArrayList<Function> keys = new ArrayList<>();
        for (String keyName : withForeignKeys) {
            Optional<Selector> key = target.selector(keyName);
            if (key.isEmpty()) throw new ScopeException("key "+keyName+" is not defined in "+target+" scope");
            keys.add(key.get());
        }
        return new Join(this, this.getPK().getKeys(), target, keys);
    }

    @Override
    public Optional<Selector> selector(String name) {
        try {
            return Optional.of(column(name));
        } catch (ScopeException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<Selector> accept(View from, Selector selector) {
        return selector.getView().isCompatibleWith(this)?Optional.of(selector):Optional.empty();
    }

    @Override
    public boolean inheritsFrom(View view) {
        return view.equals(this);
    }

    @Override
    public List<Selector> getSelectors() {
        return Collections.unmodifiableList(this.columns);
    }

    protected Column register(Column column) throws ScopeException {
        if (!column.getView().equals(this)) throw new ScopeException("cannot register a column from a different table");
        if (columns.contains(column)) return columns.get(columns.indexOf(column));
        columns.add(column);
        return column;
    }

    // PK support

    public Table PK(String... names) throws ScopeException {
        if (this.primaryKey.isPresent()) throw new ScopeException("PrimaryKey already defined");
        List<Selector> keys = new ArrayList<>();
        for (String name : names)
            keys.add(column(name));
        this.primaryKey = Optional.of(new Key(this, keys));
        return this;
    }

    public Table PK(Selector key) throws ScopeException {
        if (this.primaryKey.isPresent()) throw new ScopeException("PrimaryKey already defined");
        if (!key.getView().equals(this)) throw new ScopeException("the key doesn't belong to this table");
        this.primaryKey = Optional.ofNullable(new Key(this, Collections.singletonList(key)));
        return this;
    }

    public Table PK(List<Selector> keys) throws ScopeException {
        if (this.primaryKey.isPresent()) throw new ScopeException("PrimaryKey already defined");
        for (Selector key : keys) {
            if (!key.getView().equals(this)) throw new ScopeException("a key doesn't belong to this table");
        }
        this.primaryKey = Optional.ofNullable(new Key(this, keys));
        return this;
    }

    public Key getPK() {
        if (primaryKey.isPresent()) return primaryKey.get();
        return new Key(this);
    }

    // foreign-key constructor
    public Key FK(String... names) throws ScopeException {
        List<Function> keys = new ArrayList<>();
        for (String name : names)
            keys.add(column(name));
        return new Key(this, keys);
    }

    public PrintResult print(Scope scope, PrintResult result) {
        return result.appendIdentifier(name, databaseProperties.isQuoteIdentifier());
    }

    @Override
    public FunctionType getType() {
        return new FunctionType(this);
    }

    @Override
    public String toString() {
        return "[TABLE '" + name + "']";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Table table = (Table) o;
        return name.equals(table.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

}
