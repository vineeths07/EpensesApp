package com.budgettracker.app.data.repository

import com.budgettracker.app.data.dao.BudgetDao
import com.budgettracker.app.data.dao.CategoryDao
import com.budgettracker.app.data.dao.TransactionDao
import com.budgettracker.app.data.database.AppDatabase
import com.budgettracker.app.data.model.BudgetEntity
import com.budgettracker.app.data.model.CategoryEntity
import com.budgettracker.app.data.model.CategoryExpenseSummary
import com.budgettracker.app.data.model.MonthlyExpenseSummary
import com.budgettracker.app.data.model.TransactionEntity
import com.budgettracker.app.data.model.TransactionType
import com.budgettracker.app.data.model.TransactionWithCategory
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.time.LocalDate

class TransactionRepositoryImpl(
    private val transactionDao: TransactionDao,
    private val categoryDao: CategoryDao,
    private val budgetDao: BudgetDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : TransactionRepository {

    override suspend fun insertTransaction(
        title: String,
        amount: Long,
        type: TransactionType,
        categoryId: Long,
        date: LocalDate,
        note: String,
        currencyCode: String
    ): Long = withContext(ioDispatcher) {
        val entity = TransactionEntity(
            title = title.trim(),
            amount = amount,
            type = type,
            categoryId = categoryId,
            date = date,
            year = date.year,
            month = date.monthValue,
            note = note.trim(),
            currencyCode = currencyCode
        )
        transactionDao.insert(entity)
    }

    override suspend fun updateTransaction(transaction: TransactionEntity) = withContext(ioDispatcher) {
        transactionDao.update(
            transaction.copy(
                year = transaction.date.year,
                month = transaction.date.monthValue
            )
        )
    }

    override suspend fun deleteTransaction(transaction: TransactionEntity) = withContext(ioDispatcher) {
        transactionDao.delete(transaction)
    }

    override suspend fun deleteTransactionById(id: Long) = withContext(ioDispatcher) {
        transactionDao.deleteById(id)
    }

    override suspend fun restoreTransaction(transaction: TransactionEntity): Long = withContext(ioDispatcher) {
        transactionDao.insert(transaction)
    }

    override fun getTransactionById(id: Long): Flow<TransactionWithCategory?> {
        return transactionDao.getTransactionById(id)
    }

    override fun getAllTransactions(): Flow<List<TransactionWithCategory>> {
        return transactionDao.getAllTransactions()
    }

    override fun getRecentTransactions(limit: Int): Flow<List<TransactionWithCategory>> {
        return transactionDao.getRecentTransactions(limit)
    }

    override fun getTransactionsForDate(date: LocalDate): Flow<List<TransactionWithCategory>> {
        return transactionDao.getTransactionsForDate(date)
    }

    override fun getTransactionsBetween(
        startDate: LocalDate,
        endDate: LocalDate
    ): Flow<List<TransactionWithCategory>> {
        return transactionDao.getTransactionsBetween(startDate, endDate)
    }

    override fun getTransactionsForMonth(year: Int, month: Int): Flow<List<TransactionWithCategory>> {
        return transactionDao.getTransactionsForMonth(year, month)
    }

    override fun getTransactionsForYear(year: Int): Flow<List<TransactionWithCategory>> {
        return transactionDao.getTransactionsForYear(year)
    }

    override fun getTotalAmountForDate(type: TransactionType, date: LocalDate): Flow<Long> {
        return transactionDao.getTotalAmountForDate(type, date)
    }

    override fun getTotalAmountForMonth(type: TransactionType, year: Int, month: Int): Flow<Long> {
        return transactionDao.getTotalAmountForMonth(type, year, month)
    }

    override fun getTotalAmountForYear(type: TransactionType, year: Int): Flow<Long> {
        return transactionDao.getTotalAmountForYear(type, year)
    }

    override fun getCategorySummariesForMonth(
        type: TransactionType,
        year: Int,
        month: Int
    ): Flow<List<CategoryExpenseSummary>> {
        return transactionDao.getCategorySummariesForMonth(type, year, month)
    }

    override fun getCategorySummariesForYear(
        type: TransactionType,
        year: Int
    ): Flow<List<CategoryExpenseSummary>> {
        return transactionDao.getCategorySummariesForYear(type, year)
    }

    override fun getMonthlySummariesForYear(year: Int): Flow<List<MonthlyExpenseSummary>> {
        return transactionDao.getMonthlySummariesForYear(year)
    }

    override fun getAvailableYears(): Flow<List<Int>> {
        return transactionDao.getAvailableYears()
    }

    override fun getAllCategories(): Flow<List<CategoryEntity>> {
        return categoryDao.getAllCategories()
    }

    override fun getCategoriesByType(type: TransactionType): Flow<List<CategoryEntity>> {
        return categoryDao.getCategoriesByType(type)
    }

    override suspend fun insertCategory(category: CategoryEntity): Long = withContext(ioDispatcher) {
        categoryDao.insert(category)
    }

    override suspend fun ensureDefaultCategories() = withContext(ioDispatcher) {
        if (categoryDao.getCategoryCount() == 0) {
            categoryDao.insertAll(AppDatabase.defaultCategories)
        }
        categoryDao.deleteDuplicateCategories()
        transactionDao.remapOrphanedTransactions()
    }

    override fun getAllBudgets(): Flow<List<BudgetEntity>> = budgetDao.getAllBudgets()

    override fun getBudgetForCategory(categoryId: Long): Flow<BudgetEntity?> = budgetDao.getBudgetForCategory(categoryId)

    override suspend fun setBudget(categoryId: Long, monthlyLimit: Long, currencyCode: String): Long = withContext(ioDispatcher) {
        val entity = BudgetEntity(
            categoryId = categoryId,
            monthlyLimit = monthlyLimit,
            currencyCode = currencyCode
        )
        budgetDao.setBudget(entity)
    }

    override suspend fun deleteBudgetForCategory(categoryId: Long) = withContext(ioDispatcher) {
        budgetDao.deleteBudgetForCategory(categoryId)
    }
}
