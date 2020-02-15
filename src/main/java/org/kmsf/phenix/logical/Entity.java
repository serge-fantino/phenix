package org.kmsf.phenix.logical;

import org.kmsf.phenix.database.*;
import org.kmsf.phenix.function.Functions;
import org.kmsf.phenix.sql.PrintResult;
import org.kmsf.phenix.sql.Scope;
import org.kmsf.phenix.function.FunctionType;
import org.kmsf.phenix.function.Function;

import java.util.*;

public class Entity extends View {

    private Optional<String> name;
    private View view;
    private List<Attribute> attributes = new ArrayList<>();

    public Entity(View view) {
        this.name = view.getName();
        this.view = (View)view.copy();
    }

    public Entity(String name, View view) {
        this.name = Optional.ofNullable(name);
        this.view = (View)view.copy();
    }

    protected Entity(Entity copy) {
        this.name = copy.name;
        this.view = (View)copy.view.copy();
        this.attributes = new ArrayList<>(copy.attributes);
    }

    @Override
    public Function copy() {
        return new Entity(this);
    }

    public Optional<String> getName() {
        return name;
    }

    @Override
    public DatabaseProperties getDatabaseProperties() {
        return view.getDatabaseProperties();
    }

    @Override
    public List<Selector> getSelectors() {
        ArrayList<Selector> selectors = new ArrayList<>();
        selectors.addAll(attributes);
        for (Selector selector : view.getSelectors()) {
            if (!(selector instanceof Column)) {
                selectors.add(selector);
            }
        }
        return selectors;
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
        Optional<Selector> expr = view.selector(reference);
        if (expr.isEmpty()) throw new ScopeException("attribute '" + reference + "' is not defined in " + this);
        return register(new Attribute(this, name, expr.get()));
    }

    /**
     * Join from this View using the Key(withKeys) to the Target view PK
     * @param target
     * @param withKeys
     * @return
     */
    public Attribute join(String name, View target, String ... withKeys) throws ScopeException {
        if (target.getPK().getKeys().isEmpty()) throw new ScopeException(target.toString()+" has no PK defined, cannot create natural join");
        if (target.getPK().getKeys().size()!=withKeys.length) throw new ScopeException("cannot create natural join, keys size are different: "+target.getPK()+" versus "+withKeys);
        ArrayList<Function> keys = new ArrayList<>();
        for (String keyName : withKeys) {
            Optional<Selector> key = selector(keyName);
            if (key.isEmpty()) throw new ScopeException("key "+keyName+" is not defined in "+this+" scope");
            keys.add(key.get());
        }
        return register(new Attribute(this, name, new Join(this, keys, target, target.getPK().getKeys())));
    }

    /**
     * Join to this View PK from the source view suing the withKeys
     * @param target
     * @param withKeys
     * @return
     */
    public Attribute oppositeJoin(String name, View target, String ... withKeys) throws ScopeException {
        if (this.getPK().getKeys().isEmpty()) throw new ScopeException(this.toString()+" has no PK defined, cannot create natural join");
        if (this.getPK().getKeys().size()!=withKeys.length) throw new ScopeException("cannot create natural join, keys size are different: "+this.getPK()+" versus "+withKeys);
        ArrayList<Function> keys = new ArrayList<>();
        for (String keyName : withKeys) {
            Optional<Selector> key = target.selector(keyName);
            if (key.isEmpty()) throw new ScopeException("key "+keyName+" is not defined in "+target+" scope");
            keys.add(key.get());
        }
        return register(new Attribute(this, name, new Join(this, this.getPK().getKeys(), target, keys)));
    }

    public Attribute join(View target, String ... withKeys) throws ScopeException {
        return join(target.getName().orElse(withKeys[0]), target, withKeys);
    }

    public Attribute join(String name, View target, Function expr) throws ScopeException {
        if (expr instanceof Join) {
            return register(new Attribute(this, name, new Join(this, target, ((Join) expr).getDefinition())));
        } else {
            return register(new Attribute(this, name, new Join(this, target, expr)));
        }
    }

    public Attribute join(String name, View target, Key fk) throws ScopeException {
        if (getPK().getKeys().isEmpty()) throw new ScopeException(this.toString()+" has no PK defined, cannot create natural join");
        if (getPK().getKeys().size()!=fk.getKeys().size()) throw new ScopeException("cannot create natural join, keys size are different: "+this.getPK()+" versus "+fk);
        try {
            return join(name, target, new Join(this, fk.getView(), Functions.EQUALS(getPK().getKeys(), fk.getKeys())));
        } catch (ScopeException e) {
            throw new ScopeException("failed to create natural join from "+this+" to "+fk+": "+e.getMessage(), e);
        }
    }

    @Override
    public Optional<Selector> selector(String name) {
        Optional<Attribute> attribute = getAttribute(name);
        if (attribute.isPresent()) return Optional.of(attribute.get());
        return view.selector(name);
    }

    @Override
    public Optional<Selector> accept(View from, Selector selector) {
        return view.accept(from, selector);
    }

    @Override
    public boolean inheritsFrom(View parent) {
        return parent.equals(this) || view.isCompatibleWith(parent);
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
    public int getPrecedence() {
        return view.getPrecedence();
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
