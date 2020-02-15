package org.kmsf.phenix.logical;

import org.kmsf.phenix.database.*;
import org.kmsf.phenix.sql.PrintResult;
import org.kmsf.phenix.sql.Scope;
import org.kmsf.phenix.algebra.FunctionType;
import org.kmsf.phenix.algebra.Expression;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Entity extends View {

    private Optional<String> name;
    private View view;
    private Scope scope;

    private List<Attribute> attributes = new ArrayList<>();

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
        return attributes;
    }

    @Override
    public List<Expression> getPK() {
        return view.getPK();
    }

    public Attribute attribute(String name, Expression expr) throws ScopeException {
        FunctionType source = expr.getSource();
        if (!source.contains(view)) throw new ScopeException("invalid attribute");
        return register(new Attribute(this, name, expr));
    }

    public Attribute attribute(String name) throws ScopeException {
        Selector expr = view.selector(name);
        return register(new Attribute(this, name, expr));
    }

    public Attribute join(View target, String name, Expression expr) throws ScopeException {
        return register(new Attribute(this, name, new Join(target, expr)));
    }

    @Override
    public Selector selector(String name) throws ScopeException {
        for (Attribute attr : attributes) {
            if (name.equals(attr.getName().orElse(null))) return attr;
        }
        throw new ScopeException("attribute '" + name + "' is not defined in entity '" + getName() + "'scope");

    }

    protected Attribute register(Attribute attribute) throws ScopeException {
        if (!attribute.getView().equals(this))
            throw new ScopeException("cannot register a attribute from a different entity");
        if (attributes.contains(attribute)) return attributes.get(attributes.indexOf(attribute));
        attributes.add(attribute);
        return attribute;
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
    public Expression redux() {
        return view.redux();
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
