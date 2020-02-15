package org.kmsf.phenix.database;

import org.kmsf.phenix.algebra.Expression;

import java.util.Objects;

/**
 * A Selector is a {@link Expression} that we can project value from.
 *
 * A Selector is contained by a {@link View}.
 */
public abstract class Selector extends Expression {


    /**
     * return the View that contains this selector
     *
     * @return
     */
    public abstract View getView();



    @Override
    public int hashCode() {
        return Objects.hash(getView(), getSystemName());
    }
}
