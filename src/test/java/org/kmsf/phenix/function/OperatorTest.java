package org.kmsf.phenix.function;

import org.junit.jupiter.api.Test;
import org.kmsf.phenix.database.Column;
import org.kmsf.phenix.database.ScopeException;
import org.kmsf.phenix.database.Table;

import static org.assertj.core.api.Assertions.*;

class OperatorTest {

    @Test
    void should_implement_copy() throws ScopeException {
        // given
        Table test = new Table("test");
        Column a = test.column("a");
        Column b = test.column("b");
        // when
        Function x = Functions.ADD(a, b);
        // then
        assertThat(x.copy()).isEqualTo(x);
        assertThat(x.copy().hashCode()).isEqualTo(x.hashCode());
    }

}