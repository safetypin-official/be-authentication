package com.safetypin.authentication.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RoleTest {

    @Test
    void testEnumValues() {
        // Test that the enum has the expected values
        assertEquals(3, Role.values().length);
        assertEquals(Role.REGISTERED_USER, Role.valueOf("REGISTERED_USER"));
        assertEquals(Role.PREMIUM_USER, Role.valueOf("PREMIUM_USER"));
        assertEquals(Role.MODERATOR, Role.valueOf("MODERATOR"));
    }
    
    @Test
    void testEnumOrdinals() {
        // Test the ordinals (mostly for coverage)
        assertEquals(0, Role.REGISTERED_USER.ordinal());
        assertEquals(1, Role.PREMIUM_USER.ordinal());
        assertEquals(2, Role.MODERATOR.ordinal());
    }

    @Test
    void testEnumToString() {
        // Test the toString method
        assertEquals("REGISTERED_USER", Role.REGISTERED_USER.toString());
        assertEquals("PREMIUM_USER", Role.PREMIUM_USER.toString());
        assertEquals("MODERATOR", Role.MODERATOR.toString());
    }

    @Test
    void testEnumValueOf() {
        // Test the valueOf method
        assertEquals(Role.REGISTERED_USER, Role.valueOf("REGISTERED_USER"));
        assertEquals(Role.PREMIUM_USER, Role.valueOf("PREMIUM_USER"));
        assertEquals(Role.MODERATOR, Role.valueOf("MODERATOR"));
    }
}
