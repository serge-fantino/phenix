package org.kmsf.phenix.function;

import org.kmsf.phenix.database.View;

import java.util.*;

public class FunctionType {

    private ArrayList<View> values = new ArrayList<>();

    public FunctionType() {
        //
    }

    public FunctionType(View function) {
        values.add(function);
    }

    public FunctionType(View... functions) {
        for (View f : functions)
            values.add(f);
    }

    public FunctionType(FunctionType... flists) {
        for (FunctionType f : flists)
            addAll(f.values);
    }

    public FunctionType(List<? extends View> functions) {
        functions.forEach(fun -> add(fun));
    }

    public int size() { return values.size(); }

    public boolean contains(View fun) {
        return values.contains(fun);
    }

    public boolean contains(FunctionType source) {
        return values.containsAll(source.getValues());
    }

    /**
     * return true if at least one type is compatible with the view
     * @param view
     * @return
     */
    public boolean isCompatibleWith(View view) {
        return values.stream().anyMatch(value -> value.isCompatibleWith(view));
    }

    public FunctionType add(View fun) {
        if (!values.contains(fun)) values.add(fun);
        return this;
    }

    public FunctionType add(FunctionType type) {
        addAll(type.values);
        return this;
    }

    public void addAll(List<View> values) {
        values.forEach(function -> this.add(function));
    }

    public List<View> getValues() {
        return Collections.unmodifiableList(values);
    }

    public Optional<View> getHead() {
        if (values.isEmpty()) return Optional.empty();
        return Optional.ofNullable(values.get(0));
    }

    public Optional<View> getTail() {
        if (values.isEmpty()) return Optional.empty();
        return Optional.ofNullable(values.get(values.size() - 1));
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (!(obj instanceof FunctionType)) return false;
        FunctionType flist = (FunctionType) obj;
        return flist.values.equals(values);
    }

    @Override
    public int hashCode() {
        return values.hashCode();
    }

    @Override
    public String toString() {
        return values.toString();
    }

}
