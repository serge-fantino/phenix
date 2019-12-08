package org.kmsf.phenix.database;

import org.kmsf.phenix.function.Function;

import java.util.Optional;

public abstract class View extends Function {

    public static boolean checkIfViewAreCompatible(View first, View second) {
        return first.equals(second) || first.inheritsFrom(second) || second.inheritsFrom(first);
    }

    public final boolean isCompatibleWith(View other) {
        return checkIfViewAreCompatible(this, other);
    }

    /**
     * create a selector based on that name from this View scope
     *
     * @param name
     * @return
     */
    public abstract Selector selector(String name) throws ScopeException;


    /**
     * return true if this view can accept a selector. Default implementation is to accept anything
     * @param from : the view from which we try to accept the selector
     * @param selector
     * @return
     */
    protected Optional<Selector> accept(View from, Selector selector) {
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
