package com.budgettracker.app.ui.daily

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.budgettracker.app.data.model.AppCurrency
import com.budgettracker.app.data.model.CategoryEntity
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

data class DailyUiState(
    val selectedDate: LocalDate = LocalDate.now(),
    val transactions: List<TransactionWithCategory> = emptyList(),
    val availableCategories: List<CategoryEntity> = emptyList(),
    val isLoading: Boolean = false,
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

    val netBalance: Long
        get() = totalIncome - totalExpense
}

@OptIn(ExperimentalCoroutinesApi::class)
class DailyExpenseViewModel(
    private val repository: TransactionRepository
) : ViewModel() {

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    private val _viewingCurrency = MutableStateFlow(AppCurrency.getDefault())

    val uiState: StateFlow<DailyUiState> = combine(_selectedDate, _viewingCurrency) { date, currency ->
        date to currency
    }.flatMapLatest { (date, currency) ->
        combine(
            repository.getTransactionsForDate(date),
            repository.getAllCategories()
        ) { transactions, categories ->
            DailyUiState(
                selectedDate = date,
                transactions = transactions,
                availableCategories = categories,
                isLoading = false,
                viewingCurrency = currency
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DailyUiState()
    )

    init {
        viewModelScope.launch {
            repository.ensureDefaultCategories()
        }
    }

    fun setViewingCurrency(currency: AppCurrency) {
        _viewingCurrency.value = currency
    }

    fun selectDate(date: LocalDate) {
        _selectedDate.value = date
    }

    fun previousDay() {
        _selectedDate.value = _selectedDate.value.minusDays(1)
    }

    fun nextDay() {
        _selectedDate.value = _selectedDate.value.plusDays(1)
    }

    fun resetToToday() {
        _selectedDate.value = LocalDate.now()
    }

    fun addTransaction(
        title: String,
        amountCents: Long,
        type: TransactionType,
        categoryId: Long,
        date: LocalDate,
        note: String = "",
        currency: AppCurrency = _viewingCurrency.value
    ) {
        viewModelScope.launch {
            repository.insertTransaction(
                title = title,
                amount = amountCents,
                type = type,
                categoryId = categoryId,
                date = date,
                note = note,
                currencyCode = currency.code
            )
        }
    }

    fun deleteTransaction(id: Long) {
        viewModelScope.launch {
            repository.deleteTransactionById(id)
        }
    }

    fun deleteTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            repository.deleteTransaction(transaction)
        }
    }

    fun restoreTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            repository.restoreTransaction(transaction)
        }
    }

    fun updateTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            repository.updateTransaction(transaction)
        }
    }
}

class DailyExpenseViewModelFactory(
    private val repository: TransactionRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DailyExpenseViewModel::class.java)) {
            return DailyExpenseViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
