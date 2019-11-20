package org.kmsf.phenix.logical;

import org.kmsf.phenix.database.Selector;
import org.kmsf.phenix.database.Table;
import org.kmsf.phenix.database.View;
import org.kmsf.phenix.database.sql.PrintResult;
import org.kmsf.phenix.database.sql.Scope;
import org.kmsf.phenix.function.FunctionType;
import org.kmsf.phenix.function.Function;

import java.util.Optional;

public class Entity extends View {

    private String name;
    private View view;

    public Entity(String name, View view) {
        this.name = name;
        this.view = view;
    }

    public Optional<String> getName() {
        return Optional.ofNullable(name);
    }

    public Attribute attribute(String name, Function expr) {
        FunctionType source = expr.getSource();
        if (!source.contains(view)) throw new RuntimeException("invalid attribute");
        return new Attribute(this, name, expr);
    }

    public Attribute attribute(String name) {
        Selector expr = view.selector(name);
        FunctionType source = expr.getSource();
        if (!source.contains(view)) throw new RuntimeException("invalid attribute");
        return new Attribute(this, name, expr);
    }

    @Override
    public Selector selector(String name) {
        return attribute(name);
    }

    @Override
    public PrintResult print(Scope scope, PrintResult result) {
        return view.print(scope, result);
    }

    @Override
    public FunctionType getSource() {
        return new FunctionType(view);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj instanceof View) {
            View view = (View) obj;
            if (view.equals(this.view)) return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return view.hashCode();
    }

    @Override
    public String toString() {
        return "{ENTITY: " + view.toString() + "}";
    }
}
