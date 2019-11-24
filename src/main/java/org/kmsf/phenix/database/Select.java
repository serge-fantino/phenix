package org.kmsf.phenix.database;

import org.kmsf.phenix.sql.*;
import org.kmsf.phenix.function.FunctionType;
import org.kmsf.phenix.function.Function;
import org.kmsf.phenix.function.Functions;

import java.util.*;
import java.util.stream.Collectors;

/**
 * The Select statement purpose is to generate a syntactically correct SQL SELECT statement
 */
public class Select extends Statement {

    public static final String SELECT = "SELECT";
    public static final String FROM = "FROM";
    public static final String INNERJOIN = "INNER JOIN";
    public static final String ON = "ON";
    public static final String WHERE = "WHERE";
    public static final String GROUPBY = "GROUP BY";
    public static final String HAVING = "HAVING";
    public static final String AS = "AS";

    private List<FromClause> from = new ArrayList<>();
    private List<SelectClause> selectors = new ArrayList<>();
    private List<Function> where = new ArrayList<>();
    private List<Function> having = new ArrayList<>();
    private List<GroupByClause> groupBy = new ArrayList<>();

    private Character alias = 'a';
    private HashSet<String> aliases = new HashSet<>();

    private Scope scope = new Scope();

    public Select() {
    }

    public Select(View view) {
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
        this.selectors = new ArrayList<>(select.selectors);
        this.from = new ArrayList<>(select.from);
        this.where = new ArrayList<>(select.where);
        this.having = new ArrayList<>(select.having);
        this.groupBy = new ArrayList<>(select.groupBy);
        this.alias = select.alias;
        this.aliases = new HashSet<>(select.aliases);
        this.scope = new Scope(select.scope);
    }

    public Select select(Column column) {
        selectors.add(new SelectClause(this, scope, column, getAlias(column.getName().get())));
        return this;
    }

    public Select select(Function expr) {
        selectors.add(new SelectClause(this, scope, expr));
        return this;
    }

    public Select select(Function expr, String alias) {
        selectors.add(new SelectClause(this, scope, expr, getAlias(alias)));
        return this;
    }

    @Override
    public FunctionType getSource() {
        return new FunctionType(from.stream().map(f -> f.getValue()).collect(Collectors.toList()));
    }

    @Override
    public List<Function> getPK() {
        if (!groupBy.isEmpty()) {
            // return the groupBy definition as the new PK
            return groupBy.stream()
                    .map(clause -> clause.getValue())
                    .collect(Collectors.toList());
        }
        // only using the from - return PK even if the column is not selected
        return from.stream()
                .flatMap(clause -> clause.getValue().getPK().stream())
                .collect(Collectors.toList());
    }

    @Override
    public Scope getScope() {
        return scope;
    }

    @Override
    public List<? extends Selector> getSelectors() {
        return selectors.stream().map(clause -> clause.asSelector()).collect(Collectors.toList());
    }

    @Override
    public Selector selector(String definition) throws ScopeException {
        assert definition != null;
        // the Select.selector() return a reference to a selector already defined in the scope, using its alias name
        for (SelectClause clause : selectors) {
            if (definition.equals(clause.getAlias().orElse(null))) return clause.asSelector();
            if (definition.equals(clause.getDefinition().getSystemName().orElse(null))) return clause.asSelector();
        }
        throw new ScopeException("cannot find selector '" + definition + "'");
    }

    private String getAlias(Optional<String> name) {
        if (name.isPresent())
            return getAlias(name.get().substring(0, 1));
        else
            return getAlias((alias++).toString());
    }

    private String getAlias(String name) {
        int idx = 1;
        String alias = name;
        while (aliases.contains(alias)) {
            alias = name + (idx++);
        }
        aliases.add(alias);
        return alias;
    }

    private void addToScope(View view, String alias) {
        if (scope.contains(view)) {
            // push a new scope
            scope = new Scope(scope);
        }
        scope.add(view, alias);
        // check view sources
        Optional<FunctionType> source = Optional.ofNullable(view.getSource());
        if (source.isPresent()) for (Function value : source.get().getValues()) {
            if (!scope.contains(value))
                scope.add(value, alias);
        }
    }

    public Select from(View view) {
        String alias = getAlias(view.getName());
        addToScope(view, alias);
        from.add(new FromClause(view.getScope(), view, alias));
        return this;
    }

    public Select from(Function expr) {
        if (expr instanceof View) {
            return from((View) expr);
        } else if (expr instanceof Join) {
            return from((Join) expr);
        } else {
            throw new RuntimeException("ERROR: handling of " + expr.toString() + " NYI");
        }
    }

    private JoinClause joinClause(View view, Function join, String alias) {
        addToScope(view, alias);
        return new JoinClause(scope, view, join, alias);
    }

    public Select innerJoin(Table table, Function join) {
        from.add(joinClause(table, join, getAlias(table.getName())));
        return this;
    }

    public Select from(Join join) {
        from.add(joinClause(join.getTarget(), join.getDefinition(), getAlias(join.getTarget().getName())));
        return this;
    }

    public Select where(Function predicate) {
        where.add(predicate);
        return this;
    }

    public Select groupBy(Function arg) {
        groupBy.add(new GroupByClause(scope, arg));
        return this;
    }

    public Select groupBy(List<? extends Function> args) {
        for (Function arg : args)
            groupBy.add(new GroupByClause(scope, arg));
        return this;
    }

    public Select having(Function predicate) {
        having.add(predicate);
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
        if (!selectors.isEmpty()) {
            printClauseList(result, selectors);
        } else {
            List<SelectClause> alls = new ArrayList<>();
            for (FromClause clause : from) {
                if (clause.getValue().getSelectors().isEmpty()) {
                    alls.add(new SelectClause(this, scope, Functions.STAR(clause.getValue())));
                } else {
                    for (Selector selector : clause.getValue().getSelectors()) {
                        Scope defScope = scope.get(clause.getValue()).getScope();
                        alls.add(new SelectClause(this, defScope, selector));
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
            if (i > 0 && !(clause instanceof JoinClause))
                result.space().comma();
            else
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

}
