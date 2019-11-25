package org.kmsf.phenix.database;

import org.junit.jupiter.api.Test;
import org.kmsf.phenix.function.Function;
import org.kmsf.phenix.function.FunctionType;
import org.kmsf.phenix.sql.Scope;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.kmsf.phenix.function.Functions.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

class SelectTest {

    @org.junit.jupiter.api.Test
    void select() throws ScopeException {
        Table table = new Table("table");
        Select select = new Select().from(table).select(STAR(table));
        assertEquals("SELECT t.* FROM table t", select.print());
    }

    @Test
    void from() throws ScopeException {
        Table table = new Table("table");
        assertThrows(ScopeException.class, () -> {new Select().print();});
        assertEquals("SELECT t.something FROM table t", new Select().from(table).select(table.column("something")).print());
        assertThrows(ScopeException.class, () -> {new Select().select(table.column("something")).print();});
        assertEquals("SELECT t.a, t.b FROM table t", new Select().from(table).select(table.column("a")).select(table.column("b")).print());
        Table customer = new Table("customer");
        Table account = new Table("account");
        assertEquals("SELECT c.name, a.balance FROM customer c INNER JOIN account a ON c.ID=a.CUST_ID",
                new Select().from(customer).select(customer.column("name"))
                        .innerJoin(account, EQUALS(customer.column("ID"), account.column("CUST_ID")))
                        .select(account.column("balance")).print());
        Table people = new Table("people");
        Table department = new Table("department");
        assertEquals("SELECT p.name AS manager, d.name AS deptName, p1.name AS staff_name FROM people p INNER JOIN department d ON p.ID=d.manager_ID INNER JOIN people p1 ON p1.department_ID=d.ID",
                new Select().from(people).select(people.column("name"),"manager")
                        .innerJoin(department, EQUALS(people.column("ID"), department.column("manager_ID"))).select(department.column("name"), "deptName")
                        .innerJoin(people, EQUALS(people.column("department_ID"), department.column("ID"))).select(people.column("name"), "staff_name")
                        .print());
        assertEquals("SELECT p.name, d.name AS deptName, p1.name AS name1 FROM people p INNER JOIN department d ON p.ID=d.manager_ID INNER JOIN people p1 ON p1.department_ID=d.ID",
                new Select().from(people).select(people.column("name"))
                        .innerJoin(department, EQUALS(people.column("ID"), department.column("manager_ID"))).select(department.column("name"), "deptName")
                        .innerJoin(people, EQUALS(people.column("department_ID"), department.column("ID"))).select(people.column("name"))
                        .print());
        // inner
        assertEquals("SELECT c.name, a.balance FROM customer c INNER JOIN account a ON c.ID=a.CUST_ID",
                new Select()
                        .from(customer)
                        .select(customer.column("name"))
                        .from(new Join(account, EQUALS(customer.column("ID"), account.column("CUST_ID"))))
                        .select(account.column("balance"))
                        .print());
    }

    @Test
    void getSource() throws ScopeException {
        Table table = new Table("table");
        assertEquals(new FunctionType(table), new Select().from(table).select(table.column("a")).select(table.column("b")).getSource());
    }

    @Test
    void primaryKey() throws ScopeException {
        Table table = new Table("table").PK("ID");
        Select select = new Select(table).select(table.column("name")).select(table.column("ID"));
        assertEquals(table.getPK(), select.getPK());
        // still returning the table PK ???
        select = new Select(table).select(table.column("name"));
        assertEquals(table.getPK(), select.getPK());
        // with join
        Table transaction = new Table("transaction").PK("transID");
        select = new Select(table)
                .select(table.column("name"))
                .innerJoin(transaction, EQUALS(table.getPK(), transaction.getPK()))
                .groupBy(table.getPK()).groupBy(transaction.getPK());
        List<Function> keys = new ArrayList<>(table.getPK());
        keys.addAll(transaction.getPK());
        assertEquals(keys, select.getPK());
    }

    @Test
    void innerJoin() throws ScopeException {
        Table customer = new Table("customer");
        Table account = new Table("account");
        assertThrows(ScopeException.class, () -> {
            new Select().innerJoin(account, EQUALS(customer.column("ID"), account.column("CUST_ID"))).print();
        });
    }

