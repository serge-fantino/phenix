package org.kmsf.phenix.database;

import org.junit.jupiter.api.Test;
import org.kmsf.phenix.database.sql.PrintResult;
import org.kmsf.phenix.database.sql.Scope;
import org.kmsf.phenix.function.FunctionType;
import org.kmsf.phenix.logical.Entity;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class TableTest {

    @Test
    void getName() {
        assertEquals("table",new Table("table").getName().get());
    }

    @Test
    void primaryKey() throws ScopeException {
        Table table = new Table("table");
        assertEquals(Collections.emptyList(), table.getPK());
        table.PK(table.column("ID"));
        assertEquals(Collections.singletonList(new Column(table, "ID")), table.getPK());
        assertNotEquals(Collections.singletonList(new Column(table, "BADID")), table.getPK());
        assertThrows(ScopeException.class, () -> table.PK("ID"));
    }

    @Test
    void column() {
        Table table = new Table("table");
        assertEquals(new FunctionType(table), table.column("test").getSource());
    }

    @Test
    void getSource() {
        Table table = new Table("table");
        assertEquals(null, table.getSource());
    }

    @Test
    void testEquals() {
        Table table = new Table("table");
        assertEquals(new Table("table"), table);
        assertEquals(new Table("table"), new Entity(table));
        assertNotEquals(new Table("tableX"), table);
        assertNotEquals(null, table);
    }

    @Test
    void print() throws ScopeException {
        assertEquals("'table'", new Table("table").print(new Scope(), new PrintResult()).print());
    }
}