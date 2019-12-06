package org.kmsf.phenix.database;

import org.kmsf.phenix.function.Function;

import java.util.Objects;
import java.util.Optional;

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

    /**
     * allow the selector to override its definition before printing its value
     * @return
     */
    public abstract Optional<Function> asSelectorValue();

}
