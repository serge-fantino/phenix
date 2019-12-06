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
        // when
        Join join = tDepartment.join(tPeople.FK("DEP_ID_FK"));
        Attribute peopleDepartment =
                people.join(tDepartment, "department", join);
        // then
        assertThat(new Select().from(people).addToScopeIfNeeded(join)).containsExactly(join);
        assertThat(new Query(people).select(peopleDepartment).print().toString())
                .isEqualTo("SELECT p.ID, p.name, p.DEP_ID_FK, d.ID FROM people p INNER JOIN department d ON d.ID=p.DEP_ID_FK");
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
        Join join = tDepartment.join(tPeople.FK("DEP_ID_FK"));
        Attribute peopleDepartment =
                people.join(tDepartment, "department", join);
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
        Table ta = new Table("a");
        Entity a = new Entity("aa", ta);
        Table tb = new Table("b");
        Entity b = new Entity("bb", tb);
        assertEquals(a.attribute("a"), a.attribute("a"));
        assertEquals(a.attribute("a"), a.attribute("a", ta.column("a")));
        assertNotEquals(a.attribute("a"), a.attribute("aaa", ta.column("a")));
        assertEquals(a.attribute("a"), ta.column("a"));
        assertNotEquals(a.attribute("a"), a.attribute("b"));
        assertNotEquals(a.attribute("a"), a.attribute("a", ta.column("b")));
        assertNotEquals(a.attribute("a"), b.attribute("a"));
        assertNotEquals(a.attribute("a"), tb.column("a"));
        assertNotEquals(a.attribute("a"), ta.column("b"));
    }
}