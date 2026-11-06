package com.budgettracker.app.util

import com.budgettracker.app.data.model.AppCurrency
import java.util.Locale

object CurrencyUtils {
    /**
     * Formats an amount in cents (minor currency units) to a formatted currency string.
     * E.g., 1250L with USD -> "$12.50", with EUR -> "€12.50", with INR -> "₹12.50"
     * When mask == true (Privacy Shield active), returns "$••••"
     */
    fun formatCents(cents: Long, currency: AppCurrency = AppCurrency.USD, mask: Boolean = false): String {
        if (mask) {
            return "${currency.symbol}••••"
        }
        val dollars = cents.toDouble() / 100.0
        val formattedNumber = String.format(Locale.US, "%,.2f", dollars)
        return "${currency.symbol}$formattedNumber"
    }

    /**
     * Parses a string input (e.g. "12.50" or "12") into cents.
     * Returns null if input is invalid or negative.
     */
    fun parseToCents(input: String): Long? {
        val sanitized = input.trim().replace(",", ".")
        if (sanitized.isEmpty()) return null

        val regex = Regex("""^\d+(\.\d{1,2})?$""")
        if (!regex.matches(sanitized)) return null

        val parts = sanitized.split(".")
        val dollars = parts[0].toLongOrNull() ?: return null
        val cents = if (parts.size > 1) {
            when (parts[1].length) {
                1 -> parts[1].toLong() * 10
                2 -> parts[1].toLong()
                else -> return null
            }
        } else {
            0L
        }

        return (dollars * 100L) + cents
    }
}
