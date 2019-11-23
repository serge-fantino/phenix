package org.kmsf.phenix.logical;

import org.junit.jupiter.api.Test;
import org.kmsf.phenix.database.*;
import org.kmsf.phenix.function.ConstFunction;
import org.kmsf.phenix.function.Function;
import org.kmsf.phenix.function.FunctionType;

import static org.kmsf.phenix.function.Functions.*;

import static org.junit.jupiter.api.Assertions.*;

class EntityTest {

    @Test
    void getName() {
        Table tPeople = new Table("people");
        Entity people = new Entity("people", tPeople);
        assertEquals("people", people.getName().get());
    }

    @Test
    void primaryKey() throws ScopeException {
        Table tPeople = new Table("people").PK("ID");
        Entity people = new Entity(tPeople);
        assertEquals(tPeople.getPK(), people.getPK());
    }

    @Test
    void attributeCreation() throws ScopeException {
        Table tPeople = new Table("people");
        Entity people = new Entity("people", tPeople);
        Attribute name = people.attribute("name");
        assertEquals(new FunctionType(people), name.getSource());
        //
        Table tDepartment = new Table("department");
        Entity department = new Entity("department", tDepartment);
        Join join = new Join(tDepartment, EQUALS(tPeople.column("DEP_ID_FK"), tDepartment.column("ID")));
        Attribute aDepartment =
                people.attribute("department", join);
        assertEquals(new FunctionType(tPeople,join), aDepartment.getSource());
    }

    @Test
    void joinCreation() {
        Table tPeople = new Table("people");
        Entity people = new Entity("people", tPeople);
        //
        Table tDepartment = new Table("department");
        Entity department = new Entity("department", tDepartment);
        //
        Attribute peopleDepartment = people.join(department, "peopleDepartment", EQUALS(tPeople.column("DEP_ID_FK"), tDepartment.column("ID")));
        assertEquals(new FunctionType(tPeople, tDepartment), peopleDepartment.getSource());
    }

    /**
     * creating an Entity based on a Query
     *
     * @throws ScopeException
     */
    @Test
    void testEntityFromSelect() throws ScopeException {
        Table tPeople = new Table("people");
        Column cFirstName = tPeople.column("first_name");
        Column cLastName = tPeople.column("last_name");
        Select select = new Select().from(tPeople).select(cFirstName).select(cLastName);
        Entity peopleName = new Entity("peopleName", select);
        assertEquals("SELECT a.* FROM (SELECT p.first_name, p.last_name FROM people p) a", new Query().select(peopleName).print());
    }

    /**
     * creating an Entity based on a complexe Query
     *
     * @throws ScopeException
     */
    @Test
    void testEntityFromComplexSelect() throws ScopeException {
        Table tPeople = new Table("people");
        Entity people = new Entity("people", tPeople);
        Table tDepartment = new Table("department");
        Entity department = new Entity("department", tDepartment);
        Column departmentPK = tDepartment.column("ID");
        Attribute peoples = department.join(tPeople, "peoples", EQUALS(tPeople.column("DEPT_ID_FK"), departmentPK));
        Query departmentCount = new Query(department).select(COUNT(peoples.apply(people.attribute("ID")))).groupBy(departmentPK);
        Entity headCount = new Entity("headCount", departmentCount);
        assertEquals("SELECT a.* FROM (SELECT COUNT(DISTINCT p.ID), d.ID FROM department d INNER JOIN people p ON p.DEPT_ID_FK=d.ID GROUP BY d.ID) a",
                new Query().select(headCount).print());
        Attribute peopleDepartment = people.join(department, "peopleDepartment", EQUALS(tPeople.column("DEPT_ID_FK"), departmentPK));
        assertEquals("SELECT COUNT(DISTINCT p.ID), d.name FROM people p INNER JOIN department d ON p.DEPT_ID_FK=d.ID GROUP BY d.name",
                new Query(people).select(COUNT(people.attribute("ID"))).groupBy(peopleDepartment.apply(department.attribute("name"))).print());
    }

    @Test
    void print() {
    }

    @Test
    void getSource() {
        Table tPeople = new Table("people");
        Entity people = new Entity("people", tPeople);
        assertEquals(new FunctionType(tPeople), people.getSource());
    }

    @Test
    void testEquals() {
        Table tPeople = new Table("people");
        Entity people = new Entity("people", tPeople);
        assertEquals(people, tPeople);
        assertEquals(tPeople, people);
    }

    @Test
    void checkTableEntityJoinEquals() {
        Table tCustomer = new Table("customer");
        Entity customer = new Entity("customer", tCustomer);
        Table tTransaction = new Table("transaction");
        Entity transaction = new Entity("transaction", tTransaction);
        Join join = new Join(customer, EQUALS(tCustomer.column("ID"), tTransaction.column("CUST_ID_FK")));
        assertEquals(tCustomer, customer);
        assertEquals(customer, tCustomer);
        assertEquals(customer, join);
        assertEquals(join, customer);
        assertEquals(tCustomer, join);
        assertEquals(join, tCustomer);
        assertNotEquals(tCustomer, tTransaction);
        assertNotEquals(customer, transaction);
        assertNotEquals(customer, tTransaction);
        assertNotEquals(tTransaction, customer);
        assertNotEquals(transaction, join);
        assertNotEquals(tTransaction, join);
        assertNotEquals(join, transaction);
        assertNotEquals(join, tTransaction);
    }

    /**
     * test creating a segment entity, that is a entity based on a sub-selection
     */
    @Test
    void createSegmentEntity() throws ScopeException {
        Table tPeople = new Table("people").PK("ID");
        Entity people = new Entity("people", tPeople);
        Attribute revenue = people.attribute("revenue");
        Attribute city = people.attribute("city");
        Query query = new Query(people).where(GREATER(revenue, CONST(1000)));
        assertEquals("SELECT p.* FROM people p WHERE p.revenue>1000", query.print());
        Entity richPeople = new Entity(query);
        assertEquals("SELECT a.city, COUNT(DISTINCT a.ID) FROM (SELECT p.* FROM people p WHERE p.revenue>1000) a",
                new Query(richPeople).select(city).select(COUNT(people)).print());
        // check no side-effect
        assertEquals("SELECT p.* FROM people p WHERE p.revenue>1000", query.print());
    }
}