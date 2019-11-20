package org.kmsf.phenix.function;

import org.kmsf.phenix.database.View;

import java.util.*;

public class FunctionType {

    private ArrayList<Function> values = new ArrayList<>();

    public FunctionType(Function... functions) {
        for (Function f : functions)
            values.add(f);
    }

    public FunctionType(FunctionType... flists) {
        for (FunctionType f : flists)
            addAll(f.values);
    }

    public FunctionType(List<Function> functions) {
        values.addAll(functions);
    }

    public boolean contains(Function fun) {
        return values.contains(fun);
    }

    public FunctionType add(Function fun) {
        if (!values.contains(fun)) values.add(fun);
        return this;
    }

    public FunctionType add(FunctionType type) {
        addAll(type.values);
        return this;
    }

    public void addAll(List<Function> values) {
        values.forEach(function -> this.add(function));
    }

    public List<Function> getValues() {
        return Collections.unmodifiableList(values);
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
