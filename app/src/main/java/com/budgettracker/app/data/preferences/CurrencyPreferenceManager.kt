package com.budgettracker.app.data.preferences

import android.content.Context
import android.content.SharedPreferences
import com.budgettracker.app.data.model.AppCurrency
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class CurrencyPreferenceManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("budget_tracker_prefs", Context.MODE_PRIVATE)

    private val _selectedCurrency = MutableStateFlow(loadCurrency())
    val selectedCurrency: StateFlow<AppCurrency> = _selectedCurrency.asStateFlow()

    private fun loadCurrency(): AppCurrency {
        val defaultCurrency = AppCurrency.getDefault()
        val savedCode = prefs.getString("selected_currency", defaultCurrency.code) ?: defaultCurrency.code
        return AppCurrency.fromCode(savedCode)
    }

    fun setCurrency(currency: AppCurrency) {
        prefs.edit().putString("selected_currency", currency.code).apply()
        _selectedCurrency.value = currency
    }
}
