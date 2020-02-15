package org.kmsf.phenix.database;

import org.kmsf.phenix.algebra.PrecedenceOrder;
import org.kmsf.phenix.sql.Scope;
import org.kmsf.phenix.algebra.Expression;

import java.util.List;

/**
 * A View is a special kind of {@link Expression} which defines a {@link Scope} of values and can be manipulated with {@link Statement}.
 * <p>
 * A View contains a list of {@Link Selector selectors}, that can be iterated with {@link #getSelectors()} or retrieved by name with {@link #selector(String)}.
 */
public abstract class View extends Expression {

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
    public abstract List<Expression> getPK();

    @Override
    public int getPrecedence() {
        return PrecedenceOrder.PRECEDENCE_ORDER_VIEW;
    }

}
