package org.kmsf.phenix.function;

import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.kmsf.phenix.database.Column;
import org.kmsf.phenix.database.ScopeException;
import org.kmsf.phenix.database.Table;
import org.kmsf.phenix.sql.PrintResult;
import org.kmsf.phenix.sql.Scope;

import static org.junit.jupiter.api.Assertions.*;

class ScopedFunctionTest {

    @Test
    void should_be_identical_to_the_definition_except_for_rpinting() throws ScopeException {
        // given
        Table test = new Table("test");
        Column a = test.column("a");
        // when
        Scope x = new Scope(test).add(test, "x");
        ScopedFunction aa = new ScopedFunction(x, a);
        // then
        assertThat(aa.getName()).isEqualTo(a.getName());
        assertThat(aa.getPrecedence()).isEqualTo(a.getPrecedence());
        assertThat(aa.getSelectors()).isEqualTo(a.getSelectors());
        assertThat(aa.getSystemName()).isEqualTo(a.getSystemName());
        assertThat(aa.getType()).isEqualTo(a.getType());
        assertThat(aa.copy()).isEqualTo(aa);
        assertThat(aa.hashCode()).isEqualTo(a.hashCode());
        // when
        Scope y = new Scope(test).add(test, "y");
        // then
        assertThat(aa.print(y, new PrintResult()).print()).isEqualTo("x.a");
        assertThat(a.print(y, new PrintResult()).print()).isEqualTo("y.a");
    }

}