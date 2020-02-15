package org.kmsf.phenix.database;

import org.junit.jupiter.api.Test;
import org.kmsf.phenix.sql.PrintResult;
import org.kmsf.phenix.sql.Scope;
import org.kmsf.phenix.algebra.FunctionType;
import org.kmsf.phenix.logical.Entity;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class TableTest {

    @Test
    void getName() {
        assertEquals("table",new Table("table").getName().get());
    }

    @Test
    void primaryKey() throws ScopeException {
        Table table = new Table("table");
        Table other = new Table("other");
        assertEquals(Collections.emptyList(), table.getPK());
        table.PK(table.column("ID"));
        assertEquals(Collections.singletonList(new Column(table, "ID")), table.getPK());
        assertNotEquals(Collections.singletonList(new Column(table, "BADID")), table.getPK());
        assertThrows(ScopeException.class, () -> table.PK("ID"));
        assertThrows(ScopeException.class, () -> other.PK(table.column("badID")));
    }

    @Test
    void column() throws ScopeException {
        Table table = new Table("table");
        assertEquals(new FunctionType(table), table.column("test").getSource());
    }

    @Test
    void getSource() {
        Table table = new Table("table");
        assertEquals(new FunctionType(), table.getSource());
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
        assertEquals("table", new Table("table").print(new Scope(), new PrintResult()).print());
        assertEquals("\"table\"", new Table("table", true).print(new Scope(), new PrintResult()).print());
    }

    @Test
    void concreteTable() throws ScopeException {
        Table tPeople = new Table("people");
        assertEquals("SELECT p.* FROM people p"
                , new Select(tPeople).print());
        tPeople.PK("ID");
        tPeople.column("name");
        tPeople.column("city");
        tPeople.column("revenue");
        ConcreteTable cPeople = new ConcreteTable(tPeople);
        assertEquals(cPeople.hashCode(), tPeople.hashCode());
        assertEquals(cPeople, tPeople);
        assertEquals(tPeople, cPeople);
        assertEquals("SELECT p.ID, p.name, p.city, p.revenue FROM people p"
                , new Select(cPeople).print());
        assertThrows(ScopeException.class,
                () -> new Select().from(cPeople).select(cPeople.column("undefined")).print());
    }

    @Test
    void redux() {
        Table test = new Table("test");
        assertEquals(test, test.redux());
        assertTrue(test.redux() == test.redux().redux());
    }
}