package org.kmsf.phenix.logical;

import org.kmsf.phenix.algebra.Operators;
import org.kmsf.phenix.database.*;
import org.kmsf.phenix.sql.PrintResult;
import org.kmsf.phenix.sql.Scope;
import org.kmsf.phenix.algebra.FunctionType;
import org.kmsf.phenix.algebra.Expression;
import org.kmsf.phenix.algebra.Functions;

import java.util.Optional;

public class Attribute extends Selector {

    private Entity entity;
    private Expression definition;
    private String name;

    public static Attribute APPLY(Attribute from, Attribute expr) throws ScopeException {
        return from.apply(expr);
    }

    public Attribute(Entity entity, String name, Expression definition) {
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

    @Override
    public Optional<String> getSystemName() {
        if (definition instanceof Join)
            return Optional.of(Operators._STAR);
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
        FunctionType source = this.getSource();
        Optional<Expression> tail = source.getTail();
        FunctionType target = expr.getSource();
        Optional<Expression> head = target.getHead();
        if (tail.isEmpty() || head.isEmpty() || !tail.equals(head))
            throw new ScopeException("invalid APPLY arguments " + head + " doesn't match " + tail);
        return new Attribute(expr.entity, expr.name, expr.definition) {
            @Override
            public FunctionType getSource() {
                return new FunctionType(source, expr.getSource());
            }
        };
    }

    @Override
    public PrintResult print(Scope scope, PrintResult result) throws ScopeException {
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

    /**
     * the Attribute redux is the definition redux; thus Attribute is not a leaf
     *
     * @return
     */
    @Override
    public Expression redux() {
        return definition.redux();
    }

    @Override
    public String toString() {
        return "[Attribute '" + name + "'=" + definition + "]";
    }
}
