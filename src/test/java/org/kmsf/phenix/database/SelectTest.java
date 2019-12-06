package org.kmsf.phenix.database;

import org.junit.jupiter.api.Test;
import org.kmsf.phenix.function.Function;
import org.kmsf.phenix.function.FunctionType;

import java.util.ArrayList;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.*;
import static org.kmsf.phenix.function.Functions.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

class SelectTest {

    @Test
    void should_throw_exception_when_select_nothing() {
        // given
        var select = new Select();
        // when
        Throwable thrown = catchThrowable(()->select.print());
        //
        assertThat(thrown).isInstanceOf(ScopeException.class);
    }

    @Test
    void should_select_all() throws ScopeException {
        // given
        Table table = new Table("table");
        // when
        Select select = new Select().from(table).select(STAR(table));
        // then
        assertEquals("SELECT t.* FROM table t", select.print());
    }

    @Test
    void should_select_all_by_default() throws ScopeException {
        // given
        Table table = new Table("table");
        // when
        Select select = new Select().from(table);
        // then
        assertEquals("SELECT t.* FROM table t", select.print());
    }

    @Test
    void should_select_all_by_default_with_join() throws ScopeException {
        // given
        Table parent = new Table("parent").PK("id");
        Table children = new Table("children");
        // when
        Select select = new Select().from(parent).from(parent.join(children.FK("parent_id")).opposite());
        // then
        assertThat(select.print()).isEqualTo("SELECT p.id, c.parent_id FROM parent p INNER JOIN children c ON p.id=c.parent_id");
    }

    @Test
    void should_select_columns_from_table() throws ScopeException {
        // given
        Table table = new Table("table");
        Column test = table.column("test");
        // when
        Select select = new Select().from(table).select(test);
        // then
        assertThat(select.print()).isEqualTo("SELECT t.test FROM table t");
        assertThat(select.select(table.column("second")).print()).isEqualTo("SELECT t.test, t.second FROM table t");
    }

    @Test
    void should_throw_exception_when_selecting_column_without_table() throws ScopeException {
        // given
        Table table = new Table("table");
        // when
        Column test = table.column("test");
        // then
        assertThatThrownBy(() -> new Select().select(test)).isInstanceOf(ScopeException.class);
    }

    @Test
    void should_select_columns_from_tables() throws ScopeException {
        // given
        Table first = new Table("first");
        Column a = first.column("a");
        Table second = new Table("second");
        Column b = second.column("a");
        // when
        Select select = new Select().from(first).select(a).from(second).select(b);
        // then
        assertThat(select.print()).isEqualTo("SELECT f.a, s.a AS a1 FROM first f, second s");
    }

    @Test
    void should_select_columns_from_inner_join() throws ScopeException {
        // given
        Table first = new Table("first").PK("ID");
        Column a = first.column("a");
        Table second = new Table("second").PK("ID");
        Column b = second.column("b");
        Join ab = first.join(second.FK("FIRST_ID_FK"));
        // when
        Select select = new Select().from(first).select(a).from(ab.opposite()).select(b);
        // then
        assertThat(select.print()).isEqualTo("SELECT f.a, s.b FROM first f INNER JOIN second s ON f.ID=s.FIRST_ID_FK");
    }

    @Test
    void should_select_columns_from_inner_join_as_view() throws ScopeException {
        // given
        Table first = new Table("first").PK("ID");
        Column a = first.column("a");
        Table second = new Table("second").PK("ID");
        Column b = second.column("b");
        Join ab = first.join(second.FK("FIRST_ID_FK"));
        // when
        Select select = new Select().from(first).select(a).from((View)ab.opposite()).select(b);
        // then
        assertThat(select.print()).isEqualTo("SELECT f.a, s.b FROM first f INNER JOIN second s ON f.ID=s.FIRST_ID_FK");
    }

