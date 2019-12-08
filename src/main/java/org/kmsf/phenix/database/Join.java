package org.kmsf.phenix.database;

import org.kmsf.phenix.sql.PrintResult;
import org.kmsf.phenix.sql.Scope;
import org.kmsf.phenix.function.FunctionType;
import org.kmsf.phenix.function.Function;

import java.util.List;
import java.util.stream.Collectors;

public class Join extends View {

    private View source;
    private View target;
    private Function definition;

    public Join(View target, Function definition) throws ScopeException {
        this(computeSource(target, definition), target, definition);
    }

    private static View computeSource(View target, Function definition) throws ScopeException {
        List<View> candidates = definition.getType().getValues().stream().filter(view -> !view.isCompatibleWith(target)).collect(Collectors.toList());
        if (candidates.size()!=1)
            throw new ScopeException("cannot identify the join source; target="+target+" definition="+definition.getType().getValues());
        return candidates.get(0);
    }

    public Join(View source, View target, Function definition) throws ScopeException {
        if (!assertSourceAndTargetAreCompatibleWithDefinition(source, target, definition))
            throw new ScopeException("invalid source/target for this definition "+definition+" but expecting "+new FunctionType(source.getType(), target.getType()));
        this.source = source;
        this.target = target;
        this.definition = definition;
    }

    /**
     * return the opposite Join
     * @return
     */
    public Join opposite() throws ScopeException {
        return new Join(target, source, definition);
    }

    private boolean assertSourceAndTargetAreCompatibleWithDefinition(View source, View target, Function definition) throws ScopeException {
        FunctionType definitionSource = definition.getType();
        return definitionSource.size()>1 && definitionSource.size()<=2
                && definitionSource.isCompatibleWith(source)
                && definitionSource.isCompatibleWith(target);
    }

    public Function getDefinition() {
        return definition;
    }

    @Override
    public PrintResult print(Scope scope, PrintResult result) throws ScopeException {
        definition.print(scope, result);
        return result;
    }

    @Override
    public FunctionType getType() {
        return new FunctionType(source.getType(), new FunctionType(this));
    }

    public View getSource() {
        return source;
    }

    public Join changeSource(View source) throws ScopeException {
        return new Join(source, target, definition);
    }

    public View getTarget() {
        return target;
    }

    @Override
    public Selector selector(String name) throws ScopeException {
        return target.selector(name);
    }

    @Override
    public List<Selector> getSelectors() {
        return target.getSelectors();
    }

    @Override
    public boolean inheritsFrom(View parent) {
        if (parent instanceof Join) {
            Join join = (Join)parent;
            if (!join.definition.equals(this.definition)) return false;
        }
        return target.isCompatibleWith(parent);
    }

    @Override
    public Key getPK() {
        return target.getPK();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Join join = (Join) o;
        return source.equals(join.source) &&
                target.equals(join.target) &&
                definition.equals(join.definition);
    }

    @Override
    public int hashCode() {
        return target.hashCode();
    }

    @Override
    public String toString() {
        return "INNER JOIN " + target + " ON " + definition;
    }

}
