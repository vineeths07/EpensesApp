package com.budgettracker.app.security

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class BiometricCapabilityTest {
    @Test
    fun testBiometricCapabilityEnumValues() {
        assertEquals(4, BiometricCapability.entries.size)
        assertNotNull(BiometricCapability.valueOf("AVAILABLE"))
        assertNotNull(BiometricCapability.valueOf("NOT_ENROLLED"))
        assertNotNull(BiometricCapability.valueOf("NO_HARDWARE"))
        assertNotNull(BiometricCapability.valueOf("UNAVAILABLE"))
    }
}