    @Test
    void should_not_allow_inner_join_if_source_is_not_available() throws ScopeException {
        // given
        Table people = new Table("people").PK("ID");
        Column name = people.column("name");
        Table department = new Table("department").PK("ID");
        Column depName = department.column("name");
        Join departmentManager = people.join(department.FK("MANAGER_ID_FK"));
        // when
        Select select = new Select().from(people).select(name);
        Throwable thrown = catchThrowable(()->select.from(departmentManager));
        // then
        assertThat(thrown).isInstanceOf(ScopeException.class);
        assertDoesNotThrow(() ->select.from(departmentManager.opposite()));
        assertThat(select.select(depName, "isManaging").print())
                .isEqualTo("SELECT p.name, d.name AS isManaging FROM people p INNER JOIN department d ON p.ID=d.MANAGER_ID_FK");
    }

    @Test
    void should_select_column_from_the_last_scope() throws ScopeException {
        // given
        Table people = new Table("people").PK("ID");
        Column name = people.column("name");
        Table department = new Table("department").PK("ID");
        Column depName = department.column("name");
        Join peopleDepartment = department.join(people.FK("DEPARTMENT_ID_FK"));
        Join departmentManager = people.join(department.FK("MANAGER_ID_FK"));
        // when
        Select hierarchy =
                new Select()
                        .from(people).select(name)
                        .from(peopleDepartment).select(depName)
                        .from(departmentManager).select(name, "manager");
        // then
        assertThat(hierarchy.print())
                .isEqualTo("SELECT p.name, d.name AS name1, p1.name AS manager FROM people p INNER JOIN department d ON d.ID=p.DEPARTMENT_ID_FK INNER JOIN people p1 ON p1.ID=d.MANAGER_ID_FK");
    }

    @Test
    void should_handle_same_table_twice_in_scope() throws ScopeException {
        // given
        Table people = new Table("people").PK("ID");
        Table transaction = new Table("transaction").PK("ID");
        Join buyer = new Join(transaction, people, EQUALS(people.column("ID"),transaction.column("BUYER_ID_FK")));
        Join seller = new Join(transaction, people, EQUALS(people.column("ID"),transaction.column("SELLER_ID_FK")));
        Select select = new Select()
                .from(transaction).select(transaction.column("PRICE"));
        // when select buyer
        select.from(buyer);
        // then
        assertEquals("p", select.getScope().resolves(buyer).getAlias());
        select.select(people.column("name"),"buyer_name");
        assertEquals("p", select.getScope().resolves(people).getAlias());
        // when select seller
        select.from(seller);
        // then
        assertEquals("p1", select.getScope().resolves(people).getAlias());
        select.select(people.column("name"),"seller_name");
        assertEquals("p1", select.getScope().resolves(seller).getAlias());
        // and then
        assertEquals("SELECT t.PRICE, p.name AS buyer_name, p1.name AS seller_name FROM transaction t INNER JOIN people p ON p.ID=t.BUYER_ID_FK INNER JOIN people p1 ON p1.ID=t.SELLER_ID_FK"
                ,select.print());
    }

    @Test
    void should_select_from_sub_select() throws ScopeException {
        // given
        Table a = new Table("a");
        Table b = new Table("b");
        // when
        Select ab = new Select().from(a).from(b);
        // then
        assertThat(ab.print()).isEqualTo("SELECT a.*, b.* FROM a a, b b");
        assertThat(new Select().from(ab).select(a.column("test_a")).select(b.column("test_b")).print())
            .isEqualTo("SELECT a.test_a, a.test_b FROM (SELECT a.test_a, b.test_b FROM a a, b b) a");
        // ... check no side-effect
        assertThat(ab.print()).isEqualTo("SELECT a.*, b.* FROM a a, b b");
    }

