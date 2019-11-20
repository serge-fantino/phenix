package org.kmsf.phenix.database;

import org.junit.jupiter.api.Test;
import org.kmsf.phenix.database.sql.PrintResult;
import org.kmsf.phenix.database.sql.Scope;
import org.kmsf.phenix.function.FunctionType;

import static org.junit.jupiter.api.Assertions.*;

class TableTest {

    @Test
    void getName() {
        assertEquals("table",new Table("table").getName().get());
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
        assertEquals(new Table("table"), new View() {
            @Override
            public PrintResult print(Scope scope, PrintResult result) {
                return null;
            }

            @Override
            public FunctionType getSource() {
                return new FunctionType(table);
            }

            @Override
            public Selector selector(String definition) {
                return null;
            }
        });
        assertNotEquals(new Table("tableX"), table);
        assertNotEquals(null, table);
    }

    @Test
    void print() throws ScopeException {
        assertEquals("'table'", new Table("table").print(new Scope(), new PrintResult()).print());
    }
}