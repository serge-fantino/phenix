package org.kmsf.phenix.database.sql;

import org.kmsf.phenix.database.ScopeException;

import java.util.ArrayList;

public class PrintResult {

    private String literalQuote = "'";

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

    public PrintResult append(String data) {
        buffer.append(data);
        return this;
    }

    public PrintResult appendLiteral(String literal) {
        buffer.append(literalQuote).append(literal).append(literalQuote);
        return this;
    }

    public void error(ScopeException e) {
        errors.add(e);
    }

    public String print() throws ScopeException {
        if (!errors.isEmpty())
            throw new ScopeException(errors.get(0).getMessage()+": "+buffer.toString());
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
