package com.budgettracker.app.data.model

import com.budgettracker.app.R
import java.util.Currency
import java.util.Locale

enum class AppCurrency(
    val code: String,
    val symbol: String,
    val displayName: String,
    val flagResId: Int,
    val flagEmoji: String
) {
    USD("USD", "$", "USD ($) - United States", R.drawable.ic_flag_us, "🇺🇸"),
    EUR("EUR", "€", "EUR (€) - European Union", R.drawable.ic_flag_eu, "🇪🇺"),
    GBP("GBP", "£", "GBP (£) - United Kingdom", R.drawable.ic_flag_gb, "🇬🇧"),
    INR("INR", "₹", "INR (₹) - India", R.drawable.ic_flag_in, "🇮🇳"),
    JPY("JPY", "¥", "JPY (¥) - Japan", R.drawable.ic_flag_jp, "🇯🇵"),
    CAD("CAD", "CA$", "CAD (CA$) - Canada", R.drawable.ic_flag_ca, "🇨🇦"),
    AUD("AUD", "AU$", "AUD (AU$) - Australia", R.drawable.ic_flag_au, "🇦🇺"),
    CHF("CHF", "CHF", "CHF (CHF) - Switzerland", R.drawable.ic_flag_ch, "🇨🇭"),
    CNY("CNY", "¥", "CNY (¥) - China", R.drawable.ic_flag_cn, "🇨🇳");

    companion object {
        fun fromCode(code: String): AppCurrency {
            return entries.find { it.code.equals(code, ignoreCase = true) } ?: getDefault()
        }

        fun getDefault(): AppCurrency {
            return try {
                val localeCurrencyCode = Currency.getInstance(Locale.getDefault())?.currencyCode
                entries.find { it.code.equals(localeCurrencyCode, ignoreCase = true) } ?: USD
            } catch (e: Exception) {
                USD
            }
        }
    }
}
