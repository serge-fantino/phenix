package org.kmsf.phenix.sql;

import org.kmsf.phenix.database.ScopeException;
import org.kmsf.phenix.function.Function;

import java.util.ArrayList;

public class PrintResult {

    public static String identifierQuote = "\"";
    public static String literalQuote = "'";

    private StringBuffer buffer = new StringBuffer();
    private ArrayList<ScopeException> errors = new ArrayList<>();

    public PrintResult() {
    }

    public PrintResult space() {
        buffer.append(" ");
        return this;
    }

    public PrintResult comma() {
        buffer.append(",");
        return this;
    }

    public PrintResult dot() {
        buffer.append(".");
        return this;
    }

    public PrintResult append(String data) {
        buffer.append(data);
        return this;
    }

    public PrintResult append(Scope scope, Function expr) throws ScopeException {
        return expr.print(scope, this);
    }

    public PrintResult append(Scope scope, Function expr, boolean enclose) throws ScopeException {
        if (enclose) append("(");
        expr.print(scope, this);
        if (enclose) append(")");
        return this;
    }

    public PrintResult appendIdentifier(String literal, boolean quoteIdentifier) {
        if (quoteIdentifier)
            buffer.append(identifierQuote).append(literal).append(identifierQuote);
        else
            buffer.append(literal);
        return this;
    }

    public PrintResult appendConstant(Object value) {
        if (value instanceof String) {
            buffer.append(literalQuote).append(value).append(literalQuote);
        } else {
            buffer.append(value);
        }
        return this;
    }

    public void error(ScopeException e) {
        errors.add(e);
    }

    public String print() throws ScopeException {
        if (!errors.isEmpty())
            throw new ScopeException(errors.get(0).getMessage() + ": " + buffer.toString());
        return buffer.toString();
    }

    public int size() {
        return buffer.length();
    }

    @Override
    public String toString() {
        try {
            return print();
        } catch (ScopeException e) {
            return e.toString();
        }
    }
}
