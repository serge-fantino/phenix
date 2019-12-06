package org.kmsf.phenix.database;

import org.kmsf.phenix.function.Function;
import org.kmsf.phenix.function.FunctionType;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class Key {

    private View view;
    private List<? extends Function> keys = null;

    public Key(View view) {
        this(view, Collections.emptyList());
    }

    public Key(View view, List<? extends Function> keys) {
        this.view = view;
        this.keys = keys;
    }

    public View getView() {
        return this.view;
    }

    public List<Function> getKeys() {
        return Collections.unmodifiableList(keys);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Key key = (Key) o;
        return view.equals(key.view) &&
                keys.equals(key.keys);
    }

    @Override
    public int hashCode() {
        return Objects.hash(view, keys);
    }

    @Override
    public String toString() {
        return "[Key for " + view + " is " + keys + "]";
    }
}
