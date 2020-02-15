package org.kmsf.phenix.function;

import org.junit.jupiter.api.Test;
import org.kmsf.phenix.database.Column;
import org.kmsf.phenix.database.ScopeException;
import org.kmsf.phenix.database.Select;
import org.kmsf.phenix.database.Table;
import org.kmsf.phenix.logical.Entity;
import org.kmsf.phenix.sql.Mapping;
import org.kmsf.phenix.sql.PrintResult;
import org.kmsf.phenix.sql.Scope;

import java.util.Collections;

import static org.kmsf.phenix.function.Functions.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.*;

class FunctionTest {

    @Test
    void testFunctions() throws ScopeException {
        Table t = new Table("customer");
        var scope = new Scope(new Select());
        scope = scope.add(t, "t");
        assertEquals("t.a=t.b", EQUALS(t.column("a"), t.column("b")).print(scope, new PrintResult()).print());
        assertEquals("SUM(t.a)", SUM(t.column("a")).print(scope, new PrintResult()).print());
    }

    @Test
    void listFunctions() throws ScopeException {
        Table t = new Table("customer").PK("a", "b");
        var scope = new Scope(new Select());
        scope = scope.add(t, "t");
        assertEquals("t.a=t.a AND t.b=t.b",
                EQUALS(t.getPK().getKeys(), t.getPK().getKeys()).print(scope, new PrintResult()).print());
        assertThrows(ScopeException.class, () -> EQUALS(Collections.singletonList(t.column("bad")), t.getPK().getKeys()));
    }

    @Test
    void testPrecedenceOrder() throws ScopeException {
        assertTrue(Function.PRECEDENCE_ORDER_VIEW>Function.PRECEDENCE_ORDER_STATEMENT);
        assertEquals(
                "(10+10)*(10+10)",
                MULTIPLY(ADD(CONST(10), CONST(10)), ADD(CONST(10), CONST(10))).print(new Scope(new Select()), new PrintResult()).print());
        assertEquals(
                "10*10+10*10",
                ADD(MULTIPLY(CONST(10), CONST(10)), MULTIPLY(CONST(10), CONST(10))).print(new Scope(new Select()), new PrintResult()).print());
    }

    @Test
    void should_relink_not_change_original() throws ScopeException {
        // given
        Table t = new Table("t");
        Entity e = new Entity("e", t);
        // when
        Column a = t.column("a");
        // then
        assertThat(a.relinkTo(e).getType().getValues()).containsExactly(e);
        assertThat(a.getType().getValues()).containsExactly(t);
    }

    @Test
    void should_be_equals_when_relinked() throws ScopeException {
        // given
        Table t = new Table("t");
        Entity e = new Entity("e", t);
        // when
        Column a = t.column("a");
        // then
        assertThat(a.relinkTo(e)).isEqualTo(a);
        assertThat(a).isEqualTo(a.relinkTo(e));
        // when
        Table u = new Table("u");
        Entity f = new Entity("f", u);
        Column b = u.column("b");
        Function eq = EQUALS(a, b);
        assertThat(eq.relinkTo(e).relinkTo(f)).isEqualTo(eq);
        assertThat(eq).isEqualTo(eq.relinkTo(e).relinkTo(f));
    }

}