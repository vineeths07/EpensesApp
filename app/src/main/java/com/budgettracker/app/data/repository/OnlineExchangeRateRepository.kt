package com.budgettracker.app.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.budgettracker.app.data.model.AppCurrency
import com.budgettracker.app.util.CurrencyExchangeRates
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class OnlineExchangeRateRepository(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("exchange_rates_cache_prefs", Context.MODE_PRIVATE)

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _lastUpdatedMillis = MutableStateFlow(prefs.getLong("last_updated_time", 0L))
    val lastUpdatedMillis: StateFlow<Long> = _lastUpdatedMillis.asStateFlow()

    init {
        loadCachedRates()
    }

    private fun loadCachedRates() {
        val cached = mutableMapOf<AppCurrency, Double>()
        AppCurrency.entries.forEach { currency ->
            val key = "rate_${currency.code}"
            if (prefs.contains(key)) {
                val rate = prefs.getFloat(key, 0f).toDouble()
                if (rate > 0.0) {
                    cached[currency] = rate
                }
            }
        }
        if (cached.isNotEmpty()) {
            CurrencyExchangeRates.updateRates(cached)
        }
    }

    suspend fun refreshRates(): Boolean = withContext(Dispatchers.IO) {
        _isRefreshing.value = true
        var success = false
        try {
            val apiUrl = "https://open.er-api.com/v6/latest/USD"
            val url = URL(apiUrl)
            val connection = url.openConnection() as HttpURLConnection
            connection.connectTimeout = 8000
            connection.readTimeout = 8000
            connection.requestMethod = "GET"

            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                val reader = BufferedReader(InputStreamReader(connection.inputStream))
                val response = reader.readText()
                reader.close()

                val json = JSONObject(response)
                if (json.optString("result") == "success" && json.has("rates")) {
                    val ratesObj = json.getJSONObject("rates")
                    val newRates = mutableMapOf<AppCurrency, Double>()
                    val editor = prefs.edit()

                    AppCurrency.entries.forEach { currency ->
                        if (ratesObj.has(currency.code)) {
                            val rate = ratesObj.getDouble(currency.code)
                            newRates[currency] = rate
                            editor.putFloat("rate_${currency.code}", rate.toFloat())
                        }
                    }

                    val now = System.currentTimeMillis()
                    editor.putLong("last_updated_time", now)
                    editor.apply()

                    CurrencyExchangeRates.updateRates(newRates)
                    _lastUpdatedMillis.value = now
                    success = true
                }
            }
            connection.disconnect()
        } catch (e: Exception) {
            success = false
        } finally {
            _isRefreshing.value = false
        }
        success
    }
}
