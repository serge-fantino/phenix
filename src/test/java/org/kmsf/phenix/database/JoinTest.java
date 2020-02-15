package org.kmsf.phenix.database;

import org.junit.jupiter.api.Test;
import org.kmsf.phenix.sql.PrintResult;
import org.kmsf.phenix.sql.Scope;
import org.kmsf.phenix.function.FunctionType;

import static org.assertj.core.api.Assertions.*;
import static org.kmsf.phenix.function.Functions.*;

import static org.junit.jupiter.api.Assertions.*;

class JoinTest {

    @Test
    public void should_implements_equal_based_on_values() throws ScopeException {
        // given
        Table a = new Table("a").PK("ID");
        Table b = new Table("b").PK("ID");
        Table c = new Table("c").PK("ID");
        // when
        Join join = b.join(a, "A_ID_FK");
        // then
        assertThat(join).isEqualTo(join);
        assertThat(join).isEqualTo(b.join( a, "A_ID_FK"));
        assertThat(join).isNotEqualTo(b.join(a, "A2_ID_FK"));
        assertThat(join).isNotEqualTo(c.join(a, "A_ID_FK"));
        assertThat(join).isNotEqualTo(c.join(b, "A_ID_FK"));
    }

    @Test
    public void should_resolve_join_definition_based_on_type() throws ScopeException {
        // given
        Table a = new Table("a");
        Table b = new Table("b");
        var scope = new Scope(new Select());
        scope = scope.add(a, "a");
        // when
        Join join = new Join(b, EQUALS(a.column("ID"), b.column("A_ID_FK")));
        scope = scope.add(join, "b");
        // then
        assertThat(join.getType()).isEqualTo(join.getType());
        assertThat(join.getType()).isEqualTo(new FunctionType(a,join));
        assertThat(join.print(scope, new PrintResult()).toString()).isEqualTo("a.ID=b.A_ID_FK");
        assertThat(join.getDefinition().getType().getValues()).containsExactly(a, join);
    }

    @Test
    public void should_different_join_with_same_source_and_target_be_different() throws ScopeException {
        // given
        Table people = new Table("people").PK("ID");
        Table transaction = new Table("transaction").PK("ID");
        // when
        Join buyer = new Join(transaction, people, EQUALS(people.column("ID"),transaction.column("BUYER_ID_FK")));
        Join seller = new Join(transaction, people, EQUALS(people.column("ID"),transaction.column("SELLER_ID_FK")));
        // then
        assertNotEquals(buyer, seller);
        assertNotEquals(buyer, people);
        assertNotEquals(buyer, transaction);
        assertNotEquals(people, seller);
        assertNotEquals(people, transaction);
    }

    @Test
    public void should_join_inherits_from_target() throws ScopeException {
        // given
        Table people = new Table("people").PK("ID");
        Table transaction = new Table("transaction").PK("ID");
        // when
        Join buyer = transaction.join(people, "BUYER_ID_FK");
        Join seller = transaction.join(people, "SELLER_ID_FK");
        // then
        assertThat(buyer.inheritsFrom(people)).isTrue();
        assertThat(people.inheritsFrom(buyer)).isFalse();
        assertThat(buyer.inheritsFrom(buyer)).isTrue();
        assertThat(buyer.inheritsFrom(seller)).isFalse();
    }

    @Test
    public void should_not_inherits_from_different_join() throws ScopeException {
        // given
        Table people = new Table("people").PK("ID");
        Table transaction = new Table("transaction").PK("ID");
        // when
        Join buyer = transaction.join(people, "BUYER_ID_FK");
        Join seller = transaction.join(people, "SELLER_ID_FK");
        // then
        assertThat(buyer.isCompatibleWith(seller)).isFalse();
    }

    @Test
    public void should_support_self_join() throws ScopeException {
        // given
        Table people = new Table("people").PK("ID");
        Column peopleName = people.column("name");
        // when
        Join manager = people.join("manager", people, "MANAGER_ID_FK");
        Scope scope = new Scope(people).add(people,"people").add(manager, "manager");
        // then
        assertThat(manager.getDefinition().getType().getValues()).containsExactly(people, manager);
        assertThat(manager.print(scope, new PrintResult()).print())
                .isEqualTo("people.MANAGER_ID_FK=manager.ID");
    }

}