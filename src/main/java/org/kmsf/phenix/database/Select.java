package org.kmsf.phenix.database;

import org.kmsf.phenix.function.*;
import org.kmsf.phenix.sql.*;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * The Select statement purpose is to generate a syntactically correct SQL SELECT statement
 */
public class Select extends Statement {

    private static Logger logger = Logger.getLogger(Select.class.getName());

    public static final String SELECT = "SELECT";
    public static final String FROM = "FROM";
    public static final String INNERJOIN = "INNER JOIN";
    public static final String ON = "ON";
    public static final String WHERE = "WHERE";
    public static final String GROUPBY = "GROUP BY";
    public static final String HAVING = "HAVING";
    public static final String AS = "AS";

    private List<FromClause> from = new ArrayList<>();
    private List<SelectClause> selection = new ArrayList<>();
    private List<ScopedFunction> where = new ArrayList<>();
    private List<ScopedFunction> having = new ArrayList<>();
    private List<SimpleClause> groupBy = new ArrayList<>();

    private AliasMap aliases = new AliasMap();

    private Scope scope = null;

    public Select() {
        this.scope = new Scope(this);
    }

    public Select(View view) throws ScopeException {
        this();
        from(view);
    }

    /**
     * This is the copy constructor
     * <p>Instead of using select as a from clause, it creates a copy of select and allows to extend it without side-effects on select
     * <p>If you require generating a sub-select, wrap the select inside a view
     *
     * @param select
     */
    public Select(Select select) {
        this.selection = new ArrayList<>(select.selection);
        this.from = new ArrayList<>(select.from);
        this.where = new ArrayList<>(select.where);
        this.having = new ArrayList<>(select.having);
        this.groupBy = new ArrayList<>(select.groupBy);
        this.aliases = new AliasMap(select.aliases);
        this.scope = select.scope;
    }

    public Select select(Function expr) throws ScopeException {
        return select(expr, expr.getName());
    }

    public Select select(Function expr, String alias) throws ScopeException {
        return select(expr, Optional.of(alias));
    }

    public Select select(Function expr, Optional<String> alias) throws ScopeException {
        Optional<Function> checkIfAcceptable = accept(expr);
        if (checkIfAcceptable.isEmpty()) throw new ScopeException("cannot select "+expr+" in "+scope);
        addSelector(checkIfAcceptable.get(), alias);
        //Optional<SelectClause> selector = createSelectClause(scope, checkIfAcceptable.get(), aliases.getAlias(alias));
        //if (selector.isPresent()) selection.add(selector.get());
        return this;
    }

    protected Optional<SelectClause> addSelector(Function expr, Optional<String> alias) {
        Optional<SelectClause> clause = createSelectClause(scope, expr, aliases.getAlias(alias));
        if (clause.isPresent()) selection.add(clause.get());
        return clause;
    }

    private Optional<SelectClause> createSelectClause(Scope scope, Function expr, Optional<String> alias) {
        if (expr instanceof Selector) {
            Selector selector = (Selector) expr;
            Optional<Function> value = selector.asSelectorValue();
            if (value.isPresent()) return Optional.of(new SelectClause(this, scope, expr, alias));
            return Optional.empty();
        } else {
            return Optional.of(new SelectClause(this, scope, expr, alias));
        }
    }


    protected Optional<Function> accept(Function expr) {
        return accept(this, expr);
    }

    protected Optional<Function> accept(View from, Function expr) {
        if (expr instanceof Selector) {
            Selector selector = (Selector) expr;
            return Optional.ofNullable(accept(from, selector).orElse(null));
        } else if (expr instanceof Operator) {
            Operator op = (Operator) expr;
            List<Optional<Function>> override = op.getArguments().stream().map(fun -> this.accept(from, fun)).collect(Collectors.toList());
            if (override.contains(Optional.empty())) return Optional.empty();
            return Optional.of(op.copy(override.stream().map(Optional::get).collect(Collectors.toList())));
        } else if (expr instanceof View) {
            View view = (View)expr;
            try {
                Mapping mapping = scope.resolves(view);
                return Optional.ofNullable(mapping.getReference());
            } catch (ScopeException e) {
                return Optional.empty();
            }
        } else {
            // TODO implement support for Operator function in select#accept()
            throw new RuntimeException("NYI: accept("+expr+")");
        }
    }

