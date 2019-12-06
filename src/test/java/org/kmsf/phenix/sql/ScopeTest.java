package org.kmsf.phenix.sql;

import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.kmsf.phenix.database.ScopeException;
import org.kmsf.phenix.database.Select;
import org.kmsf.phenix.database.Table;

import java.util.ArrayList;
import java.util.Arrays;

class ScopeTest {

    @Test
    void should_create_an_empty_scope() {
        // given
        Select select = new Select();
        // when
        Scope scope = new Scope(select);
        // then
        assertThat(scope.isEmpty()).isTrue();
    }

    @Test
    void should_not_change_scope_when_adding_mapping() {
        // given
        Select select = new Select();
        Table first = new Table("first");
        // when
        Scope scope = new Scope(select);
        var scope1 = scope.add(first, "a");
        // then
        assertThat(scope.isEmpty()).as("the original scope must not be modified when adding a mapping").isTrue();
        assertThat(scope).isNotEqualTo(scope1);
    }

    @Test
    void should_scope_be_invariant() throws ScopeException {
        // given
        Table first = new Table("first");
        Table second = new Table("second");
        // when
        Scope scope = new Scope();
        var scope1 = scope.add(first, "a");
        var scope2 = scope1.add(second, "b");
        var scope3 = scope2.add(first, "c");
        // then
        assertThat(scope2.resolves(first).getAlias()).isEqualTo("a");
    }

    @Test
    void should_iterate_over_scope_in_lifo_order() {
        // given
        Table first = new Table("first");
        Table second = new Table("second");
        // when
        Scope scope = new Scope();
        var scope1 = scope.add(first, "a");
        var scope2 = scope1.add(second, "b");
        var scope3 = scope2.add(first, "c");
        // then
        ArrayList<String> results = new ArrayList<>();
        for (Mapping m : scope3) {
            results.add(m.getAlias());
        }
        assertThat(results).isEqualTo(Arrays.asList(new String[]{"c", "b", "a"}));
    }

}