    @Test
    void precedence() {
        Table table = new Table("table");
        assertTrue(table.getPrecedence()>new Select().from(table).getPrecedence());
    }

    @Test
    void groupBy() throws ScopeException {
        Table customer = new Table("customer");
        Table transaction = new Table("transaction");
        Select amountByCustomer = new Select()
                .from(customer).select(customer.column("name"))
                .innerJoin(transaction,
                        EQUALS(customer.column("ID"), customer.column("CUST_ID_FK")))
                .select(SUM(transaction.column("amount")))
                .groupBy(customer.column("name"));
        assertEquals("SELECT c.name, SUM(t.amount) FROM customer c INNER JOIN transaction t ON c.ID=c.CUST_ID_FK GROUP BY c.name",
                amountByCustomer.print());
    }

    @Test
    void copyConstructor() throws ScopeException {
        Table people = new Table("people");
        Select select = new Select().from(people).select(people.column("name"));
        Select copy = new Select(select);
        assertEquals(select.print(), copy.print());
        assertEquals("SELECT p.name, p.city FROM people p", copy.select(people.column("city")).print());
        assertEquals("SELECT p.name FROM people p", select.print());
    }

    @Test
    void whereClause() throws ScopeException {
        Table people = new Table("people").PK("peopleID");
        Column city = people.column("city");
        Select richPeople = new Select().from(people).where(GREATER(people.column("revenue"), CONST(1000)));
        assertEquals("SELECT p.peopleID, p.city, p.revenue FROM people p WHERE p.revenue>1000", richPeople.print());
        Select richPeopleInBeverlyHill = new Select(richPeople).where(EQUALS(people.column("city"), CONST("Beverly Hill")));
        assertEquals(richPeople.print() + " AND p.city='Beverly Hill'", richPeopleInBeverlyHill.print());
    }

    @Test
    void havingClause() throws ScopeException {
        Table people = new Table("people").PK("peopleID");
        Column city = people.column("city");
        Select richPeople = new Select().from(people).where(GREATER(people.column("revenue"), CONST(1000)));
        Select richCity = new Select(richPeople).select(city).select(COUNT(people), "count").groupBy(city).having(GREATER(COUNT(people), CONST(1000)));
        assertEquals("SELECT p.city, COUNT(DISTINCT p.peopleID) AS count FROM people p WHERE p.revenue>1000 GROUP BY p.city HAVING COUNT(DISTINCT p.peopleID)>1000",
                richCity.print());
    }

    @Test
    void whereInClause() throws ScopeException {
        Table people = new Table("people").PK("peopleID");
        Column city = people.column("city");
        Column revenue = people.column("revenue");
        Select peopleInRichCity = new Select(people)
                .where(IN(city,
                        new Select().from(people).select(city).groupBy(city).having(GREATER(SUM(revenue),
                                MULTIPLY(CONST(3), new Select().from(people).select(SUM(revenue)))))));
        assertEquals("SELECT p.peopleID, p.city, p.revenue FROM people p WHERE p.city IN (SELECT p.city FROM people p GROUP BY p.city HAVING SUM(p.revenue)>3*(SELECT SUM(p.revenue) FROM people p))",
                peopleInRichCity.print());
    }

    @Test
    void testSelectors() throws ScopeException {
        Table people = new Table("people").PK("peopleID");
        Column city = people.column("city");
        Column revenue = people.column("revenue");
        Function square = MULTIPLY(revenue, revenue);
        Function twice = MULTIPLY(revenue, CONST(2));
        Select something = new Select(people).select(square, "squareRevenue");
        assertDoesNotThrow(() -> something.selector("squareRevenue"));
        assertThrows(ScopeException.class, () -> something.selector("nothingToShow"));
        Selector squareRevenue = something.selector("squareRevenue");
        assertEquals(square, squareRevenue);
        assertNotEquals(twice, squareRevenue);
        assertEquals("SELECT p.revenue*p.revenue AS squareRevenue FROM people p", something.print());
        assertEquals(Arrays.asList(new Selector[]{squareRevenue})
                , something.getSelectors());
    }

    @Test
    void redux() {
        Table test = new Table("test");
        Column a = new Column(test, "a");
        Select select = new Select().from(test).select(a);
        assertEquals(select, select.redux());
        assertTrue(select == select.redux());
        assertTrue(select.redux() == select.redux().redux());
    }

}