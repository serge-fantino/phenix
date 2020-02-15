package org.kmsf.phenix.database;

import org.kmsf.phenix.function.Functions;
import org.kmsf.phenix.function.Operator;
import org.kmsf.phenix.sql.Mapping;
import org.kmsf.phenix.sql.PrintResult;
import org.kmsf.phenix.sql.Scope;
import org.kmsf.phenix.function.FunctionType;
import org.kmsf.phenix.function.Function;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class Join extends View {

    private View source;
    private View target;
    private Function definition;

    private Optional<String> name = Optional.empty();

    public Join(View target, Function definition) throws ScopeException {
        this(computeSource(target, definition), target, definition);
    }

    @Override
    public DatabaseProperties getDatabaseProperties() {
        return target.getDatabaseProperties();
    }

    private static View computeSource(View target, Function definition) throws ScopeException {
        List<View> candidates = definition.getType().getValues().stream().filter(view -> !view.isCompatibleWith(target)).collect(Collectors.toList());
        if (candidates.size()!=1)
            throw new ScopeException("cannot identify the join source; target="+target+" definition="+definition.getType().getValues());
        return candidates.get(0);
    }

    public Join(View source, View target, Function definition) throws ScopeException {
        this.source = source;
        this.target = target;
        this.definition = definition.relinkTo(source).relinkTo(this);
        if (!assertSourceAndTargetAreCompatibleWithDefinition(source, target, definition))
            throw new ScopeException("invalid source/target for this definition "+definition+" but expecting "+new FunctionType(source.getType(), target.getType())+" but was "+ definition.getType());
    }

    public Join(String name, View source, View target, Function definition) throws ScopeException {
        this(source, target, definition);
        this.name = Optional.ofNullable(name);
    }

    public Join(View source, List<Function> sourceKeys, View target, List<Function>  targetKeys) throws ScopeException {
        this.source = source;
        this.target = target;
        this.definition = Functions.EQUALS(//sourceKeys, targetKeys);
                  sourceKeys.stream().map(fun -> fun.relinkTo(source)).collect(Collectors.toList())
                , targetKeys.stream().map(fun -> fun.relinkTo(this)).collect(Collectors.toList()));
        if (!assertSourceAndTargetAreCompatibleWithDefinition(source, target, definition))
            throw new ScopeException("invalid source/target for this definition "+definition+" but expecting "+new FunctionType(source.getType(), target.getType())+" but was "+ definition.getType());
    }

    protected Join(Join copy) {
        this.source = copy.source;
        this.target = copy.target;
        this.definition = copy.definition;
        this.name = copy.name;
    }

    @Override
    public Optional<String> getName() {
        return name;
    }

    @Override
    public Function copy() {
        return new Join(this);
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
        if (definitionSource.size()==2)
            return definitionSource.isCompatibleWith(source)
                && definitionSource.isCompatibleWith(target);
        else if (definitionSource.size()==1 && source.isCompatibleWith(target)) return definitionSource.isCompatibleWith(source);
        else return false;
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
        return new FunctionType(source, this);
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
    public Optional<Selector> selector(String name) {
        return target.selector(name);
    }

    @Override
    public List<Selector> getSelectors() {
        return target.getSelectors().stream().map(fun -> (Selector)fun.relinkTo(this)).collect(Collectors.toList());
    }

    @Override
    public Optional<Selector> accept(View from, Selector selector) {
        return target.accept(from, selector).map(fun -> (Selector)fun.relinkTo(this));
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
        return source + " INNER JOIN " + target + " ON " + definition;
    }

}
