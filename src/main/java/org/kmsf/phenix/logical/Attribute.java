package org.kmsf.phenix.logical;

import org.kmsf.phenix.database.*;
import org.kmsf.phenix.sql.PrintResult;
import org.kmsf.phenix.sql.Scope;
import org.kmsf.phenix.function.FunctionType;
import org.kmsf.phenix.function.Function;
import org.kmsf.phenix.function.Functions;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class Attribute extends Selector {

    private Entity entity;
    private Function definition;
    private String name;

    public static Attribute APPLY(Attribute from, Attribute expr) throws ScopeException {
        return from.apply(expr);
    }

    public Attribute(Entity entity, String name, Function definition) {
        this.entity = entity;
        this.definition = definition;
        this.name = name;
    }

    public View getView() {
        return entity;
    }

    public Optional<String> getName() {
        return Optional.ofNullable(name);
    }

    public Function getDefinition() {
        return definition;
    }

    @Override
    public Optional<String> getSystemName() {
        if (definition instanceof Join)
            return Optional.of(Functions._STAR);
        if (definition instanceof Selector)
            return ((Selector) definition).getSystemName();
        // else
        return Optional.empty();
    }

    /**
     * allow to rename an attribute, for instance when created from a column name
     *
     * @param name
     * @return
     */
    public Attribute alias(String name) {
        this.name = name;
        return this;
    }

    public Attribute apply(Attribute expr) throws ScopeException {
        // checking the source first
        FunctionType source = this.getType();
        Optional<View> tail = source.getTail();
        FunctionType target = expr.getType();
        Optional<View> head = target.getHead();
        if (tail.isEmpty() || head.isEmpty() || !tail.get().isCompatibleWith(head.get()))
            throw new ScopeException("invalid APPLY arguments " + head + " doesn't match " + tail);
        return new Attribute(expr.entity, expr.name, expr.definition) {
            @Override
            public FunctionType getType() {
                return new FunctionType(source, expr.getType());
            }
        };
    }

    @Override
    public Optional<Function> asSelectorValue() {
        if (definition instanceof Join) {
            return Optional.empty();
        } else {
            return Optional.of(this);
        }
    }

    @Override
    public List<Selector> getSelectors() {
        return definition.getSelectors();
    }

    @Override
    public Function unwrapReference() {
        return definition;
    }

    @Override
    public PrintResult print(Scope scope, PrintResult result) throws ScopeException {
        return definition.print(scope, result);
    }

    @Override
    public FunctionType getType() {
        return definition.getType();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Attribute attribute = (Attribute) o;
        return entity.equals(attribute.entity) &&
                definition.equals(attribute.definition) &&
                name.equals(attribute.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(definition, name);
    }

    @Override
    public String toString() {
        return "[Attribute '" + name + "'=" + definition + "]";
    }
}
