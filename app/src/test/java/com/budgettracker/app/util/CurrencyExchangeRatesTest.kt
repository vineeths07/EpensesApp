package com.budgettracker.app.util

import com.budgettracker.app.data.model.AppCurrency
import org.junit.Assert.assertEquals
import org.junit.Test

class CurrencyExchangeRatesTest {
    @Test
    fun testConvert_sameCurrencyReturnsIdentical() {
        assertEquals(1500L, CurrencyExchangeRates.convert(1500L, AppCurrency.USD, AppCurrency.USD))
        assertEquals(25000L, CurrencyExchangeRates.convert(25000L, AppCurrency.INR, AppCurrency.INR))
    }

    @Test
    fun testConvert_zeroReturnsZero() {
        assertEquals(0L, CurrencyExchangeRates.convert(0L, AppCurrency.USD, AppCurrency.INR))
    }

    @Test
    fun testConvert_usdToInrAndReverse() {
        // $10.00 (1000 cents) at 83.50 -> ₹835.00 (83500 cents)
        val inr = CurrencyExchangeRates.convert(1000L, AppCurrency.USD, AppCurrency.INR)
        assertEquals(83500L, inr)

        // ₹835.00 at 83.50 -> $10.00
        val backToUsd = CurrencyExchangeRates.convert(83500L, AppCurrency.INR, AppCurrency.USD)
        assertEquals(1000L, backToUsd)
    }

    @Test
    fun testConvert_usdToEur() {
        // $10.00 at 0.92 -> €9.20 (920 cents)
        val eur = CurrencyExchangeRates.convert(1000L, AppCurrency.USD, AppCurrency.EUR)
        assertEquals(920L, eur)
    }

    @Test
    fun testGetRate() {
        assertEquals(1.0, CurrencyExchangeRates.getRate(AppCurrency.USD, AppCurrency.USD), 0.001)
        assertEquals(83.50, CurrencyExchangeRates.getRate(AppCurrency.USD, AppCurrency.INR), 0.001)
    }
}
