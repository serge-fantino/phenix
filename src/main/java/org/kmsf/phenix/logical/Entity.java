package org.kmsf.phenix.logical;

import org.kmsf.phenix.database.*;
import org.kmsf.phenix.sql.PrintResult;
import org.kmsf.phenix.sql.Scope;
import org.kmsf.phenix.function.FunctionType;
import org.kmsf.phenix.function.Function;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class Entity extends View {

    private Optional<String> name;
    private View view;
    private Scope scope;

    public Entity(View view) {
        this.name = view.getName();
        this.view = view;
        this.scope = new Scope(view.getScope());
    }

    public Entity(String name, View view) {
        this.name = Optional.ofNullable(name);
        this.view = view;
    }

    public Optional<String> getName() {
        return name;
    }

    @Override
    public Scope getScope() {
        return scope;
    }

    @Override
    public List<? extends Selector> getSelectors() {
        return Collections.emptyList();
    }

    @Override
    public List<Function> getPK() {
        return view.getPK();
    }

    public Attribute attribute(String name, Function expr) throws ScopeException {
        FunctionType source = expr.getSource();
        if (!source.contains(view)) throw new ScopeException("invalid attribute");
        return new Attribute(this, name, expr);
    }

    public Attribute attribute(String name) throws ScopeException {
        Selector expr = view.selector(name);
        return new Attribute(this, name, expr);
    }

    public Attribute join(View target, String name, Function expr) {
        return new Attribute(this, name, new Join(target, expr));
    }

    @Override
    public Selector selector(String name) throws ScopeException {
        return attribute(name);
    }

    @Override
    public PrintResult print(Scope scope, PrintResult result) throws ScopeException {
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
        if (obj instanceof Join) {
            View target = ((Join) obj).getTarget();
            if (target.equals(this.view)) return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return view.hashCode();
    }

    @Override
    public String toString() {
        return "[ENTITY: " + view.toString() + "]";
    }
}
