package org.kmsf.phenix.database;

import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.Test;

class ColumnTest {

    @Test
    void shouldImplementEqual() throws ScopeException {
        // given
        Table a = new Table("a");
        // when
        Table b = new Table("b");
        // then
        assertThat(a.column("a")).isEqualTo(a.column("a"));
        assertThat(a.column("a")).isNotEqualTo(b.column("a"));
        assertThat(a.column("a")).isNotEqualTo(a.column("b"));
    }

    @Test
    void shouldHaveTypeEqualsToTheDefiningTable() throws ScopeException {
        // given
        Table a = new Table("a");
        // when
        Column test = a.column("test");
        // then
        assertThat(test.getType().getValues()).containsExactly(a);
    }

}