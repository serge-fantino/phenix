package org.kmsf.phenix.sql;

import org.kmsf.phenix.database.ScopeException;
import org.kmsf.phenix.database.Select;
import org.kmsf.phenix.database.Selector;
import org.kmsf.phenix.database.View;
import org.kmsf.phenix.function.Function;
import org.kmsf.phenix.function.FunctionType;
import org.kmsf.phenix.function.Functions;

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

    /**
     * return a selector based on that mapping
     *
     * @return
     */
    public Selector asSelector() {
        return new Selector() {

            @Override
            public PrintResult print(Scope scope, PrintResult result) throws ScopeException {
                String from = scope.get(view).getAlias();
                return result.append(from).append(".").append(alias.orElse(definition.getSystemName().get()));
            }

            @Override
            public FunctionType getSource() {
                return definition.getSource();
            }

            @Override
            public View getView() {
                return view;
            }

            @Override
            public Function redux() {
                return definition.redux();
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
