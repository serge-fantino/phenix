package org.kmsf.phenix.algebra;

import java.util.*;

public class FunctionType {

    private ArrayList<Expression> values = new ArrayList<>();

    public FunctionType() {
        //
    }

    public FunctionType(Expression expression) {
        values.add(expression);
    }

    public FunctionType(Expression... expressions) {
        for (Expression f : expressions)
            values.add(f);
    }

    public FunctionType(FunctionType... flists) {
        for (FunctionType f : flists)
            addAll(f.values);
    }

    public FunctionType(List<? extends Expression> functions) {
        functions.forEach(fun -> add(fun));
    }

    public boolean contains(Expression fun) {
        return values.contains(fun);
    }

    public FunctionType add(Expression fun) {
        if (!values.contains(fun)) values.add(fun);
        return this;
    }

    public FunctionType add(FunctionType type) {
        addAll(type.values);
        return this;
    }

    public void addAll(List<Expression> values) {
        values.forEach(function -> this.add(function));
    }

    public List<Expression> getValues() {
        return Collections.unmodifiableList(values);
    }

    public Optional<Expression> getHead() {
        if (values.isEmpty()) return Optional.empty();
        return Optional.ofNullable(values.get(0));
    }

    public Optional<Expression> getTail() {
        if (values.isEmpty()) return Optional.empty();
        return Optional.ofNullable(values.get(values.size() - 1));
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj instanceof FunctionType) {
            FunctionType flist = (FunctionType) obj;
            return flist.values.equals(values);
        }
        if (values.size() == 1) {
            return values.contains(obj);
        }
        return false;
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
