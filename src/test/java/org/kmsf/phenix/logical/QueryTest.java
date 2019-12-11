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
                people.join("department", department,
                        EQUALS(tPeople.column("DEP_ID_FK"), tDepartment.column("ID")));
        Attribute depName = department.attribute("depName", "name");
        // then
        // ... using where to force the selector to be empty...
        assertThat(new Query().from(people).where(EQUALS(peopleDepartment.apply(depName), CONST("R&D"))).print())
                .isEqualTo("SELECT p.name AS peopleName, d.name AS depName FROM people p INNER JOIN department d ON p.DEP_ID_FK=d.ID WHERE d.name='R&D'");
        assertThat(new Query().select(peopleDepartment).print())
                .isEqualTo("SELECT p.name AS peopleName, d.name AS depName FROM people p INNER JOIN department d ON p.DEP_ID_FK=d.ID");
        assertThat(new Query().select(peopleName).select(peopleDepartment).print())
                .isEqualTo("SELECT p.name AS peopleName FROM people p INNER JOIN department d ON p.DEP_ID_FK=d.ID");
    }

    @org.junit.jupiter.api.Test
    public void should_handle_sub_query() throws ScopeException {
        // given
        Table tPeople = new Table("people").PK("ID");
        Entity people = new Entity("people", tPeople);
        Attribute peopleName = people.attribute("peopleName", tPeople.column("name"));
        Table tDepartment = new Table("department");
        Entity department = new Entity("department", tDepartment);
        Attribute peopleDepartment =
                people.join("department", tDepartment,
                        EQUALS(tPeople.column("DEP_ID_FK"), tDepartment.column("ID")));
        Attribute departmentManager = department.attribute("manager", tPeople.join(tDepartment.FK("MANAGER_ID_FK")));
        Attribute depName = department.attribute("deptName", tDepartment.column("name"));
        // when
        Query johnDoeDepartment = new Query().from(department).where(EQUALS(departmentManager.apply(peopleName), CONST("JohnDoe")));
        // then
        assertThat(johnDoeDepartment.print())
                .isEqualTo("SELECT d.name AS deptName, p.ID, p.name, p.DEP_ID_FK FROM department d INNER JOIN people p ON p.ID=d.MANAGER_ID_FK WHERE p.name='JohnDoe'");
        assertThat(new Query().from(johnDoeDepartment)
                .select(depName)
                .select(peopleName).print())
                .isEqualTo("SELECT a.deptName, a.peopleName FROM (SELECT d.name AS deptName, p.name AS peopleName FROM department d INNER JOIN people p ON p.ID=d.MANAGER_ID_FK WHERE p.name='JohnDoe') a");
    }

    /**
     * testing how to join from a sub-select statement
     *
     * @throws ScopeException
     */
    @org.junit.jupiter.api.Test
    public void should_support_sub_select_in_join() throws ScopeException {
        // given
        Table tPeople = new Table("people").PK("ID");
        Entity people = new Entity("people", tPeople);
        Attribute peopleName = people.attribute("name", tPeople.column("name"));
        Table tDepartment = new Table("department").PK("ID");
        Entity department = new Entity("department", tDepartment);
        // when
        Attribute departmentPeoples =
                department.oppositeJoin("peoples",people,"DEP_ID_FK");
        // then
        assertThat(departmentPeoples.getType().getTail()).isPresent()
                .hasValueSatisfying(view -> {
                    view.equals(departmentPeoples.unwrapReference());
                    view.isCompatibleWith(people);
                });
        assertEquals("SELECT p.name FROM (SELECT d.* FROM department d) a INNER JOIN people p ON p.DEP_ID_FK=a.ID",
                new Query()
                        .from(new Query().select(department))
                        .select(departmentPeoples.apply(peopleName)).print());
    }

    @Test
    void should_implement_selector() throws ScopeException {
        // given
        Table tPeople = new Table("people");
        Entity people = new Entity("people", tPeople);
        Attribute peopleName = people.attribute("peopleName", tPeople.column("name"));
        // when
        Query query = new Query()
                .select(peopleName);
        // then
        assertThat(query.print()).isEqualTo("SELECT p.name AS peopleName FROM people p");
        assertDoesNotThrow(() -> query.selector("peopleName"));
        assertThat(query.selector("peopleName").get().unwrapReference()).isEqualTo(peopleName);
        assertThat(query.getSelectors()).containsExactly(query.selector("peopleName").get());
        assertFalse(query.selector("undefined").isPresent());
    }

    @Test
    void should_support_duplicate_entities() throws ScopeException {
        // given
        Table tpeople = new Table("people").PK("ID");
        Entity people = new Entity(tpeople);
        Table ttransaction = new Table("transaction").PK("ID");
        Entity transaction = new Entity(ttransaction);
        Table taddress = new Table("address").PK("ID");
        Entity address = new Entity(taddress);
        Attribute country = address.attribute("country");
        people.join("invoice_address", address, "INVOICE_ID_FK");
        // when
        Attribute buyer = transaction.join("buyer", people, "BUYER_ID_FK");
        Attribute seller = transaction.join("seller", people, "SELLER_ID_FK");
        // then
        assertThat(buyer).isNotEqualTo(seller);
        assertThat(((Join)buyer.getDefinition()).isCompatibleWith((Join)seller.getDefinition())).isFalse();
        // when
        Query transborder = new Query().select(transaction)
                .select(seller)
                    .select(people.attribute("name"), "seller_name")
                    .from(people.attribute("invoice_address")).select(country, "seller_country");
        // then
        assertThat(transborder.print())
                .isEqualTo("SELECT p.name AS seller_name, a.country AS seller_country FROM transaction t INNER JOIN people p ON p.ID=t.SELLER_ID_FK INNER JOIN address a ON a.ID=p.INVOICE_ID_FK");
        // when
        transborder.select(buyer)
                .select(people.attribute("name"), "buyer_name")
                .from(people.attribute("invoice_address"))
                .select(country, "buyer_country");
        // then
        assertThat(transborder.print())
                .isEqualTo("SELECT p.name AS seller_name, a.country AS seller_country, p1.name AS buyer_name, a1.country AS buyer_country FROM transaction t INNER JOIN people p ON p.ID=t.SELLER_ID_FK INNER JOIN address a ON a.ID=p.INVOICE_ID_FK INNER JOIN people p1 ON p1.ID=t.BUYER_ID_FK INNER JOIN address a1 ON a1.ID=p1.INVOICE_ID_FK");
        assertThat(transborder.where(NOTEQUALS(transborder.selector("seller_country").get(), transborder.selector("buyer_country").get())).print())
        .isEqualTo("SELECT p.name AS seller_name, a.country AS seller_country, p1.name AS buyer_name, a1.country AS buyer_country FROM transaction t INNER JOIN people p ON p.ID=t.SELLER_ID_FK INNER JOIN address a ON a.ID=p.INVOICE_ID_FK INNER JOIN people p1 ON p1.ID=t.BUYER_ID_FK INNER JOIN address a1 ON a1.ID=p1.INVOICE_ID_FK WHERE a.country!=a1.country");
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
        asset.join("contract", contract, contractAsset);
        contract.join("asset", asset, contractAsset);
        Entity tracker = new Entity(_tracker);
        asset.join("tracker", tracker, assetTracker);
        tracker.join("asset", asset, assetTracker);
        Entity options = new Entity(_options);
        contract.join("options", options, contractOptions);
        options.join("contract", contract, contractOptions);
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