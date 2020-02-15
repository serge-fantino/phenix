package org.kmsf.phenix.algebra;

import org.kmsf.phenix.database.ScopeException;
import org.kmsf.phenix.sql.PrintResult;
import org.kmsf.phenix.sql.Scope;

import java.util.Optional;

/**
 * A Expression is the root Object in our system, providing the essential properties to the algebraic model.
 * <p>
 * A Expression has a {@link FunctionType functional type} which is the {@link #getSource() source domain}.
 * <p>
 * A Function can have a {@link #getSystemName() system name} which is unmodifiable. This system name can me overrided by {@link #getName() a name} defined by user.
 * <p>
 * A Function has a {@link #getPrecedence() precedence} which is defined by {@link PrecedenceOrder} interface, and that can be used by the arythmetic system.
 */
public abstract class Expression {

    public abstract PrintResult print(Scope scope, PrintResult result) throws ScopeException;

    public abstract FunctionType getSource();

    /**
     * return the default name of the function, as defined by the system, that is without taking user modifiction into account
     *
     * @return
     */
    public Optional<String> getSystemName() {
        return Optional.empty();
    }

    public Optional<String> getName() {
        return Optional.empty();
    }

    public int getPrecedence() {
        return PrecedenceOrder.PRECEDENCE_ORDER_DEFAULT;
    }

    /**
     * return this function value reduction, i.e. for a Function f, return a Function fx that verifies f.equals(fx) && redux(fx)==fx
     *
     * @return
     */
    public Expression redux() {
        return this;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj instanceof Expression) {
            return redux().identity(((Expression) obj).redux());
        }
        return false;
    }

    /**
     * this is the identity method that check if this can be replaced by fun in a expression.
     * Subclass may override to solve "leaf" cases, i.e. when redux(X).class==X.class
     *
     * @param fun
     * @return
     */
    public boolean identity(Expression fun) {
        return this == fun;
    }
}
