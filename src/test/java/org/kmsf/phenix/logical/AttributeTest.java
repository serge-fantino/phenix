package org.kmsf.phenix.logical;

import org.junit.jupiter.api.Test;
import org.kmsf.phenix.database.ScopeException;
import org.kmsf.phenix.database.Table;

import static org.junit.jupiter.api.Assertions.*;

class AttributeTest {

    @Test
    void constructor() {
        Table ta = new Table("a");
        Entity a = new Entity("aa", ta);
        assertDoesNotThrow(() -> a.attribute("a", ta.column("a")));
        Table tb = new Table("b");
        assertThrows(ScopeException.class, () -> a.attribute("a", tb.column("a")));
    }

    @Test
    void equals() throws ScopeException {
        Table ta = new Table("a");
        Entity a = new Entity("aa", ta);
        Table tb = new Table("b");
        Entity b = new Entity("bb", tb);
        assertEquals(a.attribute("a"), a.attribute("a"));
        assertEquals(a.attribute("a"), a.attribute("a", ta.column("a")));
        assertEquals(a.attribute("a"), a.attribute("aaa", ta.column("a")));
        assertEquals(a.attribute("a"), ta.column("a"));
        assertNotEquals(a.attribute("a"), a.attribute("b"));
        assertNotEquals(a.attribute("a"), a.attribute("a", ta.column("b")));
        assertNotEquals(a.attribute("a"), b.attribute("a"));
        assertNotEquals(a.attribute("a"), tb.column("a"));
        assertNotEquals(a.attribute("a"), ta.column("b"));
    }

}