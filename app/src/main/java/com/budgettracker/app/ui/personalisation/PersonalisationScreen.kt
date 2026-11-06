package com.budgettracker.app.ui.personalisation

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.budgettracker.app.R
import com.budgettracker.app.data.model.AppCurrency
import com.budgettracker.app.data.model.BudgetEntity
import com.budgettracker.app.data.model.CategoryEntity
import com.budgettracker.app.data.model.TransactionWithCategory
import com.budgettracker.app.data.preferences.CurrencyPreferenceManager
import com.budgettracker.app.data.preferences.SecurityPreferenceManager
import com.budgettracker.app.data.repository.OnlineExchangeRateRepository
import com.budgettracker.app.data.repository.TransactionRepository
import com.budgettracker.app.ui.components.AppHeader
import com.budgettracker.app.ui.components.CountryFlagBadge
import com.budgettracker.app.ui.monthly.SetBudgetSheet
import com.budgettracker.app.ui.security.BiometricSecurityCard
import com.budgettracker.app.util.CurrencyExchangeRates
import com.budgettracker.app.util.DataExportUtils
import com.budgettracker.app.util.DateUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@Composable
fun PersonalisationScreen(
    currencyManager: CurrencyPreferenceManager,
    securityManager: SecurityPreferenceManager,
    exchangeRateRepo: OnlineExchangeRateRepository,
    repository: TransactionRepository,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentCurrency by currencyManager.selectedCurrency.collectAsState()
    val isBiometricEnabled by securityManager.isBiometricEnabled.collectAsState()
    val isPrivacyModeEnabled by securityManager.isPrivacyModeEnabled.collectAsState()
    val lastUpdatedMillis by exchangeRateRepo.lastUpdatedMillis.collectAsState()
    val isRefreshingRates by exchangeRateRepo.isRefreshing.collectAsState()

    val budgets by repository.getAllBudgets().collectAsState(initial = emptyList())
    val expenseCategories by repository.getCategoriesByType(com.budgettracker.app.data.model.TransactionType.EXPENSE).collectAsState(initial = emptyList())

    var showBudgetSheet by remember { mutableStateOf(false) }

    androidx.compose.material3.Scaffold(
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // App Header
            item {
                AppHeader(
                    title = "Personalisation",
                    subtitle = "Preferences & App Security",
                    currency = currentCurrency,
                    onCurrencySelected = { currencyManager.setCurrency(it) },
                    detailedDescription = "Customize security settings, manage default currency and exchange rates, configure category budgets, and export financial data."
                )
            }

            // Section 1: Security & Protection
            item {
                SectionHeader(title = "Security & Protection")
            }

            item {
                BiometricSecurityCard(
                    isBiometricEnabled = isBiometricEnabled,
                    onBiometricToggle = { securityManager.setBiometricEnabled(it) }
                )
            }

            // Privacy Mode Toggle Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.size(38.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.VisibilityOff,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Text(
                                    text = "Privacy Shield (Mask Numbers)",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Obscures balances as •••• across all screens",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Switch(
                            checked = isPrivacyModeEnabled,
                            onCheckedChange = { securityManager.setPrivacyModeEnabled(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.primary,
                                checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                    }
                }
            }

            // Section 2: Currency & Exchange Rates
            item {
                Spacer(modifier = Modifier.height(10.dp))
                SectionHeader(title = "Currency & Live Daily Rates")
            }

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CountryFlagBadge(currency = currentCurrency, height = 20.dp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Base Display Currency",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = currentCurrency.displayName,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Live rates refresh control
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Online Daily Exchange Rates",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                val statusStr = if (lastUpdatedMillis > 0L) {
                                    "Updated: ${DateUtils.formatTime(lastUpdatedMillis)}"
                                } else {
                                    "Offline baseline rates"
                                }
                                Text(
                                    text = statusStr,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            OutlinedButton(
                                onClick = {
                                    CoroutineScope(Dispatchers.IO).launch {
                                        val ok = exchangeRateRepo.refreshRates()
                                        CoroutineScope(Dispatchers.Main).launch {
                                            if (ok) {
                                                Toast.makeText(context, "Daily exchange rates updated!", Toast.LENGTH_SHORT).show()
                                            } else {
                                                Toast.makeText(context, "Network offline. Using cached rates.", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    }
                                },
                                enabled = !isRefreshingRates,
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                if (isRefreshingRates) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(14.dp),
                                        strokeWidth = 2.dp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Update Rates", style = MaterialTheme.typography.labelMedium)
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "Conversion Benchmarks (1 ${currentCurrency.code} =):",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        val otherCurrencies = remember(currentCurrency) {
                            AppCurrency.entries.filter { it != currentCurrency }.take(4)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            otherCurrencies.forEach { other ->
                                val rate = CurrencyExchangeRates.getRate(currentCurrency, other)
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
                                        .padding(horizontal = 8.dp, vertical = 6.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        CountryFlagBadge(currency = other, height = 12.dp)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = other.code,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    val formattedRate = if (rate >= 100.0) {
                                        String.format(java.util.Locale.US, "%.2f", rate)
                                    } else {
                                        String.format(java.util.Locale.US, "%.4f", rate)
                                    }
                                    Text(
                                        text = formattedRate,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Daily mid-market rates from open.er-api.com. Intraday spot broker rates may fluctuate by ±0.1%.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }

            // Section 3: Monthly Budgets
            item {
                Spacer(modifier = Modifier.height(10.dp))
                SectionHeader(title = "Budgeting & Spending Caps")
            }

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.size(38.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.AccountBalance,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Text(
                                    text = "Category Spending Caps",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${budgets.size} active category limits set",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        OutlinedButton(
                            onClick = { showBudgetSheet = true },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                text = "Manage",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                }
            }

            // Section 4: Data Portability & Export
            item {
                Spacer(modifier = Modifier.height(10.dp))
                SectionHeader(title = "Data Management & Export")
            }

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Export Transactions (CSV)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Download or share your entire financial transaction log in universal CSV spreadsheet format.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = {
                                CoroutineScope(Dispatchers.IO).launch {
                                    val all = repository.getAllTransactions().first()
                                    val csv = DataExportUtils.generateCsv(all)
                                    CoroutineScope(Dispatchers.Main).launch {
                                        DataExportUtils.shareCsv(context, csv)
                                        Toast.makeText(context, "Exporting ${all.size} transactions...", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.FileDownload,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Export Records to CSV",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Section 5: App Identity & About
            item {
                Spacer(modifier = Modifier.height(20.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                                shape = RoundedCornerShape(16.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_launcher_foreground),
                            contentDescription = "Logo",
                            modifier = Modifier.size(48.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Budget Tracker",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Version 1.0.0 • Developed by Vineeth Sampathirao",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "100% Offline & Private • Encrypted on Device",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "All financial records stay strictly on your device.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }

    if (showBudgetSheet) {
        SetBudgetSheet(
            categories = expenseCategories,
            budgets = budgets,
            currency = currentCurrency,
            onDismiss = { showBudgetSheet = false },
            onSaveBudget = { catId, amountCents ->
                CoroutineScope(Dispatchers.IO).launch {
                    repository.setBudget(catId, amountCents, currentCurrency.code)
                }
                showBudgetSheet = false
            },
            onDeleteBudget = { catId ->
                CoroutineScope(Dispatchers.IO).launch {
                    repository.deleteBudgetForCategory(catId)
                }
                showBudgetSheet = false
            }
        )
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
    )
}
