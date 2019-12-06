package org.kmsf.phenix.database;

import org.kmsf.phenix.function.Function;

import java.util.Optional;

public abstract class View extends Function {

    /**
     * create a selector based on that name from this View scope
     *
     * @param name
     * @return
     */
    public abstract Selector selector(String name) throws ScopeException;


    /**
     * return true if this view can accept a selector. Default implementation is to accept anything
     * @param selector
     * @return
     */
    protected Optional<Selector> accept(Selector selector) {
        return Optional.ofNullable(selector);
    }

    /**
     * check if this view inherits from parent view
     * @param parent
     * @return
     */
    public abstract boolean inheritsFrom(View parent);

    /**
     * return the composite PrimaryKey if defined for this view
     *
     * @return
     */
    public abstract Key getPK();

    @Override
    public int getPrecedence() {
        return PRECEDENCE_ORDER_VIEW;
    }
}
