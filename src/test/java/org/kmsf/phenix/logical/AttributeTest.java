package org.kmsf.phenix.logical;

import org.junit.jupiter.api.Test;
import org.kmsf.phenix.database.*;
import org.kmsf.phenix.function.FunctionType;
import org.xml.sax.helpers.AttributeListImpl;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.*;
import static org.kmsf.phenix.function.Functions.EQUALS;

class AttributeTest {

    @Test
    void should_attribute_type_equals_to_definition_type() throws ScopeException {
        // given
        Table tPeople = new Table("people").PK("ID");
        Entity people = new Entity("people", tPeople);
        // when
        Attribute peopleName = people.attribute("peopleName", tPeople.column("name"));
        // then
        assertThat(peopleName.getType()).isEqualTo(tPeople.column("name").getType()).isEqualTo(tPeople.getType());
    }

    @Test
    void should_handle_attribute_as_join() throws ScopeException {
        // given
        Table tPeople = new Table("people").PK("ID");
        Entity people = new Entity("people", tPeople);
        Attribute peopleName = people.attribute("peopleName", tPeople.column("name"));
        Table tDepartment = new Table("department").PK("ID");
        Entity department = new Entity("department", tDepartment);
        Attribute depName = department.attribute("name");
        // when
        Join join = tPeople.join(tDepartment,"DEP_ID_FK");
        Attribute peopleDepartment =
                people.join("department", department, "DEP_ID_FK");
        // then
        assertThat(new Select().from(people).addToScopeIfNeeded(join)).containsExactly(join);
        assertThat(people.getSelectors()).containsExactly(peopleName, peopleDepartment);
        assertThat(department.getSelectors()).containsExactly(depName);
        assertThat(peopleDepartment.getSelectors()).containsExactly(depName);
        assertThat(new Query().select(people).select(peopleDepartment).print())
                .isEqualTo("SELECT p.name AS peopleName, d.name FROM people p INNER JOIN department d ON p.DEP_ID_FK=d.ID");
        // new Select(tPeople).from(join).print().toString()
    }

    @Test
    void should_apply_target_function_to_join_attribute() throws ScopeException {
        // given
        Table tPeople = new Table("people").PK("ID");
        Entity people = new Entity("people", tPeople);
        Attribute peopleName = people.attribute("peopleName", tPeople.column("name"));
        Table tDepartment = new Table("department").PK("ID");
        Entity department = new Entity("department", tDepartment);
        Attribute depName = department.attribute("name");
        // when
        Join join = tDepartment.join(tPeople, "DEP_ID_FK");
        Attribute peopleDepartment =
                people.join("department", tDepartment, join);
        // then
        assertThat(new Query().select(peopleName).select(peopleDepartment.apply(depName), "depName").print())
                .isEqualTo("SELECT p.name AS peopleName, d.name AS depName FROM people p INNER JOIN department d ON d.ID=p.DEP_ID_FK");
    }

    @Test
    void should_enforce_attribute_type_is_within_entity() {
        // given
        Table ta = new Table("a");
        Table tb = new Table("b");
        // when
        Entity a = new Entity("aa", ta);
        // then
        assertDoesNotThrow(() -> a.attribute("a", ta.column("a")));
        assertThrows(ScopeException.class, () -> a.attribute("a", tb.column("a")));
    }

    @Test
    void equals() throws ScopeException {
        // given
        Table ta = new Table("a");
        Entity a = new Entity("aa", ta);
        Table tb = new Table("b");
        Entity b = new Entity("bb", tb);
        // when
        Attribute test = a.attribute("test");
        // then
        assertThat(test)
                .isEqualTo(test)
                .isEqualTo(a.attribute("test"))
                .isEqualTo(a.attribute("test", ta.column("test")))
                .isNotEqualTo(a.attribute("bad"))
                .isNotEqualTo(a.attribute("bad", ta.column("test")))
                .isNotEqualTo(b.attribute("test"));
        assertThat(test.unwrapReference())
                .isEqualTo(ta.column("test"))
                .isNotEqualTo(ta.column("bad"))
                .isNotEqualTo(tb.column("test"));
    }
}