    @Override
    protected Optional<Selector> accept(View from, Selector selector) {
        if (getSelectors().contains(selector)) return getSelector(selector);
        Optional<Selector> checkIfAcceptable = isAcceptable(from, selector);
        if (checkIfAcceptable.isPresent()) {
            if (!from.equals(this)) {
                // a super view is asking for a reference to an selector which is not yet selected => select it as a side effect
                Selector newSelector = checkIfAcceptable.get();
                Optional<SelectClause> override = addSelector(newSelector, newSelector.getName());
                return Optional.of(override.isPresent() ? override.get().asSelector() : newSelector);
            } else {
                return checkIfAcceptable;
            }
        }
        return Optional.empty();
    }

    protected Optional<Selector> isAcceptable(View from, Selector selector) {
        for (Mapping mapping : scope) {
            Optional<Selector> check = mapping.getReference().accept(from, selector);
            if (check.isPresent()) return check;
        }
        return Optional.empty();
    }

    public Function copy() {
        return new Select(this);
    }

    protected Scope getScope() {
        return scope;
    }

    @Override
    public FunctionType getType() {
        return new FunctionType(from.stream().map(f -> f.getValue()).collect(Collectors.toList()));
    }

    @Override
    public Key getPK() {
        if (!groupBy.isEmpty()) {
            // return the groupBy definition as the new PK
            return new Key(this, groupBy.stream()
                    .map(clause -> clause.getValue())
                    .collect(Collectors.toList()));
        }
        // only using the from - return PK even if the column is not selected
        return new Key(this, from.stream()
                .flatMap(clause -> clause.getValue().getPK().getKeys().stream())
                .collect(Collectors.toList()));
    }

    @Override
    public List<Selector> getSelectors() {
        return selection.stream().map(clause -> clause.asSelector()).collect(Collectors.toList());
    }

    protected Optional<Selector> getSelector(Selector selector) {
        for (SelectClause clause : selection) {
            if (clause.getDefinition().equals(selector)) return Optional.of(clause.asSelector());
        }
        return Optional.empty();
    }

    @Override
    public boolean inheritsFrom(View parent) {
        for (var clause : from) {
            if (clause.getValue().isCompatibleWith(parent)) return true;
        }
        return false;
    }

    @Override
    public Selector selector(String definition) throws ScopeException {
        assert definition != null;
        // the Select.selector() return a reference to a selector already defined in the scope, using its alias name
        for (SelectClause clause : selection) {
            if (definition.equals(clause.getAlias().orElse(null))) return clause.asSelector();
            if (definition.equals(clause.getDefinition().getSystemName().orElse(null))) return clause.asSelector();
        }
        throw new ScopeException("cannot find selector '" + definition + "'");
    }

    public List<Function> addToScopeIfNeeded(Function fun) {
        FunctionType type = fun.getType();
        List<Function> added = new ArrayList<>();
        type.getValues().forEach(
                view -> added.addAll(addToScopeIfNeeded(view))
        );
        return added;
    }

    protected List<View> addToScopeIfNeeded(View view) {
        if (!scope.canResolves(view)) {
            // add the entity
            try {
                from(view);
                return Collections.singletonList(view);
            } catch (ScopeException e) {
                logger.throwing(this.getClass().getName(), "addToScopeIfNeeded", e);
            }
        }
        return Collections.emptyList();
    }

    private void addToScope(View target, String alias) {
        logger.log(Level.INFO, "registering "+target+" as alias '"+alias+"'");
        scope = scope.add(target, alias);
    }

    public Select from(Function expr) throws ScopeException {
        if (expr instanceof Join) {
            return fromJoin((Join) expr);
        } else if (expr instanceof View) {
            return fromView((View) expr);
        } else  {
            throw new RuntimeException("ERROR: handling of " + expr.toString() + " NYI");
        }
    }

    protected Select fromView(View view) {
        assert !(view instanceof Join);
        View copy = (View)view.copy();
        String alias = aliases.getViewAlias(copy.getName());
        addToScope(copy, alias);
        from.add(new FromClause(scope, copy, alias));
        return this;
    }

