package org.kmsf.phenix.logical;

import org.kmsf.phenix.database.Column;
import org.kmsf.phenix.database.Join;
import org.kmsf.phenix.database.ScopeException;
import org.kmsf.phenix.database.Selector;
import org.kmsf.phenix.database.sql.PrintResult;
import org.kmsf.phenix.database.sql.Scope;
import org.kmsf.phenix.function.FunctionType;
import org.kmsf.phenix.function.Function;
import org.kmsf.phenix.function.Functions;

import java.util.Optional;

public class Attribute extends Selector {

    private Entity entity;
    private Function definition;
    private String name;

    public static Attribute APPLY(Attribute from, Attribute expr) throws ScopeException {
        // checking the source first
        FunctionType source = from.getSource();
        Optional<Function> tail = source.getTail();
        FunctionType target = expr.getSource();
        Optional<Function> head = target.getHead();
        if (tail.isEmpty() || head.isEmpty() || !tail.equals(head))
            throw new ScopeException("invalid APPLY arguments: " + head + " doesn't match " + tail);
        return new Attribute(expr.entity, expr.name, expr.definition) {
            @Override
            public FunctionType getSource() {
                return new FunctionType(from.getSource(), expr.getSource());
            }
        };
    }

    public Attribute(Entity entity, String name, Function definition) {
        this.entity = entity;
        this.definition = definition;
        this.name = name;
    }

    public Entity getEntity() {
        return entity;
    }

    public Optional<String> getName() {
        return Optional.ofNullable(name);
    }

    @Override
    public Optional<String> getDefaultAlias() {
        if (definition instanceof Join)
            return Optional.of(Functions._STAR);
        if (definition instanceof Selector)
            return ((Selector) definition).getDefaultAlias();
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

    @Override
    public PrintResult print(Scope scope, PrintResult result) {
        if (definition instanceof Join) {
            Join join = (Join) definition;
            return Functions.STAR(join.getTarget()).print(scope, result);
        } else {
            return definition.print(scope, result);
        }
    }

    @Override
    public FunctionType getSource() {
        if (definition.getSource().equals(entity)) {
            return new FunctionType(entity);
        } else {
            return new FunctionType(entity, definition);
        }
    }
}
