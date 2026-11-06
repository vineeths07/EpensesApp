package com.budgettracker.app.ui.monthly

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.budgettracker.app.data.model.AppCurrency
import com.budgettracker.app.data.model.BudgetEntity
import com.budgettracker.app.data.model.CategoryBudgetProgress
import com.budgettracker.app.data.model.CategoryEntity
import com.budgettracker.app.data.model.CategoryExpenseSummary
import com.budgettracker.app.data.model.TransactionEntity
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
import kotlinx.coroutines.launch
import java.time.LocalDate

data class MonthYear(
    val year: Int,
    val month: Int // 1-12
)

data class MonthlyUiState(
    val selectedYear: Int = LocalDate.now().year,
    val selectedMonth: Int = LocalDate.now().monthValue,
    val transactions: List<TransactionWithCategory> = emptyList(),
    val availableYears: List<Int> = listOf(LocalDate.now().year),
    val viewingCurrency: AppCurrency = AppCurrency.getDefault(),
    val budgets: List<BudgetEntity> = emptyList(),
    val allExpenseCategories: List<CategoryEntity> = emptyList()
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

    val savingsRate: Int
        get() = if (totalIncome > 0) {
            val rate = ((totalIncome - totalExpense) * 100 / totalIncome).toInt()
            rate.coerceIn(-100, 100)
        } else {
            0
        }

    val totalBudget: Long
        get() = budgets.sumOf {
            CurrencyExchangeRates.convert(it.monthlyLimit, it.currency, viewingCurrency)
        }

    val isBudgetConfigured: Boolean
        get() = totalBudget > 0L

    val remainingTotalBudget: Long
        get() = totalBudget - totalExpense

    val categoryBudgetProgressList: List<CategoryBudgetProgress>
        get() {
            val budgetsMap = budgets.associateBy { it.categoryId }
            val byCategory = transactions
                .filter { it.transaction.type == TransactionType.EXPENSE }
                .groupBy { it.transaction.categoryId }

            val activeCategoryIds = (byCategory.keys + budgetsMap.keys).distinct()
            val categoriesMap = allExpenseCategories.associateBy { it.id }

            return activeCategoryIds.mapNotNull { catId ->
                val category = categoriesMap[catId] ?: return@mapNotNull null
                val txList = byCategory[catId] ?: emptyList()
                val spent = txList.sumOf {
                    CurrencyExchangeRates.convert(it.transaction.amount, it.transaction.currency, viewingCurrency)
                }
                val budget = budgetsMap[catId]
                val limit = budget?.let {
                    CurrencyExchangeRates.convert(it.monthlyLimit, it.currency, viewingCurrency)
                }

                CategoryBudgetProgress(
                    categoryId = category.id,
                    categoryName = category.name,
                    iconName = category.iconName,
                    colorHex = category.colorHex,
                    spentAmount = spent,
                    budgetLimit = limit,
                    transactionCount = txList.size
                )
            }.sortedWith(
                compareByDescending<CategoryBudgetProgress> { it.isOverBudget }
                    .thenByDescending { it.spentAmount }
            )
        }

    val overBudgetCount: Int
        get() = categoryBudgetProgressList.count { it.isOverBudget }

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
class MonthlyExpenseViewModel(
    private val repository: TransactionRepository
) : ViewModel() {

    private val now = LocalDate.now()
    private val _selectedMonthYear = MutableStateFlow(MonthYear(now.year, now.monthValue))
    private val _viewingCurrency = MutableStateFlow(AppCurrency.getDefault())

    val uiState: StateFlow<MonthlyUiState> = combine(_selectedMonthYear, _viewingCurrency) { my, cur ->
        my to cur
    }.flatMapLatest { (monthYear, currency) ->
        val (year, month) = monthYear
        combine(
            repository.getTransactionsForMonth(year, month),
            repository.getAvailableYears(),
            repository.getAllBudgets(),
            repository.getCategoriesByType(TransactionType.EXPENSE)
        ) { transactions, years, budgets, expenseCategories ->
            val allYears = (years + listOf(now.year, year)).distinct().sortedDescending()
            MonthlyUiState(
                selectedYear = year,
                selectedMonth = month,
                transactions = transactions,
                availableYears = allYears,
                viewingCurrency = currency,
                budgets = budgets,
                allExpenseCategories = expenseCategories
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MonthlyUiState()
    )

    fun setViewingCurrency(currency: AppCurrency) {
        _viewingCurrency.value = currency
    }

    fun selectMonth(year: Int, month: Int) {
        _selectedMonthYear.value = MonthYear(year, month)
    }

    fun previousMonth() {
        val current = _selectedMonthYear.value
        val newDate = LocalDate.of(current.year, current.month, 1).minusMonths(1)
        _selectedMonthYear.value = MonthYear(newDate.year, newDate.monthValue)
    }

    fun nextMonth() {
        val current = _selectedMonthYear.value
        val newDate = LocalDate.of(current.year, current.month, 1).plusMonths(1)
        _selectedMonthYear.value = MonthYear(newDate.year, newDate.monthValue)
    }

    fun resetToCurrentMonth() {
        val now = LocalDate.now()
        _selectedMonthYear.value = MonthYear(now.year, now.monthValue)
    }

    fun setBudget(categoryId: Long, amountCents: Long, currency: AppCurrency = _viewingCurrency.value) {
        viewModelScope.launch {
            repository.setBudget(categoryId, amountCents, currency.code)
        }
    }

    fun deleteBudget(categoryId: Long) {
        viewModelScope.launch {
            repository.deleteBudgetForCategory(categoryId)
        }
    }

    fun updateTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            repository.updateTransaction(transaction)
        }
    }
}

class MonthlyExpenseViewModelFactory(
    private val repository: TransactionRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MonthlyExpenseViewModel::class.java)) {
            return MonthlyExpenseViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
