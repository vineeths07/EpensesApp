package com.budgettracker.app.data.repository

import com.budgettracker.app.data.model.CategoryEntity
import com.budgettracker.app.data.model.CategoryExpenseSummary
import com.budgettracker.app.data.model.MonthlyExpenseSummary
import com.budgettracker.app.data.model.TransactionEntity
import com.budgettracker.app.data.model.TransactionType
import com.budgettracker.app.data.model.TransactionWithCategory
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface TransactionRepository {
    suspend fun insertTransaction(
        title: String,
        amount: Long,
        type: TransactionType,
        categoryId: Long,
        date: LocalDate,
        note: String = "",
        currencyCode: String = com.budgettracker.app.data.model.AppCurrency.getDefault().code
    ): Long

    suspend fun updateTransaction(transaction: TransactionEntity)

    suspend fun deleteTransaction(transaction: TransactionEntity)

    suspend fun deleteTransactionById(id: Long)

    suspend fun restoreTransaction(transaction: TransactionEntity): Long

    fun getTransactionById(id: Long): Flow<TransactionWithCategory?>

    fun getAllTransactions(): Flow<List<TransactionWithCategory>>

    fun getRecentTransactions(limit: Int = 20): Flow<List<TransactionWithCategory>>

    fun getTransactionsForDate(date: LocalDate): Flow<List<TransactionWithCategory>>

    fun getTransactionsBetween(startDate: LocalDate, endDate: LocalDate): Flow<List<TransactionWithCategory>>

    fun getTransactionsForMonth(year: Int, month: Int): Flow<List<TransactionWithCategory>>

    fun getTransactionsForYear(year: Int): Flow<List<TransactionWithCategory>>

    fun getTotalAmountForDate(type: TransactionType, date: LocalDate): Flow<Long>

    fun getTotalAmountForMonth(type: TransactionType, year: Int, month: Int): Flow<Long>

    fun getTotalAmountForYear(type: TransactionType, year: Int): Flow<Long>

    fun getCategorySummariesForMonth(
        type: TransactionType,
        year: Int,
        month: Int
    ): Flow<List<CategoryExpenseSummary>>

    fun getCategorySummariesForYear(
        type: TransactionType,
        year: Int
    ): Flow<List<CategoryExpenseSummary>>

    fun getMonthlySummariesForYear(year: Int): Flow<List<MonthlyExpenseSummary>>

    fun getAvailableYears(): Flow<List<Int>>

    fun getAllCategories(): Flow<List<CategoryEntity>>

    fun getCategoriesByType(type: TransactionType): Flow<List<CategoryEntity>>

    suspend fun insertCategory(category: CategoryEntity): Long

    suspend fun ensureDefaultCategories()

    fun getAllBudgets(): Flow<List<com.budgettracker.app.data.model.BudgetEntity>>

    fun getBudgetForCategory(categoryId: Long): Flow<com.budgettracker.app.data.model.BudgetEntity?>

    suspend fun setBudget(categoryId: Long, monthlyLimit: Long, currencyCode: String): Long

    suspend fun deleteBudgetForCategory(categoryId: Long)
}
