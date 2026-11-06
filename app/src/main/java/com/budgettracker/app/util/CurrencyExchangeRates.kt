package com.budgettracker.app.util

import com.budgettracker.app.data.model.AppCurrency
import kotlin.math.roundToLong

object CurrencyExchangeRates {
    // Baseline market fallback exchange rates relative to 1 USD
    val defaultRatesToUsd: Map<AppCurrency, Double> = mapOf(
        AppCurrency.USD to 1.0,
        AppCurrency.EUR to 0.92,
        AppCurrency.GBP to 0.79,
        AppCurrency.INR to 83.50,
        AppCurrency.JPY to 155.0,
        AppCurrency.CAD to 1.36,
        AppCurrency.AUD to 1.52,
        AppCurrency.CHF to 0.90,
        AppCurrency.CNY to 7.25
    )

    @Volatile
    private var activeRates: Map<AppCurrency, Double> = defaultRatesToUsd

    fun updateRates(newRates: Map<AppCurrency, Double>) {
        activeRates = defaultRatesToUsd + newRates
    }

    fun getActiveRates(): Map<AppCurrency, Double> = activeRates

    /**
     * Converts an amount in minor units (cents) from one currency to another using the latest live rates.
     * E.g. convert(1000L, USD, INR) -> 83500L (representing ₹835.00)
     */
    fun convert(amountCents: Long, from: AppCurrency, to: AppCurrency): Long {
        if (from == to || amountCents == 0L) return amountCents

        val rateFrom = activeRates[from] ?: defaultRatesToUsd[from] ?: 1.0
        val rateTo = activeRates[to] ?: defaultRatesToUsd[to] ?: 1.0

        val inUsd = amountCents.toDouble() / rateFrom
        val converted = inUsd * rateTo
        return converted.roundToLong()
    }

    /**
     * Returns the exchange rate from one currency to another (1 unit of 'from' = X units of 'to').
     * E.g. getRate(USD, INR) -> 83.50
     */
    fun getRate(from: AppCurrency, to: AppCurrency): Double {
        if (from == to) return 1.0
        val rateFrom = activeRates[from] ?: defaultRatesToUsd[from] ?: 1.0
        val rateTo = activeRates[to] ?: defaultRatesToUsd[to] ?: 1.0
        return rateTo / rateFrom
    }
}
