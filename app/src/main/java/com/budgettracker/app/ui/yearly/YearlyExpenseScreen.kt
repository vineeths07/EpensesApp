package com.budgettracker.app.ui.yearly

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.budgettracker.app.data.model.AppCurrency
import com.budgettracker.app.ui.components.AppHeader
import com.budgettracker.app.ui.monthly.CategoryBreakdownCard
import com.budgettracker.app.ui.security.BiometricSecurityCard
import java.time.LocalDate

@Composable
fun YearlyExpenseScreen(
    viewModel: YearlyExpenseViewModel,
    currency: AppCurrency = AppCurrency.USD,
    onCurrencySelected: (AppCurrency) -> Unit = {},
    isBiometricEnabled: Boolean = false,
    onBiometricToggle: (Boolean) -> Unit = {},
    isPrivacyMode: Boolean = false,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val currentYear = remember { LocalDate.now().year }
    val isCurrentYear = state.selectedYear == currentYear

    var showYearDropdown by remember { mutableStateOf(false) }

    Scaffold(modifier = modifier.fillMaxSize()) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Top App Bar with App Icon, Title & Currency Selector
            item {
                AppHeader(
                    title = "Yearly Analytics",
                    subtitle = "Annual trends & metrics",
                    currency = currency,
                    onCurrencySelected = onCurrencySelected,
                    detailedDescription = "Inspect multi-year financial trends, compare 12-month spending patterns, and review annual category rankings."
                )
            }

            // Year Stepper Row
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(onClick = { viewModel.previousYear() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBackIosNew,
                            contentDescription = "Previous Year",
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Box {
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { showYearDropdown = true }
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            color = MaterialTheme.colorScheme.surface
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = state.selectedYear.toString(),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = showYearDropdown,
                            onDismissRequest = { showYearDropdown = false }
                        ) {
                            state.availableYears.forEach { yr ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = yr.toString(),
                                            fontWeight = if (yr == state.selectedYear) FontWeight.Bold else FontWeight.Normal,
                                            color = if (yr == state.selectedYear) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                        )
                                    },
                                    onClick = {
                                        viewModel.selectYear(yr)
                                        showYearDropdown = false
                                    }
                                )
                            }
                        }
                    }

                    IconButton(onClick = { viewModel.nextYear() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                            contentDescription = "Next Year",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Jump to current year shortcut
            if (!isCurrentYear) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        AssistChip(
                            onClick = { viewModel.resetToCurrentYear() },
                            label = { Text("Jump to $currentYear") },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Today,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer
                            )
                        )
                    }
                }
            }

            // Annual Summary Card
            item {
                YearlySummaryCard(
                    totalExpense = state.totalExpense,
                    totalIncome = state.totalIncome,
                    netSavings = state.netSavings,
                    averageMonthlyExpense = state.averageMonthlyExpense,
                    peakMonth = state.peakSpendingMonth,
                    currency = currency,
                    isPrivacyMode = isPrivacyMode
                )
            }

            // 12-Month Bar Chart
            item {
                YearlyBarChart(
                    monthlySummaries = state.monthlySummaries,
                    currency = currency,
                    isPrivacyMode = isPrivacyMode
                )
            }

            // Annual Category Breakdown
            item {
                CategoryBreakdownCard(
                    summaries = state.categorySummaries,
                    totalExpense = state.totalExpense,
                    currency = currency,
                    isPrivacyMode = isPrivacyMode
                )
            }

            // Biometric App Security Card (Available in Yearly view as well)
            item {
                BiometricSecurityCard(
                    isBiometricEnabled = isBiometricEnabled,
                    onBiometricToggle = onBiometricToggle
                )
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}
