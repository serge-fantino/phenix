package org.kmsf.phenix.database;

import org.kmsf.phenix.function.Function;

import java.util.Objects;

/**
 * A Selector is a Function that we can project value from a View that contains this selector.
 */
public abstract class Selector extends Function {


    /**
     * return the View that contains this selector
     *
     * @return
     */
    public abstract View getView();


    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj instanceof Selector) {
            Selector sel = (Selector) obj;
            return sel.getView().equals(getView()) && sel.getSystemName().equals(getSystemName());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getView(), getSystemName());
    }
}
