package org.kmsf.phenix.database;

import org.kmsf.phenix.function.Function;

public abstract class View extends Function {

    /**
     * create a selector based on that name from this View scope
     * @param name
     * @return
     */
    public abstract Selector selector(String name);

    @Override
    public int getPrecedence() {
        return PRECEDENCE_ORDER_VIEW;
    }

}
