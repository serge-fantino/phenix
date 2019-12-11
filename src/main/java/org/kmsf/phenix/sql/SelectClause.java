package org.kmsf.phenix.sql;

import org.kmsf.phenix.database.ScopeException;
import org.kmsf.phenix.database.Select;
import org.kmsf.phenix.database.Selector;
import org.kmsf.phenix.database.View;
import org.kmsf.phenix.function.Function;
import org.kmsf.phenix.function.FunctionType;
import org.kmsf.phenix.function.Functions;

import java.util.NoSuchElementException;
import java.util.Optional;

public class SelectClause implements Printer {

    private View view;
    private Scope scope;
    private Function definition;
    private Optional<String> alias = Optional.empty();

    public SelectClause(View view, Scope scope, Function expr) {
        this.view = view;
        this.scope = scope;
        this.definition = expr;
    }

    public SelectClause(View view, Scope scope, Function expr, String alias) {
        this(view, scope, expr);
        this.alias = Optional.ofNullable(alias);
    }

    public SelectClause(View view, Scope scope, Function expr, Optional<String> alias) {
        this(view, scope, expr);
        this.alias = alias;
    }

    public Function getDefinition() {
        return definition;
    }

    public Optional<String> getAlias() {
        return alias;
    }

    public PrintResult print(PrintResult result) throws ScopeException {
        definition.print(scope, result);
        if (alias.isPresent()) {
            if (definition instanceof Selector) {
                Selector selector = (Selector) definition;
                Optional<String> systemName = selector.getSystemName();
                if (!systemName.equals(alias) && alias.isPresent() && !(systemName.isPresent() && systemName.get().equals(Functions._STAR))) {
                    appendAlias(result, alias.get());
                }
            } else
                appendAlias(result, alias.get());
        }
        return result;
    }

    private void appendAlias(PrintResult result, String alias) {
        result.space().append(Select.AS).space().append(alias);
    }

    @Override
    public String toString() {
        return definition.toString()+" AS "+alias;
    }

    /**
     * return a selector based on that mapping
     *
     * @return
     */
    public Selector asSelector() {
        return new Selector() {

            @Override
            public Optional<String> getSystemName() {
                return alias;
            }

            @Override
            public Optional<String> getName() {
                return alias;
            }

            @Override
            public Optional<Function> asSelectorValue() {
                return Optional.of(this);
            }

            @Override
            public Function unwrapReference() {
                return definition;
            }

            @Override
            public PrintResult print(Scope scope, PrintResult result) throws ScopeException {
                if (scope.getContainer().equals(view)) {
                    // the reference is used inside the container that defines it
                    // ... caveate, must use the original scope, not the current one
                    return result.append(SelectClause.this.scope, definition);
                }
                // the reference is used in another context than the one that defines it
                String from = scope.resolves(view).getAlias();
                try {
                    return result.append(from).append(".")
                            .append(alias.orElseGet(() -> definition.getSystemName().orElseThrow()));
                } catch (NoSuchElementException e) {
                    throw new ScopeException("unable to print unnamed reference to "+definition);
                }
            }

            @Override
            public FunctionType getType() {
                return definition.getType();
            }

            @Override
            public View getView() {
                return view;
            }

            @Override
            public boolean equals(Object obj) {
                if (obj==this) return true;
                if (obj instanceof Selector) {
                    return ((Selector)obj).unwrapReference().equals(definition);
                }
                return false;
            }

            @Override
            public int hashCode() {
                return definition.hashCode();
            }

            @Override
            public String toString() {
                return "[Reference to '" + view.getName().orElse("$") + "'.'" + alias.orElse(definition.getName().orElse(definition.getSystemName().toString())) + "'=" + definition + "]";
            }
        };
    }

}
