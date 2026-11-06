package com.budgettracker.app.ui.yearly

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.budgettracker.app.data.model.AppCurrency
import com.budgettracker.app.data.model.CategoryExpenseSummary
import com.budgettracker.app.data.model.MonthlyExpenseSummary
import com.budgettracker.app.data.model.TransactionType
import com.budgettracker.app.data.model.TransactionWithCategory
import com.budgettracker.app.data.repository.TransactionRepository
import com.budgettracker.app.util.CurrencyExchangeRates
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate

data class YearlyUiState(
    val selectedYear: Int = LocalDate.now().year,
    val transactions: List<TransactionWithCategory> = emptyList(),
    val availableYears: List<Int> = listOf(LocalDate.now().year),
    val viewingCurrency: AppCurrency = AppCurrency.getDefault()
) {
    val totalExpense: Long
        get() = transactions
            .filter { it.transaction.type == TransactionType.EXPENSE }
            .sumOf { CurrencyExchangeRates.convert(it.transaction.amount, it.transaction.currency, viewingCurrency) }

    val totalIncome: Long
        get() = transactions
            .filter { it.transaction.type == TransactionType.INCOME }
            .sumOf { CurrencyExchangeRates.convert(it.transaction.amount, it.transaction.currency, viewingCurrency) }

    val netSavings: Long
        get() = totalIncome - totalExpense

    val averageMonthlyExpense: Long
        get() = totalExpense / 12L

    val averageMonthlyIncome: Long
        get() = totalIncome / 12L

    val monthlySummaries: List<MonthlyExpenseSummary>
        get() {
            val byMonth = transactions.groupBy { it.transaction.month }
            return (1..12).map { month ->
                val monthList = byMonth[month] ?: emptyList()
                val expense = monthList.filter { it.transaction.type == TransactionType.EXPENSE }
                    .sumOf { CurrencyExchangeRates.convert(it.transaction.amount, it.transaction.currency, viewingCurrency) }
                val income = monthList.filter { it.transaction.type == TransactionType.INCOME }
                    .sumOf { CurrencyExchangeRates.convert(it.transaction.amount, it.transaction.currency, viewingCurrency) }
                MonthlyExpenseSummary(month = month, totalExpense = expense, totalIncome = income)
            }
        }

    val peakSpendingMonth: MonthlyExpenseSummary?
        get() = monthlySummaries.maxByOrNull { it.totalExpense }?.takeIf { it.totalExpense > 0L }

    val categorySummaries: List<CategoryExpenseSummary>
        get() {
            return transactions
                .filter { it.transaction.type == TransactionType.EXPENSE }
                .groupBy { it.category }
                .mapNotNull { (category, txList) ->
                    if (category == null) null
                    else {
                        val sum = txList.sumOf {
                            CurrencyExchangeRates.convert(it.transaction.amount, it.transaction.currency, viewingCurrency)
                        }
                        CategoryExpenseSummary(
                            categoryId = category.id,
                            categoryName = category.name,
                            iconName = category.iconName,
                            colorHex = category.colorHex,
                            totalAmount = sum,
                            transactionCount = txList.size
                        )
                    }
                }
                .sortedByDescending { it.totalAmount }
        }
}

@OptIn(ExperimentalCoroutinesApi::class)
class YearlyExpenseViewModel(
    private val repository: TransactionRepository
) : ViewModel() {

    private val currentYear = LocalDate.now().year
    private val _selectedYear = MutableStateFlow(currentYear)
    private val _viewingCurrency = MutableStateFlow(AppCurrency.getDefault())

    val uiState: StateFlow<YearlyUiState> = combine(_selectedYear, _viewingCurrency) { yr, cur ->
        yr to cur
    }.flatMapLatest { (year, currency) ->
        combine(
            repository.getTransactionsForYear(year),
            repository.getAvailableYears()
        ) { transactions, years ->
            val allYears = (years + listOf(currentYear, year)).distinct().sortedDescending()
            YearlyUiState(
                selectedYear = year,
                transactions = transactions,
                availableYears = allYears,
                viewingCurrency = currency
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = YearlyUiState()
    )

    fun setViewingCurrency(currency: AppCurrency) {
        _viewingCurrency.value = currency
    }

    fun selectYear(year: Int) {
        _selectedYear.value = year
    }

    fun previousYear() {
        _selectedYear.value = _selectedYear.value - 1
    }

    fun nextYear() {
        _selectedYear.value = _selectedYear.value + 1
    }

    fun resetToCurrentYear() {
        _selectedYear.value = LocalDate.now().year
    }
}

class YearlyExpenseViewModelFactory(
    private val repository: TransactionRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(YearlyExpenseViewModel::class.java)) {
            return YearlyExpenseViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