    protected Select fromJoin(Join join) throws ScopeException {
        try {
            Mapping mapping = assertThatJoinIsInScope(join);
            if (mapping.getReference().equals(join.getSource())) {
                from.add(joinClause(join, aliases.getViewAlias(join.getTarget().getName())));
            } else {
                Join relink = join.changeSource(mapping.getReference());
                from.add(joinClause(relink, aliases.getViewAlias(join.getTarget().getName())));
            }
            return this;
        } catch (ScopeException e) {
            throw new ScopeException(e.getMessage()+ " when selecting "+join);
        }
    }

    private JoinClause joinClause(Join join, String alias) {
        addToScope(join, alias);
        return new JoinClause(scope, join.getTarget(), join, alias);
    }

    public Select join(View target, Function join) throws ScopeException {
        from.add(joinClause(new Join(target, join), aliases.getViewAlias(target.getName())));
        return this;
    }

    private Mapping assertThatJoinIsInScope(Join join) throws ScopeException {
        return scope.resolves(join.getSource());
    }

    public Select where(Function predicate) {
        where.add(new ScopedFunction(scope, predicate));
        return this;
    }

    public Select groupBy(Function arg) {
        groupBy.add(new SimpleClause(scope, arg));
        return this;
    }

    public Select groupBy(List<? extends Function> args) {
        for (Function arg : args)
            groupBy.add(new SimpleClause(scope, arg));
        return this;
    }

    public Select having(Function predicate) {
        having.add(new ScopedFunction(scope, predicate));
        return this;
    }

    public String print() throws ScopeException {
        return print(scope, new PrintResult()).print();
    }

    public PrintResult print(Scope scope, PrintResult result) throws ScopeException {
        result.append(SELECT);
        printSelectorClause(result);
        result.space().append(FROM);
        printFromClause(result);
        if (!where.isEmpty()) {
            result.space().append(WHERE).space().append(scope, Functions.AND(where));
        }
        if (!groupBy.isEmpty()) {
            result.space().append(GROUPBY);
            printClauseList(result, groupBy);
        }
        if (!having.isEmpty()) {
            result.space().append(HAVING).space().append(scope, Functions.AND(having));
        }
        return result;
    }

    private void printSelectorClause(PrintResult result) throws ScopeException {
        if (!selection.isEmpty()) {
            printClauseList(result, selection);
        } else {
            List<SelectClause> alls = new ArrayList<>();
            AliasMap myAliases = new AliasMap(aliases);
            for (FromClause clause : from) {
                if (clause.getValue().getSelectors().isEmpty()) {
                    alls.add(new SelectClause(this, clause.getScope(), Functions.STAR(clause.getValue())));
                } else {
                    List<SelectClause> forFromClause = new ArrayList<>();
                    for (Selector selector : clause.getValue().getSelectors()) {
                        Scope defScope = scope.resolves(selector.getType().getTail().orElse(clause.getValue())).getScope();
                        Optional<SelectClause> select = createSelectClause(defScope, selector, myAliases.getAlias(selector.getName()));
                        if (select.isPresent()) forFromClause.add(select.get());
                    }
                    if (forFromClause.isEmpty()) {
                        // if all from's selectors are optionals
                        alls.add(new SelectClause(this, clause.getScope(), Functions.STAR(clause.getValue())));
                    } else {
                        alls.addAll(forFromClause);
                    }
                }
            }
            // no specific selector, using STAR selector
            printClauseList(result, alls);
        }
    }

    private void printFromClause(PrintResult result) throws ScopeException {
        if (from.isEmpty()) result.error(new ScopeException("missing FROM clause"));
        for (int i = 0; i < from.size(); i++) {
            FromClause clause = from.get(i);
            if (i > 0 && !(clause instanceof JoinClause)) {
                result.comma();
            }
            result.space();
            clause.print(result);
        }
    }

    private void printClauseList(PrintResult result, List<? extends Printer> clauses) throws ScopeException {
        for (int i = 0; i < clauses.size(); i++) {
            if (i > 0) result.comma();
            result.space();
            clauses.get(i).print(result);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Select select = (Select) o;
        return from.equals(select.from) &&
                where.equals(select.where) &&
                having.equals(select.having) &&
                groupBy.equals(select.groupBy);
    }

    @Override
    public int hashCode() {
        return Objects.hash(from, where, having, groupBy);
    }

    @Override
    public String toString() {
        return "[SELECT "+getType()+"]";
    }
}
