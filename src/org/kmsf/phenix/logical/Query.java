package org.kmsf.phenix.logical;

import org.kmsf.phenix.database.*;
import org.kmsf.phenix.database.sql.PrintResult;
import org.kmsf.phenix.database.sql.Scope;
import org.kmsf.phenix.function.Function;
import org.kmsf.phenix.function.FunctionType;
import org.kmsf.phenix.function.Functions;

public class Query extends Statement {

    private Select select = new Select();

    public Query() {}

    public Query(Entity root) {
        this.select(root);
    }

    public Query select(Attribute attr) {
        addToScopeIfNeeded(attr);
        select.select(attr, attr.getName().get());
        return this;
    }

    public Query select(Entity entity) {
        addToScopeIfNeeded(entity);
        select.select(Functions.STAR(entity));
        return this;
    }

    protected void addToScopeIfNeeded(Function fun) {
        FunctionType source = fun.getSource();
        source.getValues().forEach(
                function -> {
                    if (!select.getScope().contains(function)) {
                        // add the entity
                        select.from(function);
                    }
                }
        );
    }

    public Query from(Entity entity) {
        select.from(entity);
        return this;
    }

    public Query from(View view) {
        select.from(view);
        return this;
    }

    public Query groupBy(Attribute attr) {
        addToScopeIfNeeded(attr);
        select.select(attr);
        select.groupBy(attr);
        return this;
    }

    @Override
    public Selector selector(String name) {
        return select.selector(name);
    }

    @Override
    public PrintResult print(Scope scope, PrintResult result) {
        return select.print(scope, result);
    }

    @Override
    public FunctionType getSource() {
        return select.getSource();
    }

    @Override
    public String print() throws ScopeException {
        return select.print();
    }

    @Override
    public int getPrecedence() {
        return select.getPrecedence();
    }
}
