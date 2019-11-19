package org.kmsf.phenix.database;

import org.kmsf.phenix.database.sql.PrintResult;
import org.kmsf.phenix.database.sql.Scope;
import org.kmsf.phenix.function.FunctionType;

import java.util.Optional;

public class Table extends View {

    private String name;

    public Table(String name) {
        this.name = name;
    }

    public Optional<String> getName() {
        return Optional.ofNullable(name);
    }

    public Column column(String name) {
        return new Column(this, name);
    }

    public Selector selector(String name) { return column(name);}

    public PrintResult print(Scope scope, PrintResult result) {
        return result.appendLiteral(name);
    }

    @Override
    public FunctionType getSource() {
        return null;
    }

    @Override
    public String toString() {
        return "{TABLE '"+name+"'}";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Table) {
            Table t = (Table)obj;
            return t.name.equals(this.name);
        }
        if (obj instanceof View) {
            View v = (View)obj;
            return v.getSource().equals(this);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
