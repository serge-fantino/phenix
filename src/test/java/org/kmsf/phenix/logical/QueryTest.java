package org.kmsf.phenix.logical;

import org.junit.jupiter.api.Test;
import org.kmsf.phenix.database.*;
import org.kmsf.phenix.function.FunctionType;

import java.util.Collections;

import static org.kmsf.phenix.function.Functions.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.*;

class QueryTest {

    @org.junit.jupiter.api.Test
    public void should_select_attribute_from_table() throws ScopeException {
        // given
        Table tPeople = new Table("people");
        Entity people = new Entity("people", tPeople);
        // when
        Attribute peopleName = people.attribute("peopleName", tPeople.column("name"));
        // then
        assertEquals("SELECT p.name AS peopleName FROM people p",
                new Query()
                        .select(peopleName).print());
        assertEquals("SELECT p.name AS peopleName, p.title FROM people p",
                new Query()
                        .select(peopleName)
                        .select(people.attribute("title", tPeople.column("title")))
                        .print());
    }

    @Test
    public void should_select_attribute_as_join() throws ScopeException {
        // given
        Table tPeople = new Table("people");
        Entity people = new Entity("people", tPeople);
        Attribute peopleName = people.attribute("peopleName", tPeople.column("name"));
        Table tDepartment = new Table("department");
        Entity department = new Entity("department", tDepartment);
        // when
        Attribute peopleDepartment =
                people.join(department, "department",
                        EQUALS(tPeople.column("DEP_ID_FK"), tDepartment.column("ID")));
        Attribute depName = department.attribute("depName", "name");
        // then
        // ... using where to force the selector to be empty...
        assertThat(new Query().from(people).where(EQUALS(peopleDepartment.apply(depName), CONST("R&D"))).print())
                .isEqualTo("SELECT p.name AS peopleName, d.name AS depName FROM people p INNER JOIN department d ON p.DEP_ID_FK=d.ID WHERE d.name='R&D'");
        assertThat(new Query().select(peopleDepartment).print())
                .isEqualTo("SELECT p.name, p.DEP_ID_FK, d.name AS depName FROM people p INNER JOIN department d ON p.DEP_ID_FK=d.ID");
        assertThat(new Query().select(peopleName).select(peopleDepartment).print())
                .isEqualTo("SELECT p.name AS peopleName FROM people p INNER JOIN department d ON p.DEP_ID_FK=d.ID");
    }


