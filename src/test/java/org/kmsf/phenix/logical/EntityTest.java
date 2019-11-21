package org.kmsf.phenix.logical;

import org.junit.jupiter.api.Test;
import org.kmsf.phenix.database.*;
import org.kmsf.phenix.function.ConstFunction;
import org.kmsf.phenix.function.Function;
import org.kmsf.phenix.function.FunctionType;
import org.kmsf.phenix.function.Functions;

import static org.junit.jupiter.api.Assertions.*;

class EntityTest {

    @Test
    void getName() {
        Table tPeople = new Table("people");
        Entity people = new Entity("people", tPeople);
        assertEquals("people", people.getName().get());
    }

    @Test
    void attributeCreation() {
        Table tPeople = new Table("people");
        Entity people = new Entity("people", tPeople);
        Attribute name = people.attribute("name");
        assertEquals(new FunctionType(people), name.getSource());
        //
        Table tDepartment = new Table("department");
        Entity department = new Entity("department", tDepartment);
        Join join = new Join(tDepartment, Functions.EQUALS(tPeople.column("DEP_ID_FK"), tDepartment.column("ID")));
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
        Attribute peopleDepartment = people.join(department, "peopleDepartment", Functions.EQUALS(tPeople.column("DEP_ID_FK"), tDepartment.column("ID")));
        assertEquals(new FunctionType(tPeople, tDepartment), peopleDepartment.getSource());
    }

    @Test
    void testEntityFromSelect() throws ScopeException {
        Table tPeople = new Table("people");
        Column cFirstName = tPeople.column("first_name");
        Column cLastName = tPeople.column("last_name");
        Select select = new Select().from(tPeople).select(cFirstName).select(cLastName);
        Entity peopleName = new Entity("peopleName", select);
        assertEquals("SELECT a.* FROM (SELECT p.'first_name', p.'last_name' FROM 'people' p) a", new Query().select(peopleName).print());
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
        Join join = new Join(customer, Functions.EQUALS(tCustomer.column("ID"), tTransaction.column("CUST_ID_FK")));
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
}