package org.kmsf.phenix.database;

import org.junit.jupiter.api.Test;
import org.kmsf.phenix.sql.PrintResult;
import org.kmsf.phenix.sql.Scope;
import org.kmsf.phenix.algebra.FunctionType;

import static org.kmsf.phenix.algebra.Functions.*;

import static org.junit.jupiter.api.Assertions.*;

class JoinTest {

    @Test
    public void test() throws ScopeException {
        Table a = new Table("a");
        Table b = new Table("b");
        Join join = new Join(b, EQUALS(a.column("ID"), b.column("A_ID_FK")));
        assertEquals(new FunctionType(a,b), join.getSource());
        Scope scope = new Scope();
        scope.add(a, "a");
        scope.add(b, "b");
        assertEquals("INNER JOIN b ON a.ID=b.A_ID_FK", join.print(scope, new PrintResult()).toString());
    }

}