package org.kmsf.phenix.database;

import org.assertj.core.api.Assertions;
import org.assertj.core.api.ListAssert;
import org.junit.jupiter.api.Test;
import org.kmsf.phenix.logical.Attribute;
import org.kmsf.phenix.logical.Query;
import org.kmsf.phenix.sql.PrintResult;
import org.kmsf.phenix.sql.Scope;
import org.kmsf.phenix.function.FunctionType;
import org.kmsf.phenix.logical.Entity;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

class TableTest {

    @Test
    void should_have_empty_pk_by_default() throws ScopeException {
        // given
        Table table = new Table("table");
        // when
        Key empty = new Key(table);
        // then
        assertThat(table.getPK()).isEqualTo(new Key(table));
    }

    @Test
    void should_allow_pk_definition_from_column() throws ScopeException {
        // given
        Table table = new Table("table");
        Table other = new Table("other");
        // when
        table.PK("ID");
        // then
        assertEquals(new Key(table, Collections.singletonList(new Column(table, "ID"))), table.getPK());
        assertNotEquals(new Key(table, Collections.singletonList(new Column(table, "BADID"))), table.getPK());
    }

    @Test
    void should_not_allow_pk_redefinition() throws ScopeException {
        // given
        Table table = new Table("table");
        Table other = new Table("other");
        // given
        table.PK(table.column("ID"));
        // then
        assertThrows(ScopeException.class, () -> table.PK("ID"));
    }

    @Test
    void should_not_allow_pk_definition_from_different_table() {
        // given
        Table table = new Table("table");
        // when
        Table other = new Table("other");
        // then
        assertThrows(ScopeException.class, () -> table.PK(other.column("ID")));
    }

    @Test
    void should_column_type_be_the_table() throws ScopeException {
        // given
        Table table = new Table("table");
        // when
        Column test = table.column("test");
        // then
        assertThat(test.getType()).isEqualTo(table.getType());// because table.getType()===table
    }

    @Test
    void should_table_type_be_fixed_oint() {
        // given
        Table table = new Table("table");
        // when
        FunctionType type = table.getType();
        // then
        assertThat(type).isEqualTo(new FunctionType(table));
    }

    @Test
    void should_tables_equal_when_same_name_only() {
        // given
        Table table = new Table("table");
        // when
        Table tablex = new Table("tableX");
        // then
        assertThat(table)
                .isEqualTo(table)
                .isEqualTo(new Table(table.getName().orElse("table")))
                .isNotEqualTo(tablex);
    }

    @Test
    void should_table_copy_be_a_new_table() throws ScopeException {
        // given
        Table table = new Table("table").PK("ID");
        // when
        Table copy = (Table)table.copy();
        // then
        assertThat(table).isEqualTo(copy);
        assertThat(table.getPK()).isEqualTo(copy.getPK());
        assertThat(table.getSelectors()).isEqualTo(copy.getSelectors());
    }

    @Test
    void should_table_copy_be_independent_from_original_table() throws ScopeException {
        // given
        Table table = new Table("table").PK("ID");
        Table copy = (Table)table.copy();
        // when
        Column some_original = table.column("some_original");
        Column some_copy = copy.column("some_copy");
        // then
        assertThat(table.getSelectors()).contains(some_original).doesNotContain(some_copy);
        assertThat(copy.getSelectors()).contains(some_copy).doesNotContain(some_original);
    }

    @Test
    void should_print_the_table_identifier() throws ScopeException {
        assertEquals("table", new Table("table").print(new Scope(new Select()), new PrintResult()).print());
        assertEquals("\"table\"", new Table("table", true).print(new Scope(new Select()), new PrintResult()).print());
    }

    @Test
    void should_fails_create_join_if_no_pk() throws ScopeException {
        // given
        Table tPeople = new Table("people");
        Table tDepartment = new Table("department");
        // when
        Throwable throwable = catchThrowable(() -> tDepartment.join(tPeople.FK("DEP_ID_FK")));
        // then
        assertThat(throwable).isInstanceOf(ScopeException.class).hasMessageContaining("has no PK defined");
    }

}