    @Test
    void should_select_from_sub_select_with_ambiguous_namming() throws ScopeException {
        // given
        Table a = new Table("a");
        Table b = new Table("b");
        // when
        Select ab = new Select().from(a).from(b);
        Select subSelect = new Select().from(ab).select(a.column("test_x")).select(b.column("test_x"));
        // then
        assertThat(ab.print()).isEqualTo("SELECT a.*, b.* FROM a a, b b");
        assertThat(new Select().from(ab).select(a.column("test_x")).select(b.column("test_x")).print())
                .isEqualTo("SELECT a.test_x, a.test_x1 FROM (SELECT a.test_x, b.test_x AS test_x1 FROM a a, b b) a");
        // ... check no side-effect
        assertThat(ab.print()).isEqualTo("SELECT a.*, b.* FROM a a, b b");
    }

    @Test
    void should_return_type_based_on_from_clause() throws ScopeException {
        // given
        Table table = new Table("table");
        Table b = new Table("b");
        // when ?
        // then
        assertEquals(new FunctionType(table), new Select().from(table).select(table.column("a")).select(table.column("b")).getType());
        assertEquals(new FunctionType(table, b), new Select().from(table).from(b).getType());
    }

    @Test
    void should_return_the_pk_from_types() throws ScopeException {
        // given
        Table table = new Table("table").PK("ID");
        // when
        Select select = new Select(table).select(table.column("name")).select(table.column("ID"));
        // then
        assertThat(select.getPK().getView().inheritsFrom(table)).isTrue();
        assertThat(select.getPK().getKeys()).isEqualTo(table.getPK().getKeys());
    }

    @Test
    void should_return_the_table_pk_when_selecting_some_columns() throws ScopeException {
        // given
        Table table = new Table("table").PK("ID");
        // when
        Select select = new Select(table).select(table.column("name"));
        // then
        assertThat(select.getPK().getView().inheritsFrom(table)).isTrue();
        assertThat(select.getPK().getKeys()).isEqualTo(table.getPK().getKeys());
    }

    @Test
    void should_return_group_by_columns_as_pk() throws ScopeException {
        // given
        Table table = new Table("table").PK("ID");
        Table transaction = new Table("transaction").PK("transID");
        // when
        var select = new Select(table)
                .select(table.column("name"))
                .from(transaction.join(table.getPK()))
                .groupBy(table.getPK().getKeys()).groupBy(transaction.getPK().getKeys());
        // then
        var keys = new ArrayList<>();
        keys.addAll(table.getPK().getKeys());
        keys.addAll(transaction.getPK().getKeys());
        assertThat(select.getPK().getKeys()).isEqualTo(keys);
    }

    @Test
    void should_table_have_greater_precedence_then_select() throws ScopeException {
        Table table = new Table("table");
        assertTrue(table.getPrecedence()>new Select().from(table).getPrecedence());
    }

    @Test
    void should_implement_group_by() throws ScopeException {
        Table customer = new Table("customer");
        Table transaction = new Table("transaction");
        Select amountByCustomer = new Select()
                .from(customer).select(customer.column("name"))
                .join(transaction,
                        EQUALS(customer.column("ID"), transaction.column("CUST_ID_FK")))
                .select(SUM(transaction.column("amount")))
                .groupBy(customer.column("name"));
        assertEquals("SELECT c.name, SUM(t.amount) FROM customer c INNER JOIN transaction t ON c.ID=t.CUST_ID_FK GROUP BY c.name",
                amountByCustomer.print());
    }

    @Test
    void should_copy_be_equals_to_original() throws ScopeException {
        // given
        Table people = new Table("people");
        Select select = new Select().from(people).select(people.column("name"));
        // hen
        Select copy = new Select(select);
        // then
        assertThat(select).isEqualTo(copy);
        assertEquals(select.print(), copy.print());
        assertEquals("SELECT p.name, p.city FROM people p", copy.select(people.column("city")).print());
        assertEquals("SELECT p.name FROM people p", select.print());
    }

