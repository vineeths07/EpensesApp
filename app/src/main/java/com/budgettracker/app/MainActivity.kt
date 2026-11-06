package com.budgettracker.app

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import com.budgettracker.app.data.database.AppDatabase
import com.budgettracker.app.data.preferences.CurrencyPreferenceManager
import com.budgettracker.app.data.preferences.SecurityPreferenceManager
import com.budgettracker.app.data.repository.OnlineExchangeRateRepository
import com.budgettracker.app.data.repository.TransactionRepository
import com.budgettracker.app.data.repository.TransactionRepositoryImpl
import com.budgettracker.app.ui.daily.DailyExpenseScreen
import com.budgettracker.app.ui.daily.DailyExpenseViewModel
import com.budgettracker.app.ui.daily.DailyExpenseViewModelFactory
import com.budgettracker.app.ui.monthly.MonthlyExpenseScreen
import com.budgettracker.app.ui.monthly.MonthlyExpenseViewModel
import com.budgettracker.app.ui.monthly.MonthlyExpenseViewModelFactory
import com.budgettracker.app.ui.personalisation.PersonalisationScreen
import com.budgettracker.app.ui.security.BiometricLockScreen
import com.budgettracker.app.ui.splash.SplashScreen
import com.budgettracker.app.ui.theme.BudgetTrackerTheme
import com.budgettracker.app.ui.yearly.YearlyExpenseScreen
import com.budgettracker.app.ui.yearly.YearlyExpenseViewModel
import com.budgettracker.app.ui.yearly.YearlyExpenseViewModelFactory

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = AppDatabase.getDatabase(applicationContext)
        val repository = TransactionRepositoryImpl(database.transactionDao(), database.categoryDao(), database.budgetDao())
        val currencyManager = CurrencyPreferenceManager(applicationContext)
        val securityManager = SecurityPreferenceManager(applicationContext)
        val exchangeRateRepo = OnlineExchangeRateRepository(applicationContext)

        setContent {
            BudgetTrackerTheme {
                BudgetTrackerApp(
                    repository = repository,
                    currencyManager = currencyManager,
                    securityManager = securityManager,
                    exchangeRateRepo = exchangeRateRepo
                )
            }
        }
    }
}

enum class AppDestinations(
    val label: String,
    val icon: ImageVector,
) {
    DAILY("Daily", Icons.Default.Today),
    MONTHLY("Monthly", Icons.Default.CalendarMonth),
    YEARLY("Yearly", Icons.AutoMirrored.Filled.TrendingUp),
    PERSONALISATION("Personalise", Icons.Default.Tune),
}

@Composable
fun BudgetTrackerApp(
    repository: TransactionRepository,
    currencyManager: CurrencyPreferenceManager,
    securityManager: SecurityPreferenceManager,
    exchangeRateRepo: OnlineExchangeRateRepository
) {
    var showSplash by rememberSaveable { mutableStateOf(true) }
    var currentDestination by rememberSaveable { mutableStateOf(AppDestinations.DAILY) }
    val currentCurrency by currencyManager.selectedCurrency.collectAsState()
    val isBiometricEnabled by securityManager.isBiometricEnabled.collectAsState()
    val isPrivacyModeEnabled by securityManager.isPrivacyModeEnabled.collectAsState()

    var isAppUnlocked by rememberSaveable { mutableStateOf(!securityManager.isBiometricEnabled.value) }

    val dailyViewModel: DailyExpenseViewModel = viewModel(
        factory = DailyExpenseViewModelFactory(repository)
    )

    val monthlyViewModel: MonthlyExpenseViewModel = viewModel(
        factory = MonthlyExpenseViewModelFactory(repository)
    )

    val yearlyViewModel: YearlyExpenseViewModel = viewModel(
        factory = YearlyExpenseViewModelFactory(repository)
    )

    // Synchronize viewing currency
    LaunchedEffect(currentCurrency) {
        dailyViewModel.setViewingCurrency(currentCurrency)
        monthlyViewModel.setViewingCurrency(currentCurrency)
        yearlyViewModel.setViewingCurrency(currentCurrency)
    }

    // Refresh live daily market rates on launch in background
    LaunchedEffect(Unit) {
        exchangeRateRepo.refreshRates()
    }

    LaunchedEffect(isBiometricEnabled) {
        if (!isBiometricEnabled) {
            isAppUnlocked = true
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        NavigationSuiteScaffold(
            navigationSuiteItems = {
                AppDestinations.entries.forEach { destination ->
                    item(
                        icon = {
                            Icon(
                                imageVector = destination.icon,
                                contentDescription = destination.label
                            )
                        },
                        label = { Text(destination.label) },
                        selected = destination == currentDestination,
                        onClick = { currentDestination = destination }
                    )
                }
            }
        ) {
            AnimatedContent(
                targetState = currentDestination,
                label = "tab_transition",
                transitionSpec = {
                    fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300))
                }
            ) { targetDest ->
                when (targetDest) {
                    AppDestinations.DAILY -> {
                        DailyExpenseScreen(
                            viewModel = dailyViewModel,
                            currency = currentCurrency,
                            onCurrencySelected = { currencyManager.setCurrency(it) },
                            isPrivacyMode = isPrivacyModeEnabled
                        )
                    }
                    AppDestinations.MONTHLY -> {
                        MonthlyExpenseScreen(
                            viewModel = monthlyViewModel,
                            onDeleteTransaction = { dailyViewModel.deleteTransaction(it) },
                            onRestoreTransaction = { dailyViewModel.restoreTransaction(it) },
                            currency = currentCurrency,
                            onCurrencySelected = { currencyManager.setCurrency(it) },
                            isBiometricEnabled = isBiometricEnabled,
                            onBiometricToggle = { enabled ->
                                securityManager.setBiometricEnabled(enabled)
                                if (enabled) isAppUnlocked = true
                            },
                            isPrivacyMode = isPrivacyModeEnabled
                        )
                    }
                    AppDestinations.YEARLY -> {
                        YearlyExpenseScreen(
                            viewModel = yearlyViewModel,
                            currency = currentCurrency,
                            onCurrencySelected = { currencyManager.setCurrency(it) },
                            isBiometricEnabled = isBiometricEnabled,
                            onBiometricToggle = { enabled ->
                                securityManager.setBiometricEnabled(enabled)
                                if (enabled) isAppUnlocked = true
                            },
                            isPrivacyMode = isPrivacyModeEnabled
                        )
                    }
                    AppDestinations.PERSONALISATION -> {
                        PersonalisationScreen(
                            currencyManager = currencyManager,
                            securityManager = securityManager,
                            exchangeRateRepo = exchangeRateRepo,
                            repository = repository
                        )
                    }
                }
            }
        }

        // Biometric Lock Screen Overlay
        if (isBiometricEnabled && !isAppUnlocked && !showSplash) {
            BiometricLockScreen(
                onUnlocked = { isAppUnlocked = true }
            )
        }

        // Animated Splash Screen Overlay
        AnimatedVisibility(
            visible = showSplash,
            enter = EnterTransition.None,
            exit = fadeOut(tween(450)) + scaleOut(targetScale = 1.15f, animationSpec = tween(450))
        ) {
            SplashScreen(onSplashFinished = { showSplash = false })
        }
    }
}
