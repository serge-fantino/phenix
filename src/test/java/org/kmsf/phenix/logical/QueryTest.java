package org.kmsf.phenix.logical;

import org.junit.jupiter.api.Test;
import org.kmsf.phenix.database.Column;
import org.kmsf.phenix.database.ScopeException;
import org.kmsf.phenix.database.Table;
import org.kmsf.phenix.algebra.FunctionType;

import java.util.Collections;

import static org.kmsf.phenix.algebra.Functions.*;

import static org.junit.jupiter.api.Assertions.*;

class QueryTest {

    @org.junit.jupiter.api.Test
    public void select() throws ScopeException {
        Table tPeople = new Table("people");
        Entity people = new Entity("people", tPeople);
        Attribute peopleName = people.attribute("peopleName",tPeople.column("name"));
        assertEquals("SELECT p.name AS peopleName FROM people p",
                new Query()
                        .select(peopleName).print());
        assertEquals("SELECT p.name AS peopleName, p.title FROM people p",
                new Query()
                        .select(peopleName)
                        .select(people.attribute("title",tPeople.column("title")))
                        .print());
        //
        Table tDepartment = new Table("department");
        Entity department = new Entity("department", tDepartment);
        Attribute peopleDepartment =
                people.join(tDepartment, "department",
                        EQUALS(tPeople.column("DEP_ID_FK"), tDepartment.column("ID")));
        assertEquals("SELECT d.* FROM people p INNER JOIN department d ON p.DEP_ID_FK=d.ID"
                , new Query()
                        .select(peopleDepartment)
                        .print());
        Attribute depName = department.attribute("name");
        assertEquals("SELECT p.name AS peopleName, d.* FROM people p INNER JOIN department d ON p.DEP_ID_FK=d.ID", new Query().select(peopleName).select(peopleDepartment).print());
    }


    @org.junit.jupiter.api.Test
    public void subSelect() throws ScopeException {
        Table tPeople = new Table("people");
        Entity people = new Entity("people", tPeople);
        Attribute peopleName = people.attribute("name", tPeople.column("name"));
        Table tDepartment = new Table("department");
        Entity department = new Entity("department", tDepartment);
        Attribute peopleDepartment =
                people.join(tDepartment, "department",
                        EQUALS(tPeople.column("DEP_ID_FK"), tDepartment.column("ID")));
        Attribute depName = department.attribute("name");
        assertEquals("SELECT a.name FROM (SELECT d.* FROM department d) a", new Query().from(new Query().select(department)).select(depName).print());
    }

    /**
     * testing how to join from a sub-select statement
     *
     * @throws ScopeException
     */
    @org.junit.jupiter.api.Test
    public void subSelectWithJoin() throws ScopeException {
        Table tPeople = new Table("people");
        Entity people = new Entity("people", tPeople);
        Attribute peopleName = people.attribute("name", tPeople.column("name"));
        Table tDepartment = new Table("department");
        Entity department = new Entity("department", tDepartment);
        Attribute departmentPeoples =
                department.join(tPeople, "peoples",
                        EQUALS(tPeople.column("DEP_ID_FK"), tDepartment.column("ID")));
        assertEquals(new FunctionType(tDepartment, tPeople), departmentPeoples.apply(peopleName).getSource());
        assertEquals("SELECT p.name FROM (SELECT d.* FROM department d) a INNER JOIN people p ON p.DEP_ID_FK=a.ID",
                new Query()
                        .from(new Query().select(department))
                        .select(departmentPeoples.apply(peopleName)).print());
    }

    @Test
    void selector() throws ScopeException {
        Table tPeople = new Table("people");
        Entity people = new Entity("people", tPeople);
        Attribute peopleName = people.attribute("peopleName", tPeople.column("name"));
        Query query = new Query()
                .select(peopleName);
        assertEquals("SELECT p.name AS peopleName FROM people p",
                query.print());
        assertDoesNotThrow(() -> query.selector("peopleName"));
        assertThrows(ScopeException.class, () -> query.selector("undefined"));
        assertEquals(peopleName, query.selector("peopleName"));
        assertEquals(Collections.singletonList(peopleName), query.getSelectors());
        assertNotEquals(Collections.singletonList(people.attribute("revenue")), query.getSelectors());
    }

    @Test
    void redux() {
        Table table = new Table("test");
        Column col = new Column(table, "a");
        Entity entity = new Entity(table);
        Attribute attr = new Attribute(entity, "a", col);
        Query query = new Query().select(attr);
        assertEquals(query, query);
        assertEquals(query, query.redux());
        assertTrue(query.redux() == query.redux().redux());
    }

}