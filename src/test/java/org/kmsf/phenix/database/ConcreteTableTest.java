package org.kmsf.phenix.database;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class ConcreteTableTest {

    @Test
    void should_concrete_table_inherits_from_table() throws ScopeException {
        // given
        Table tPeople = new Table("people");
        tPeople.PK("ID");
        tPeople.column("name");
        tPeople.column("city");
        tPeople.column("revenue");
        // when
        ConcreteTable cPeople = new ConcreteTable(tPeople);
        // then
        assertThat(cPeople.hashCode()).isEqualTo(tPeople.hashCode());
        assertThat(cPeople.getType().getValues()).containsExactly(tPeople);
        assertThat(cPeople.inheritsFrom(tPeople)).isTrue();
    }

    @Test
    void should_select_all_table_columns() throws ScopeException {
        // given
        Table tPeople = new Table("people");
        tPeople.PK("ID");
        tPeople.column("name");
        tPeople.column("city");
        tPeople.column("revenue");
        // when
        ConcreteTable cPeople = new ConcreteTable(tPeople);
        // then
        assertThat(new Select(cPeople).print()).isEqualTo("SELECT p.ID, p.name, p.city, p.revenue FROM people p");
    }

    @Test
    void should_throw_exception_when_selecting_undefined_column_from_concrete_table() throws ScopeException {
        // given
        Table tPeople = new Table("people");
        tPeople.PK("ID");
        tPeople.column("name");
        tPeople.column("city");
        tPeople.column("revenue");
        // when
        ConcreteTable cPeople = new ConcreteTable(tPeople);
        // then
        assertThatThrownBy(() -> new Select().from(cPeople).select(cPeople.column("undefined")).print())
            .isInstanceOf(ScopeException.class);
    }

}