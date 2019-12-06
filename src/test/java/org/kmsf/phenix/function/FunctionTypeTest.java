package org.kmsf.phenix.function;

import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.kmsf.phenix.database.Table;
import org.kmsf.phenix.logical.Entity;

import static org.junit.jupiter.api.Assertions.*;

class FunctionTypeTest {

    @Test
    void should_equals_when_same_definition_and_order() {
        // given
        Table a = new Table("A");
        Table b = new Table("B");
        // when
        FunctionType typeAB = new FunctionType(a, b);
        // then
        assertEquals(typeAB
                , new FunctionType((new Table("A"))).add(new Table("B")));
    }

    @Test
    void should_equals_when_inheriting_type() {
        // given
        Table a = new Table("A");
        // when
        Entity A = new Entity(a);
        // then
        assertThat(a.getType()).isEqualTo(A.getType());
        assertThat(A.getType()).isEqualTo(a.getType());
    }
}