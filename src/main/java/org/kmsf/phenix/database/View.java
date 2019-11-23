package org.kmsf.phenix.database;

import org.kmsf.phenix.database.sql.Scope;
import org.kmsf.phenix.function.Function;

import java.util.List;
import java.util.Optional;

public abstract class View extends Function {

    /**
     * return the view's scope
     *
     * @return
     */
    public abstract Scope getScope();

    /**
     * create a selector based on that name from this View scope
     *
     * @param name
     * @return
     */
    public abstract Selector selector(String name);

    /**
     * return the composite PrimaryKey if defined for this view
     *
     * @return
     */
    public abstract List<Function> getPK();

    @Override
    public int getPrecedence() {
        return PRECEDENCE_ORDER_VIEW;
    }

}
