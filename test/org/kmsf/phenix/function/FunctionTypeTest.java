package org.kmsf.phenix.function;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FunctionTypeTest {

    @Test
    void add() {
        assertEquals(new FunctionType(new ConstFunction<>("A"), new ConstFunction<>("B")), new FunctionType((new ConstFunction<>("A"))).add(new ConstFunction<>("B")));
    }

    @Test
    void addAll() {
    }

    @Test
    void testEquals() {
    }
}