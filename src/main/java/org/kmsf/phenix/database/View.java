package org.kmsf.phenix.database;

import org.kmsf.phenix.sql.Scope;
import org.kmsf.phenix.function.Function;

import java.util.List;

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
    public abstract Selector selector(String name) throws ScopeException;

    /**
     * return the selectors of the view
     *
     * @return
     */
    public abstract List<? extends Selector> getSelectors();

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