    @Test
    void should_display_where_clause() throws ScopeException {
        // given
        Table people = new Table("people").PK("peopleID");
        Column city = people.column("city");
        // when
        Select richPeople = new Select().from(people).where(GREATER(people.column("revenue"), CONST(1000)));
        Select richPeopleInBeverlyHill = new Select(richPeople).where(EQUALS(people.column("city"), CONST("Beverly Hill")));
        // then
        // ... where clause should not select column by side effect
        assertThat(richPeople.print())
                .isEqualTo("SELECT p.peopleID, p.city FROM people p WHERE p.revenue>1000");
        // ... override default selection if setting one
        assertThat(new Select(richPeople).select(people.column("revenue")).print())
                .isEqualTo("SELECT p.revenue FROM people p WHERE p.revenue>1000");
        assertThat(richPeopleInBeverlyHill.print())
                .isEqualTo(richPeople.print() + " AND p.city='Beverly Hill'");
    }

    @Test
    void should_display_having_clause() throws ScopeException {
        // given
        Table people = new Table("people").PK("peopleID");
        Column city = people.column("city");
        // when
        Select richPeople = new Select().from(people).where(GREATER(people.column("revenue"), CONST(1000)));
        Select richCity = new Select(richPeople).select(city).select(COUNT(people), "count").groupBy(city).having(GREATER(COUNT(people), CONST(1000)));
        // then
        assertEquals("SELECT p.city, COUNT(DISTINCT p.peopleID) AS count FROM people p WHERE p.revenue>1000 GROUP BY p.city HAVING COUNT(DISTINCT p.peopleID)>1000",
                richCity.print());
    }

    @Test
    void should_support_where_in_subselect() throws ScopeException {
        // given
        Table people = new Table("people").PK("peopleID");
        Column city = people.column("city");
        Column revenue = people.column("revenue");
        // when
        Select peopleInRichCity = new Select(people)
                .where(IN(city,
                        new Select().from(people).select(city).groupBy(city).having(GREATER(SUM(revenue),
                                MULTIPLY(CONST(3), new Select().from(people).select(SUM(revenue)))))));
        // then
        assertEquals("SELECT p.peopleID, p.city, p.revenue FROM people p WHERE p.city IN (SELECT p.city FROM people p GROUP BY p.city HAVING SUM(p.revenue)>3*(SELECT SUM(p.revenue) FROM people p))",
                peopleInRichCity.print());
    }

    @Test
    void should_select_result_by_alias_name() throws ScopeException {
        // given
        Table people = new Table("people").PK("peopleID");
        Column revenue = people.column("revenue");
        // when
        Function square = MULTIPLY(revenue, revenue);
        Select something = new Select(people).select(square, "squareRevenue");
        // then
        assertDoesNotThrow(() -> something.selector("squareRevenue"));
        assertThrows(ScopeException.class, () -> something.selector("nothingToShow"));
        assertEquals("SELECT p.revenue*p.revenue AS squareRevenue FROM people p", something.print());
    }

    @Test
    void should_include_type_if_required() throws ScopeException {
        // given
        Table people = new Table("people").PK("peopleID");
        Column revenue = people.column("revenue");
        // when
        Select select = new Select();
        select.addToScopeIfNeeded(revenue);
        // then
        assertThat(select.getType()).isEqualTo(new FunctionType(people));
    }

    @Test
    void should_selector_equals_to_definition() throws ScopeException {
        // given
        Table people = new Table("people").PK("peopleID");
        Column revenue = people.column("revenue");
        // when
        Function square = MULTIPLY(revenue, revenue);
        Function twice = MULTIPLY(revenue, CONST(2));
        Select something = new Select(people).select(square, "squareRevenue");
        Selector squareRevenue = something.selector("squareRevenue");
        // then
        assertEquals(square, squareRevenue);
        assertNotEquals(twice, squareRevenue);
        assertEquals(Arrays.asList(new Selector[]{squareRevenue})
                , something.getSelectors());
    }

}