package org.kmsf.phenix.database;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ColumnTest {

    @Test
    void equals() throws ScopeException {
        Table a = new Table("a");
        Table b = new Table("b");
        assertEquals(a.column("a"), a.column("a"));
        assertNotEquals(a.column("a"), b.column("a"));
        assertNotEquals(a.column("a"), a.column("b"));
    }

    @Test
    void redux() {
        Table test = new Table("test");
        Column a = new Column(test, "a");
        assertEquals(a, a.redux());
        assertTrue(a.redux() == a.redux().redux());
    }

}