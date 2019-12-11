package org.kmsf.phenix.logical;

import org.junit.jupiter.api.Test;
import org.kmsf.phenix.database.*;
import org.kmsf.phenix.function.FunctionType;

import java.util.Arrays;

import static org.kmsf.phenix.function.Functions.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.*;

class EntityTest {

    @Test
    void should_entity_as_its_own_name() {
        // given
        Table tPeople = new Table("tPeople");
        // when
        Entity people = new Entity("people", tPeople);
        // then
        assertThat(people.getName()).contains("people");
    }

    @Test
    void should_inherits_primary_key_definition() throws ScopeException {
        // given
        Table tPeople = new Table("people").PK("ID");
        // when
        Entity people = new Entity(tPeople);
        // then
        assertThat(people.getPK()).isEqualTo(tPeople.getPK());
    }

    @Test
    void should_inherited_view_be_independent_from_modification() throws ScopeException {
        // given
        Table tPeople = new Table("people");
        // when
        Entity people = new Entity(tPeople);
        tPeople.PK("ID");
        // then
        assertThat(people.getPK()).isNotEqualTo(tPeople.getPK());
    }

    @Test
    void should_attribute_type_equals_to_definition_type() throws ScopeException {
        // given
        Table tPeople = new Table("people");
        // when
        Entity people = new Entity("people", tPeople);
        Attribute name = people.attribute("name");
        // then
        assertThat(name.getType()).isEqualTo(people.getType());
    }

    /**
     * creating an Entity based on a Query
     *
     * @throws ScopeException
     */
    @Test
    void should_allow_using_select_to_define_entity() throws ScopeException {
        // given
        Table tPeople = new Table("people");
        Column cFirstName = tPeople.column("first_name");
        Column cLastName = tPeople.column("last_name");
        // when
        Select select = new Select().from(tPeople).select(cFirstName).select(cLastName);
        Entity peopleName = new Entity("peopleName", select);
        // then
        assertThat(new Query().select(peopleName).print())
                .isEqualTo("SELECT a.first_name, a.last_name FROM (SELECT p.first_name, p.last_name FROM people p) a");
    }

    @Test
    void should_join_type_equals_to_definition_type() throws ScopeException {
        // given
        Table tPeople = new Table("people");
        Entity people = new Entity("people", tPeople);
        Attribute name = people.attribute("name");
        // when
        Table tDepartment = new Table("department");
        Entity department = new Entity("department", tDepartment);
        Join join = new Join(tDepartment, EQUALS(tPeople.column("DEP_ID_FK"), tDepartment.column("ID")));
        Attribute aDepartment =
                people.attribute("department", join);
        // then
        assertThat(aDepartment.getType().getValues()).containsExactly(tPeople, join);
    }

    @Test
    void should_allow_join_creation() throws ScopeException {
        // given
        Table tPeople = new Table("people").PK("ID");
        Entity people = new Entity("people", tPeople);
        Attribute peopleName = people.attribute("name");
        Table tDepartment = new Table("department").PK("ID");
        Entity department = new Entity("department", tDepartment);
        Attribute depName = department.attribute("depName", "name");
        // when
        Join join = tDepartment.join(tPeople.FK("DEP_ID_FK"));
        Attribute peopleDepartment = people.join("peopleDepartment", department, join);
        // then
        assertThat(new Query().select(peopleName).select(peopleDepartment).select(depName).print())
                .isEqualTo("SELECT p.name, d.name AS depName FROM people p INNER JOIN department d ON d.ID=p.DEP_ID_FK");
    }

    /**
     * creating an Entity based on a complex Query
     *
     * @throws ScopeException
     */
    @Test
    void should_create_entity_based_on_query() throws ScopeException {
        // given
        Table tPeople = new Table("people");
        Entity people = new Entity("people", tPeople);
        Table tDepartment = new Table("department");
        Entity department = new Entity("department", tDepartment);
        Column departmentPK = tDepartment.column("ID");
        Attribute peoples = department.join("peoples", people, EQUALS(tPeople.column("DEPT_ID_FK"), departmentPK));
        // when
        Query departmentCount =
                new Query(department).select(COUNT(peoples.apply(people.attribute("ID")))).groupBy(departmentPK);
        Entity headCount = new Entity("headCount", departmentCount);
        // then
        assertThat(headCount.inheritsFrom(departmentCount)).isTrue();
        assertThat(departmentCount.print())
                .isEqualTo("SELECT COUNT(DISTINCT p.ID) AS x, d.ID FROM department d INNER JOIN people p ON p.DEPT_ID_FK=d.ID GROUP BY d.ID");
        assertThat(new Select(headCount).inheritsFrom(headCount)).isTrue();
        assertThat(new Query(headCount).isCompatibleWith(headCount)).isTrue();
        assertThat(new Query().select(headCount).print())
                .isEqualTo("SELECT a.x, a.ID FROM (SELECT COUNT(DISTINCT p.ID) AS x, d.ID FROM department d INNER JOIN people p ON p.DEPT_ID_FK=d.ID GROUP BY d.ID) a");
        // when
        Attribute peopleDepartment = people.join("peopleDepartment", department, EQUALS(tPeople.column("DEPT_ID_FK"), departmentPK));
        // then
        assertEquals("SELECT COUNT(DISTINCT p.ID) AS x, d.name FROM people p INNER JOIN department d ON p.DEPT_ID_FK=d.ID GROUP BY d.name",
                new Query(people).select(COUNT(people.attribute("ID"))).groupBy(peopleDepartment.apply(department.attribute("name"))).print());
    }

