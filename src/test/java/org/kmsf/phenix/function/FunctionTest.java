package org.kmsf.phenix.function;

import org.junit.jupiter.api.Test;
import org.kmsf.phenix.database.ScopeException;
import org.kmsf.phenix.database.Table;
import org.kmsf.phenix.database.sql.Mapping;
import org.kmsf.phenix.database.sql.PrintResult;
import org.kmsf.phenix.database.sql.Scope;
import org.kmsf.phenix.function.Functions;

import static org.junit.jupiter.api.Assertions.*;

class FunctionTest {

    @Test
    void testFunctions() throws ScopeException {
        Table t = new Table("customer");
        Scope scope = new Scope();
        scope.add(t, new Mapping(t, "t"));
        assertEquals("t.'a'=t.'b'", Functions.EQUALS(t.column("a"), t.column("b")).print(scope, new PrintResult()).print());
        assertEquals("SUM(t.'a')", Functions.SUM(t.column("a")).print(scope, new PrintResult()).print());
    }

    @Test
    void testPrecedenceOrder() {
        assertTrue(Function.PRECEDENCE_ORDER_VIEW>Function.PRECEDENCE_ORDER_STATEMENT);
    }

}