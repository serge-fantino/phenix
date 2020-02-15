package org.kmsf.phenix.algebra;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FunctionTypeTest {

    @Test
    void add() {
        assertEquals(new FunctionType(new ConstExpression<>("A"), new ConstExpression<>("B")), new FunctionType((new ConstExpression<>("A"))).add(new ConstExpression<>("B")));
    }

    @Test
    void addAll() {
    }

    @Test
    void testEquals() {
    }
}