    @Test
    void should_entity_type_be_the_view_definition() {
        // given
        Table tPeople = new Table("people");
        // when
        Entity people = new Entity("people", tPeople);
        // then
        assertThat(people.getType().getValues()).containsExactly(tPeople);
    }

    @Test
    void should_entity_compatible_with_table() {
        // given
        Table tPeople = new Table("people");
        // when
        Entity people = new Entity("people", tPeople);
        // then
        assertThat(people.isCompatibleWith(tPeople)).isTrue();
        assertThat(tPeople.isCompatibleWith(people)).isTrue();
    }

    @Test
    void should_entity_compatible_with_join() throws ScopeException {
        // given
        Table tCustomer = new Table("customer");
        Entity customer = new Entity("customer", tCustomer);
        Table tTransaction = new Table("transaction");
        Entity transaction = new Entity("transaction", tTransaction);
        // when
        Join join = new Join(customer, EQUALS(tCustomer.column("ID"), tTransaction.column("CUST_ID_FK")));
        // then
        assertThat(tCustomer.isCompatibleWith(customer)).isTrue();
        assertThat(customer.isCompatibleWith(tCustomer)).isTrue();
        assertThat(customer.isCompatibleWith(join)).isTrue();
        assertThat(join.isCompatibleWith(customer)).isTrue();
        assertThat(tCustomer.isCompatibleWith(join)).isTrue();
        assertThat(join.isCompatibleWith(tCustomer)).isTrue();
        // and then
        assertThat(tCustomer.isCompatibleWith(tTransaction)).isFalse();
        assertThat(customer.isCompatibleWith(transaction)).isFalse();
        assertThat(customer.isCompatibleWith(tTransaction)).isFalse();
        assertThat(tTransaction.isCompatibleWith(customer)).isFalse();
        assertThat(transaction.isCompatibleWith(join)).isFalse();
        assertThat(tTransaction.isCompatibleWith(join)).isFalse();
        assertThat(join.isCompatibleWith(transaction)).isFalse();
        assertThat(join.isCompatibleWith(tTransaction)).isFalse();
    }

    /**
     * test creating a segment entity, that is a entity based on a sub-selection
     */
    @Test
    void should_entity_support_where_clause() throws ScopeException {
        // given
        Table tPeople = new Table("people").PK("ID");
        Entity people = new Entity("people", tPeople);
        Attribute revenue = people.attribute("revenue");
        Attribute city = people.attribute("city");
        Query query = new Query(people).where(GREATER(revenue, CONST(1000)));
        assertEquals("SELECT p.revenue, p.city FROM people p WHERE p.revenue>1000",
                query.print());
        // when
        Entity richPeople = new Entity(query);
        // then
        assertEquals("SELECT a.* FROM (SELECT p.revenue, p.city FROM people p WHERE p.revenue>1000) a",
                new Query(richPeople).print());
        assertEquals("SELECT a.city, COUNT(DISTINCT a.ID) AS x FROM (SELECT p.city, p.ID FROM people p WHERE p.revenue>1000) a",
                new Query(richPeople).select(city).select(COUNT(people)).print());
        // check no side-effect
        assertEquals("SELECT p.revenue, p.city FROM people p WHERE p.revenue>1000", query.print());
    }

    @Test
    void should_entity_selectors_returns_attributes() throws ScopeException {
        // given
        Table tPeople = new Table("people").PK("ID");
        Entity people = new Entity("people", tPeople);
        // when
        Attribute revenue = people.attribute("revenue");
        Attribute city = people.attribute("city");
        // then
        assertThat(people.getSelectors()).containsExactly(revenue, city);
        assertTrue(revenue == people.attribute("revenue"));
        assertTrue(revenue == people.selector("revenue").get());
    }

    @Test
    public void should_select_entity_attribute_by_default() throws ScopeException {
        // given
        Table tPeople = new Table("people").PK("ID");
        Table tDepartment = new Table("department").PK("ID");
        // when
        Entity people = new Entity(tPeople);
        people.attribute("firstName");
        people.attribute("lastName");
        Entity department = new Entity(tDepartment);
        department.attribute("city");
        Attribute peopleDepartment = people.join(department, "department_id_fk");
        // then
        assertThat(new Query().select(people).select(peopleDepartment).print())
                .isEqualTo("SELECT p.firstName, p.lastName, d.city FROM people p INNER JOIN department d ON d.ID=p.department_id_fk");
    }

}