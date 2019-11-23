package org.kmsf.phenix.logical;

import org.kmsf.phenix.database.ScopeException;
import org.kmsf.phenix.database.Table;
import org.kmsf.phenix.function.FunctionType;

import static org.kmsf.phenix.function.Functions.*;

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

}