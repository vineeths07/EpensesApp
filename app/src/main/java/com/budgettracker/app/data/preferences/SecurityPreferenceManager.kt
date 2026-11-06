package com.budgettracker.app.data.preferences

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SecurityPreferenceManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("budget_tracker_security_prefs", Context.MODE_PRIVATE)

    private val _isBiometricEnabled = MutableStateFlow(loadBiometricEnabled())
    val isBiometricEnabled: StateFlow<Boolean> = _isBiometricEnabled.asStateFlow()

    private val _isPrivacyModeEnabled = MutableStateFlow(loadPrivacyModeEnabled())
    val isPrivacyModeEnabled: StateFlow<Boolean> = _isPrivacyModeEnabled.asStateFlow()

    private fun loadBiometricEnabled(): Boolean {
        return prefs.getBoolean("is_biometric_enabled", false)
    }

    private fun loadPrivacyModeEnabled(): Boolean {
        return prefs.getBoolean("is_privacy_mode_enabled", false)
    }

    fun setBiometricEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("is_biometric_enabled", enabled).apply()
        _isBiometricEnabled.value = enabled
    }

    fun setPrivacyModeEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("is_privacy_mode_enabled", enabled).apply()
        _isPrivacyModeEnabled.value = enabled
    }
}
