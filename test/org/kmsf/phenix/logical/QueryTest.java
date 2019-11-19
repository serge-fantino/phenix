package org.kmsf.phenix.logical;

import org.kmsf.phenix.database.Join;
import org.kmsf.phenix.database.ScopeException;
import org.kmsf.phenix.database.Select;
import org.kmsf.phenix.database.Table;
import org.kmsf.phenix.database.sql.PrintResult;
import org.kmsf.phenix.database.sql.Scope;
import org.kmsf.phenix.function.Functions;

import static org.junit.jupiter.api.Assertions.*;

class QueryTest {

    @org.junit.jupiter.api.Test
    public void select() throws ScopeException {
        Table tPeople = new Table("people");
        Entity people = new Entity("people", tPeople);
        Attribute peopleName = people.attribute("name",tPeople.column("name"));
        assertEquals("SELECT p.'name' FROM 'people' p",
                new Query()
                        .select(peopleName).print());
        assertEquals("SELECT p.'name', p.'title' FROM 'people' p",
                new Query()
                        .select(peopleName)
                        .select(people.attribute("title",tPeople.column("title")))
                        .print());
        //
        Table tDepartment = new Table("department");
        Entity department = new Entity("department", tDepartment);
        Attribute peopleDepartment =
                people.attribute("department",
                        new Join(tDepartment, Functions.EQUALS(tPeople.column("DEP_ID_FK"), tDepartment.column("ID"))));
        assertEquals("SELECT d.* FROM 'people' p INNER JOIN 'department' d ON p.'DEP_ID_FK'=d.'ID'", new Query().select(peopleDepartment).print());
        Attribute depName = department.attribute("name");
        assertEquals("SELECT p.'name', d.* FROM 'people' p INNER JOIN 'department' d ON p.'DEP_ID_FK'=d.'ID'", new Query().select(peopleName).select(peopleDepartment).print());
    }


    @org.junit.jupiter.api.Test
    public void subSelect() throws ScopeException {
        Table tPeople = new Table("people");
        Entity people = new Entity("people", tPeople);
        Attribute peopleName = people.attribute("name", tPeople.column("name"));
        Table tDepartment = new Table("department");
        Entity department = new Entity("department", tDepartment);
        Attribute peopleDepartment =
                people.attribute("department",
                        new Join(tDepartment, Functions.EQUALS(tPeople.column("DEP_ID_FK"), tDepartment.column("ID"))));
        Attribute depName = department.attribute("name");
        assertEquals("SELECT a.'name' FROM (SELECT d.* FROM 'department' d) a", new Query().from(new Query().select(department)).select(depName).print());
    }

    @org.junit.jupiter.api.Test
    public void subSelect2() throws ScopeException {
        Table tPeople = new Table("people");
        Entity people = new Entity("people", tPeople);
        Attribute peopleName = people.attribute("name", tPeople.column("name"));
        Table tDepartment = new Table("department");
        Entity department = new Entity("department", tDepartment);
        Attribute peopleDepartment =
                people.attribute("department",
                        new Join(tDepartment, Functions.EQUALS(tPeople.column("DEP_ID_FK"), tDepartment.column("ID"))));
        assertEquals("SELECT p.'name' FROM (SELECT d.* FROM 'department' d) a ,'people' p",
                new Query()
                .from(new Query().select(department))
                        .select(peopleName).print());
    }

}