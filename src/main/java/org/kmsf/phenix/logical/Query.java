package org.kmsf.phenix.logical;

import org.kmsf.phenix.database.*;
import org.kmsf.phenix.sql.PrintResult;
import org.kmsf.phenix.sql.Scope;
import org.kmsf.phenix.function.Function;
import org.kmsf.phenix.function.FunctionType;

import java.util.List;
import java.util.Optional;

public class Query extends Statement {

    private Select select = new Select();

    public Query() {
    }

    public Query(Entity entity) {
        select.addToScopeIfNeeded(entity);
    }

    public Query(Query copy) {
        this.select = (Select)copy.select.copy();
    }

    @Override
    public Function copy() {
        return new Query(this);
    }

    @Override
    public List<Selector> getSelectors() {
        return select.getSelectors();
    }

    @Override
    public Key getPK() {
        return select.getPK();
    }

    public Query select(Function expr) throws ScopeException {
        return select(expr, expr.getName());
    }

    public Query select(Function expr, String alias) throws ScopeException {
        return select(expr, Optional.of(alias));
    }

    public Query select(Function expr, Optional<String> alias) throws ScopeException {
        select.addToScopeIfNeeded(expr);
        if (alias.isPresent())
            select.select(expr, alias.get());
        else
            select.select(expr);
        return this;
    }

    public Query select(Entity entity) throws ScopeException {
        select.addToScopeIfNeeded(entity);
        //select.select(Functions.STAR(entity));
        return this;
    }

    public Query from(Entity entity) throws ScopeException {
        select.from(entity);
        return this;
    }

    public Query from(View view) throws ScopeException {
        select.from(view);
        return this;
    }

    public Query from(Attribute attr) throws ScopeException {
        select.from(attr.getDefinition());
        select.select(attr);
        return this;
    }

    public Query where(Function predicat) {
        select.addToScopeIfNeeded(predicat);
        select.where(predicat);
        return this;
    }

    public Query groupBy(Function expr) throws ScopeException {
        select(expr);
        select.groupBy(expr);
        return this;
    }

    @Override
    public Optional<Selector> selector(String name) {
        return select.selector(name);
    }

    @Override
    public Optional<Selector> accept(View from, Selector selector) {
        return select.accept(from, selector);
    }

    @Override
    public boolean inheritsFrom(View parent) {
        return this.equals(parent) || select.isCompatibleWith(parent);
    }

    @Override
    public PrintResult print(Scope scope, PrintResult result) throws ScopeException {
        return select.print(scope, result);
    }

    @Override
    public FunctionType getType() {
        return select.getType();
    }

    @Override
    public String print() throws ScopeException {
        return select.print();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Query query = (Query) o;
        return select.equals(query.select);
    }

    @Override
    public int hashCode() {
        return select.hashCode();
    }

    @Override
    public String toString() {
        return "[Query " + hashCode() + "]";
    }
}
