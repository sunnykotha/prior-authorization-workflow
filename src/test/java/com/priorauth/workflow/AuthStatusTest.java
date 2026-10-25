package com.priorauth.workflow;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class AuthStatusTest {

    @Test
    void shouldContainAllTenAllowedStatuses() {
        assertEquals(10, AuthStatus.values().length);
    }

    @Test
    void shouldContainEveryRequiredStatus() {
        assertNotNull(AuthStatus.valueOf("SUBMITTED"));
        assertNotNull(AuthStatus.valueOf("UNDER_REVIEW"));
        assertNotNull(AuthStatus.valueOf("INFO_REQUESTED"));
        assertNotNull(AuthStatus.valueOf("APPROVED"));
        assertNotNull(AuthStatus.valueOf("DENIED"));
        assertNotNull(AuthStatus.valueOf("APPEALED"));
        assertNotNull(AuthStatus.valueOf("APPEAL_REVIEW"));
        assertNotNull(AuthStatus.valueOf("OVERTURNED"));
        assertNotNull(AuthStatus.valueOf("UPHELD"));
        assertNotNull(AuthStatus.valueOf("CLOSED_INSUFFICIENT_INFO"));
    }
}