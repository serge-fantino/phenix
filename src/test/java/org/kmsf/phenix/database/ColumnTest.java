package org.kmsf.phenix.database;

import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.Test;

class ColumnTest {

    @Test
    void should_implements_equal() throws ScopeException {
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
    void should_have_type_equals_to_the_defining_table() throws ScopeException {
        // given
        Table a = new Table("a");
        // when
        Column test = a.column("test");
        // then
        assertThat(test.getType().getValues()).containsExactly(a);
    }

    @Test
    void should_relink_column() throws ScopeException {
        // given
        Table a = new Table("a").PK("ID");
        Table b = new Table("b").PK("ID");
        Table c = new Table("c").PK("ID");
        Join a2b = a.join(b, "B_ID_FK");
        Join b2c = b.join(c, "C_ID_FK");
        // when
        Column x = c.column("X");
        // then
        assertThat(x.relinkTo(b2c).getType().getValues()).containsExactly(b2c);
        assertThat(x.relinkTo(b2c).relinkTo(c).getType().getValues()).containsExactly(c);
    }

}