package org.kmsf.phenix.database;

import org.kmsf.phenix.database.sql.*;
import org.kmsf.phenix.function.FunctionType;
import org.kmsf.phenix.function.Function;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * The Select statement purpose is to generate a syntactically correct SQL SELECT statement
 */
public class Select extends Statement {

    public static final String SELECT = "SELECT";
    public static final String FROM = "FROM";
    public static final String INNERJOIN = "INNER JOIN";
    public static final String ON = "ON";
    public static final String GROUPBY = "GROUP BY";
    public static final String AS = "AS";

    private ArrayList<FromClause> from = new ArrayList<>();
    private ArrayList<SelectClause> selectors = new ArrayList<>();
    private ArrayList<GroupByClause> groupBy = new ArrayList<>();

    private Character alias = 'a';
    private HashSet<String> aliases = new HashSet<>();

    private Scope scope = new Scope();

    public Select() {}

    public Select select(Column column) {
        selectors.add(new SelectClause(scope, column, getAlias(column.getName().get())));
        return this;
    }

    public Select select(Function expr) {
        selectors.add(new SelectClause(scope, expr));
        return this;
    }

    public Select select(Function expr, String alias) {
        selectors.add(new SelectClause(scope, expr, getAlias(alias)));
        return this;
    }

    @Override
    public FunctionType getSource() {
        return new FunctionType(from.stream().map(f -> f.getValue()).collect(Collectors.toList()));
    }

    public Scope getScope() {
        return scope;
    }


    private String getAlias(Optional<String> name) {
        if (name.isPresent())
            return getAlias(name.get().substring(0,1));
        else
            return getAlias((alias++).toString());
    }

    private String getAlias(String name) {
        int idx = 1;
        String alias = name;
        while (aliases.contains(alias)) {
            alias = name+(idx++);
        }
        aliases.add(alias);
        return alias;
    }

    private void addToScope(View view, String alias) {
        if (scope.contains(view)) {
            // push a new scope
            scope = new Scope(scope);
        }
        scope.add(view, new Mapping(view, alias));
        // check view sources
        Optional<FunctionType> source = Optional.ofNullable(view.getSource());
        if (source.isPresent()) for (Function value : source.get().getValues()) {
            if (!scope.contains(value))
                scope.add(value, new Mapping(view, alias));
        }
    }

    public Select from(View view) {
        String alias = getAlias(view.getName());
        addToScope(view, alias);
        from.add(new FromClause(scope, view, alias));
        return this;
    }

    public Select from(Function expr) {
        if (expr instanceof View) {
            return from((View)expr);
        } else if (expr instanceof Join) {
            return from((Join)expr);
        } else {
            throw new RuntimeException("ERROR: handling of "+expr.toString()+" NYI");
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

    public Select groupBy(Function expr) {
        groupBy.add(new GroupByClause(scope, expr));
        return this;
    }

    public String print() throws ScopeException {
        return print(scope, new PrintResult()).print();
    }

    public PrintResult print(Scope scope, PrintResult result) {
        result.append(SELECT);
        printClauseList(result, selectors);
        result.space().append(FROM);
        printFromClause(result);
        if (!groupBy.isEmpty()) {
            result.space().append(GROUPBY);
            printClauseList(result, groupBy);
        }
        return result;
    }

    private void printFromClause(PrintResult result) {
        if (from.isEmpty()) result.error(new ScopeException("missing FROM clause"));
        for (int i=0; i<from.size(); i++) {
            FromClause clause = from.get(i);
            if (i>0 && !(clause instanceof JoinClause))
                result.space().comma();
            else
                result.space();
            clause.print(result);
        }
    }

    private void printClauseList(PrintResult result, List<? extends Printer> clauses) {
        for (int i=0; i<clauses.size(); i++) {
            if (i>0) result.comma();
            result.space();
            clauses.get(i).print(result);
        }
    }

    @Override
    public Selector selector(String definition) {
        throw new RuntimeException("NYI");
    }
}
