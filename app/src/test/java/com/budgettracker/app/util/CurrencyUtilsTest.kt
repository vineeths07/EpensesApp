package com.budgettracker.app.util

import com.budgettracker.app.data.model.AppCurrency
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CurrencyUtilsTest {
    @Test
    fun testFormatCents_currencies() {
        assertEquals("$12.50", CurrencyUtils.formatCents(1250L, AppCurrency.USD))
        assertEquals("€12.50", CurrencyUtils.formatCents(1250L, AppCurrency.EUR))
        assertEquals("£12.50", CurrencyUtils.formatCents(1250L, AppCurrency.GBP))
        assertEquals("₹12.50", CurrencyUtils.formatCents(1250L, AppCurrency.INR))
        assertEquals("¥12.50", CurrencyUtils.formatCents(1250L, AppCurrency.JPY))
        assertEquals("$0.00", CurrencyUtils.formatCents(0L, AppCurrency.USD))
        assertEquals("$10,000.00", CurrencyUtils.formatCents(1000000L, AppCurrency.USD))
    }

    @Test
    fun testFormatCents_privacyShieldMasking() {
        assertEquals("$••••", CurrencyUtils.formatCents(1250L, AppCurrency.USD, mask = true))
        assertEquals("₹••••", CurrencyUtils.formatCents(50000L, AppCurrency.INR, mask = true))
        assertEquals("€••••", CurrencyUtils.formatCents(920L, AppCurrency.EUR, mask = true))
    }

    @Test
    fun testParseToCents_validInputs() {
        assertEquals(1250L, CurrencyUtils.parseToCents("12.50"))
        assertEquals(1250L, CurrencyUtils.parseToCents("12.5"))
        assertEquals(1200L, CurrencyUtils.parseToCents("12"))
        assertEquals(99L, CurrencyUtils.parseToCents("0.99"))
        assertEquals(5L, CurrencyUtils.parseToCents("0.05"))
        assertEquals(0L, CurrencyUtils.parseToCents("0"))
        assertEquals(1500L, CurrencyUtils.parseToCents("15,00")) // Comma decimal support
    }

    @Test
    fun testParseToCents_invalidInputs() {
        assertNull(CurrencyUtils.parseToCents(""))
        assertNull(CurrencyUtils.parseToCents("   "))
        assertNull(CurrencyUtils.parseToCents("abc"))
        assertNull(CurrencyUtils.parseToCents("-12.50"))
        assertNull(CurrencyUtils.parseToCents("12.345")) // More than 2 decimal digits
    }
}
