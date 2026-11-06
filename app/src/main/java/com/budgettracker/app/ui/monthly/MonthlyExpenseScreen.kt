package com.budgettracker.app.ui.monthly

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.budgettracker.app.data.model.AppCurrency
import com.budgettracker.app.data.model.TransactionEntity
import com.budgettracker.app.ui.components.AppHeader
import com.budgettracker.app.ui.components.TransactionItem
import com.budgettracker.app.ui.security.BiometricSecurityCard
import com.budgettracker.app.util.DateUtils
import kotlinx.coroutines.launch
import java.time.LocalDate

@Composable
fun MonthlyExpenseScreen(
    viewModel: MonthlyExpenseViewModel,
    onDeleteTransaction: (TransactionEntity) -> Unit = {},
    onRestoreTransaction: (TransactionEntity) -> Unit = {},
    currency: AppCurrency = AppCurrency.USD,
    onCurrencySelected: (AppCurrency) -> Unit = {},
    isBiometricEnabled: Boolean = false,
    onBiometricToggle: (Boolean) -> Unit = {},
    isPrivacyMode: Boolean = false,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    var showPickerDialog by remember { mutableStateOf(false) }
    var showSetBudgetSheet by remember { mutableStateOf(false) }
    var selectedCategoryForBudget by remember { mutableStateOf<Long?>(null) }
    var editingItem by remember { mutableStateOf<com.budgettracker.app.data.model.TransactionWithCategory?>(null) }

    val currentNow = remember { LocalDate.now() }
    val isCurrentMonth = state.selectedYear == currentNow.year && state.selectedMonth == currentNow.monthValue

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    val bounceTransition = rememberInfiniteTransition(label = "bounce")
    val bounceOffset by bounceTransition.animateFloat(
        initialValue = 0f,
        targetValue = -15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "icon_bounce"
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Top App Bar with App Icon, Title & Currency Selector
            item {
                AppHeader(
                    title = "Monthly Overview",
                    subtitle = "Monthly cash flow & budgets",
                    currency = currency,
                    onCurrencySelected = onCurrencySelected,
                    detailedDescription = "Analyze monthly expenditures, monitor category spending caps with live overspend alerts, and review savings rate health."
                )
            }

            // Month Navigation Row
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(onClick = { viewModel.previousMonth() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBackIosNew,
                            contentDescription = "Previous Month",
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { showPickerDialog = true }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = DateUtils.formatMonthYear(state.selectedYear, state.selectedMonth),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    IconButton(onClick = { viewModel.nextMonth() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                            contentDescription = "Next Month",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Jump to current month shortcut
            if (!isCurrentMonth) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        AssistChip(
                            onClick = { viewModel.resetToCurrentMonth() },
                            label = { Text("Jump to Current Month") },
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

            // Financial Summary Card
            item {
                MonthlySummaryCard(
                    totalExpense = state.totalExpense,
                    totalIncome = state.totalIncome,
                    netSavings = state.netSavings,
                    savingsRate = state.savingsRate,
                    totalBudget = state.totalBudget,
                    isBudgetConfigured = state.isBudgetConfigured,
                    overBudgetCount = state.overBudgetCount,
                    currency = currency,
                    isPrivacyMode = isPrivacyMode
                )
            }

            // Category Breakdown & Budgets Card
            item {
                CategoryBreakdownCard(
                    budgetProgressList = state.categoryBudgetProgressList,
                    totalExpense = state.totalExpense,
                    currency = currency,
                    isPrivacyMode = isPrivacyMode,
                    onOpenSetBudget = { catId ->
                        selectedCategoryForBudget = catId
                        showSetBudgetSheet = true
                    }
                )
            }

            // Biometric App Security Card (Integrated natively in the flow)
            item {
                BiometricSecurityCard(
                    isBiometricEnabled = isBiometricEnabled,
                    onBiometricToggle = onBiometricToggle
                )
            }

            // Monthly Transactions Section Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Transactions this Month",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${state.transactions.size} records",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Monthly Transactions List
            if (state.transactions.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .offset(y = bounceOffset.dp)
                                    .size(72.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ReceiptLong,
                                    contentDescription = null,
                                    modifier = Modifier.size(36.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No transactions logged this month",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            } else {
                items(
                    items = state.transactions,
                    key = { it.transaction.id }
                ) { item ->
                    TransactionItem(
                        item = item,
                        onDelete = { tx ->
                            onDeleteTransaction(tx)
                            coroutineScope.launch {
                                snackbarHostState.currentSnackbarData?.dismiss()
                                val result = snackbarHostState.showSnackbar(
                                    message = "Deleted \"${tx.title}\"",
                                    actionLabel = "Undo",
                                    duration = SnackbarDuration.Short
                                )
                                if (result == SnackbarResult.ActionPerformed) {
                                    onRestoreTransaction(tx)
                                }
                            }
                        },
                        onEdit = { editingItem = it },
                        currency = currency,
                        isPrivacyMode = isPrivacyMode,
                        modifier = Modifier.animateItem()
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }

        if (showPickerDialog) {
            MonthYearPickerDialog(
                initialYear = state.selectedYear,
                initialMonth = state.selectedMonth,
                onDismiss = { showPickerDialog = false },
                onConfirm = { year, month ->
                    viewModel.selectMonth(year, month)
                    showPickerDialog = false
                }
            )
        }

        if (showSetBudgetSheet) {
            SetBudgetSheet(
                categories = state.allExpenseCategories,
                budgets = state.budgets,
                currency = currency,
                initialCategoryId = selectedCategoryForBudget,
                onDismiss = { showSetBudgetSheet = false },
                onSaveBudget = { catId, amountCents ->
                    viewModel.setBudget(catId, amountCents, currency)
                    showSetBudgetSheet = false
                },
                onDeleteBudget = { catId ->
                    viewModel.deleteBudget(catId)
                    showSetBudgetSheet = false
                }
            )
        }

        // Edit Transaction Sheet
        editingItem?.let { item ->
            com.budgettracker.app.ui.components.EditTransactionSheet(
                item = item,
                categories = state.allExpenseCategories,
                currency = currency,
                onDismiss = { editingItem = null },
                onSave = { updated ->
                    viewModel.updateTransaction(updated)
                    editingItem = null
                }
            )
        }
    }
}
