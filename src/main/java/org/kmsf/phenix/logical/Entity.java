package org.kmsf.phenix.logical;

import org.kmsf.phenix.database.*;
import org.kmsf.phenix.sql.PrintResult;
import org.kmsf.phenix.sql.Scope;
import org.kmsf.phenix.function.FunctionType;
import org.kmsf.phenix.function.Function;

import java.util.*;

public class Entity extends View {

    private Optional<String> name;
    private View view;
    private Scope scope;

    private List<Attribute> attributes = new ArrayList<>();

    public Entity(View view) {
        this.name = view.getName();
        this.view = view;
    }

    public Entity(String name, View view) {
        this.name = Optional.ofNullable(name);
        this.view = view;
    }

    public Optional<String> getName() {
        return name;
    }

    @Override
    public List<Selector> getSelectors() {
        return Collections.unmodifiableList(attributes);
    }

    @Override
    public Key getPK() {
        return view.getPK();
    }

    public Attribute attribute(String name, Function expr) throws ScopeException {
        FunctionType source = expr.getType();
        if (!source.contains(view)) throw new ScopeException("invalid attribute");
        return register(new Attribute(this, name, expr));
    }

    public Attribute attribute(String reference) throws ScopeException {
        return attribute(reference, reference);
    }

    public Attribute attribute(String name, String reference) throws ScopeException {
        Optional<Attribute> attr = getAttribute(reference);
        if (attr.isPresent()) return attr.get();
        Selector expr = view.selector(reference);
        return register(new Attribute(this, name, expr));
    }

    public Attribute join(View target, String name, Function expr) throws ScopeException {
        if (expr instanceof Join) {
            return register(new Attribute(this, name, new Join(this, target, ((Join) expr).getDefinition())));
        } else {
            return register(new Attribute(this, name, new Join(this, target, expr)));
        }
    }

    @Override
    public Selector selector(String name) throws ScopeException {
        return getAttribute(name).orElseThrow(() -> new ScopeException("attribute '" + name + "' is not defined in " + this + "' scope="+attributes));
    }

    @Override
    public boolean inheritsFrom(View parent) {
        return view.inheritsFrom(parent);
    }

    protected Optional<Attribute> getAttribute(String name) {
        for (Attribute attr : attributes) {
            if (name.equals(attr.getName().orElse(null))) return Optional.of(attr);
        }
        return Optional.empty();
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
    public FunctionType getType() {
        return new FunctionType(view);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Entity entity = (Entity) o;
        return name.equals(entity.name) &&
                view.equals(entity.view) &&
                attributes.equals(entity.attributes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, view, attributes);
    }

    @Override
    public String toString() {
        return "[ENTITY: '"+getName().orElse("$")+"'=" + view.toString() + "]";
    }
}
