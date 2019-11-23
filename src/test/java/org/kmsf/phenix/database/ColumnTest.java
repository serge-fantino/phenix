package org.kmsf.phenix.database;

import org.junit.jupiter.api.Test;
import org.kmsf.phenix.database.sql.PrintResult;
import org.kmsf.phenix.database.sql.Scope;

import static org.junit.jupiter.api.Assertions.*;

class ColumnTest {

    @Test
    void equals() {
        Table a = new Table("a");
        Table b = new Table("b");
        assertEquals(a.column("a"), a.column("a"));
        assertNotEquals(a.column("a"), b.column("a"));
        assertNotEquals(a.column("a"), a.column("b"));
    }

}