    @org.junit.jupiter.api.Test
    public void should_handle_sub_qeury() throws ScopeException {
        // given
        Table tPeople = new Table("people").PK("ID");
        Entity people = new Entity("people", tPeople);
        Attribute peopleName = people.attribute("peopleName", tPeople.column("name"));
        Table tDepartment = new Table("department");
        Entity department = new Entity("department", tDepartment);
        Attribute peopleDepartment =
                people.join(tDepartment, "department",
                        EQUALS(tPeople.column("DEP_ID_FK"), tDepartment.column("ID")));
        Attribute departmentManager = department.attribute("manager", tPeople.join(tDepartment.FK("MANAGER_ID_FK")));
        Attribute depName = department.attribute("deptName", tDepartment.column("name"));
        // when
        Query johnDoeDepartment = new Query().from(department).where(EQUALS(departmentManager.apply(peopleName), CONST("JohnDoe")));
        // then
        assertThat(johnDoeDepartment.print())
                .isEqualTo("SELECT d.name AS deptName, p.ID, p.name, p.DEP_ID_FK FROM department d INNER JOIN people p ON p.ID=d.MANAGER_ID_FK WHERE p.name='JohnDoe'");
        assertThat(new Query().from(johnDoeDepartment).select(depName).select(peopleName).print())
                .isEqualTo("SELECT a.name FROM (SELECT d.* FROM department d) a");
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
        assertEquals(new FunctionType(tDepartment, tPeople), departmentPeoples.apply(peopleName).getType());
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
    void duplicatedEntity() throws ScopeException {
        Table tpeople = new Table("people").PK("ID");
        Entity people = new Entity(tpeople);
        Table ttransaction = new Table("transaction").PK("ID");
        Entity transaction = new Entity(ttransaction);
        Attribute buyer = transaction.join(people, "buyer", EQUALS(tpeople.column("ID"), ttransaction.column("BUYER_ID_FK")));
        Attribute seller = transaction.join(people, "seller", EQUALS(tpeople.column("ID"), ttransaction.column("SELLER_ID_FK")));
        Table taddress = new Table("address").PK("ID");
        Entity address = new Entity(taddress);
        Attribute country = address.attribute("country");
        people.join(address, "invoice_address", EQUALS(tpeople.column("INVOICE_ID_FK"),taddress.column("ID")));
        assertNotEquals(seller, buyer);
        assertEquals(people, buyer);
        assertEquals(seller, new Join(tpeople, EQUALS(tpeople.column("ID"), ttransaction.column("SELLER_ID_FK"))));
        assertEquals(seller, people);
        Query transborder = new Query().select(transaction)
                .select(seller)
                    .select(people.attribute("name"), "seller_name")
                    .select(people.attribute("invoice_address").apply(country), "seller_country");
        assertEquals("SELECT t.*, p.*, p.name AS seller_name, a.country AS seller_country FROM transaction t INNER JOIN people p ON p.ID=t.SELLER_ID_FK INNER JOIN address a ON p.INVOICE_ID_FK=a.ID",
                transborder.print());
        transborder.select(buyer)
                .select(people.attribute("name"), "buyer_name")
                .select(people.attribute("invoice_address").apply(country), "buyer_country");
        assertEquals("SELECT t.*, p.*, p.name AS seller_name, a.country AS seller_country, p1.*, p1.name AS buyer_name, a1.country AS buyer_country FROM transaction t INNER JOIN people p ON p.ID=t.SELLER_ID_FK INNER JOIN address a ON p.INVOICE_ID_FK=a.ID INNER JOIN people p1 ON p1.ID=t.BUYER_ID_FK INNER JOIN address a1 ON p1.INVOICE_ID_FK=a1.ID",
                transborder.print());
        transborder.where(NOTEQUALS(transborder.selector("seller_country"), transborder.selector("buyer_country")));
        assertEquals("",transborder.print());
    }


    @Test
    void businessCase() throws ScopeException {
        Table _asset = new Table("asset").PK("ID");
        Table _contract = new Table("contract").PK("ID");
        Join contractAsset = new Join(_asset, EQUALS(_asset.getPK().getKeys(), Collections.singletonList(_contract.column("ASSET_ID_FK"))));
        Table _tracker = new Table("tracker").PK("ID");
        Join assetTracker = new Join(_tracker, EQUALS(_asset.getPK().getKeys(), Collections.singletonList(_tracker.column("ASSET_ID_FK"))));
        Table _options = new Table("option").PK("ID");
        Join contractOptions = new Join(_options, EQUALS(_contract.getPK().getKeys(), Collections.singletonList(_options.column("CONTRACT_ID_FK"))));
        Entity asset = new Entity(_asset);
        Entity contract = new Entity(_contract);
        asset.join(contract,"contract", contractAsset);
        contract.join(asset,"asset", contractAsset);
        Entity tracker = new Entity(_tracker);
        asset.join(tracker,"tracker", assetTracker);
        tracker.join(asset,"asset", assetTracker);
        Entity options = new Entity(_options);
        contract.join(options,"options", contractOptions);
        options.join(contract,"contract", contractOptions);
        assertEquals("SELECT a.*, c.*, o.* FROM asset a INNER JOIN contract c ON a.ID=c.ASSET_ID_FK INNER JOIN option o ON c.ID=o.CONTRACT_ID_FK",
            new Query().select(asset).select(asset.attribute("contract")).select(contract.attribute("options")).print());
        assertEquals("SELECT c.*, a.*, o.* FROM contract c INNER JOIN asset a ON a.ID=c.ASSET_ID_FK INNER JOIN option o ON c.ID=o.CONTRACT_ID_FK",
                new Query().select(contract).select(contract.attribute("asset")).select(contract.attribute("options")).print());
        assertEquals("SELECT o.*, c.*, a.* FROM option o INNER JOIN contract c ON c.ID=o.CONTRACT_ID_FK INNER JOIN asset a ON a.ID=c.ASSET_ID_FK",
                new Query().select(options).select(options.attribute("contract")).select(contract.attribute("asset")).print());
        assertEquals("SELECT o.*, a.*, c.* FROM option o ,contract c INNER JOIN asset a ON a.ID=c.ASSET_ID_FK WHERE c.ID=o.CONTRACT_ID_FK",
                new Query().select(options).select(contract.attribute("asset")).select(options.attribute("contract")).print());
    }

}