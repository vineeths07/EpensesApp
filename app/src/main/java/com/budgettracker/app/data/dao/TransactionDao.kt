package com.budgettracker.app.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.budgettracker.app.data.model.CategoryExpenseSummary
import com.budgettracker.app.data.model.MonthlyExpenseSummary
import com.budgettracker.app.data.model.TransactionEntity
import com.budgettracker.app.data.model.TransactionType
import com.budgettracker.app.data.model.TransactionWithCategory
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface TransactionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaction: TransactionEntity): Long

    @Update
    suspend fun update(transaction: TransactionEntity)

    @Delete
    suspend fun delete(transaction: TransactionEntity)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("UPDATE transactions SET categoryId = 11 WHERE categoryId NOT IN (SELECT id FROM categories)")
    suspend fun remapOrphanedTransactions()

    @Transaction
    @Query("SELECT * FROM transactions WHERE id = :id LIMIT 1")
    fun getTransactionById(id: Long): Flow<TransactionWithCategory?>

    @Transaction
    @Query("SELECT * FROM transactions ORDER BY date DESC, createdAtMillis DESC")
    fun getAllTransactions(): Flow<List<TransactionWithCategory>>

    @Transaction
    @Query("SELECT * FROM transactions ORDER BY date DESC, createdAtMillis DESC LIMIT :limit")
    fun getRecentTransactions(limit: Int = 20): Flow<List<TransactionWithCategory>>

    @Transaction
    @Query("SELECT * FROM transactions WHERE date = :date ORDER BY createdAtMillis DESC")
    fun getTransactionsForDate(date: LocalDate): Flow<List<TransactionWithCategory>>

    @Transaction
    @Query("SELECT * FROM transactions WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC, createdAtMillis DESC")
    fun getTransactionsBetween(startDate: LocalDate, endDate: LocalDate): Flow<List<TransactionWithCategory>>

    @Transaction
    @Query("SELECT * FROM transactions WHERE year = :year AND month = :month ORDER BY date DESC, createdAtMillis DESC")
    fun getTransactionsForMonth(year: Int, month: Int): Flow<List<TransactionWithCategory>>

    @Transaction
    @Query("SELECT * FROM transactions WHERE year = :year ORDER BY date DESC, createdAtMillis DESC")
    fun getTransactionsForYear(year: Int): Flow<List<TransactionWithCategory>>

    @Query("SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE type = :type AND date = :date")
    fun getTotalAmountForDate(type: TransactionType, date: LocalDate): Flow<Long>

    @Query("SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE type = :type AND year = :year AND month = :month")
    fun getTotalAmountForMonth(type: TransactionType, year: Int, month: Int): Flow<Long>

    @Query("SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE type = :type AND year = :year")
    fun getTotalAmountForYear(type: TransactionType, year: Int): Flow<Long>

    @Query("SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE type = :type AND date BETWEEN :startDate AND :endDate")
    fun getTotalAmountBetween(type: TransactionType, startDate: LocalDate, endDate: LocalDate): Flow<Long>

    @Query("""
        SELECT 
            c.id AS categoryId,
            c.name AS categoryName,
            c.iconName AS iconName,
            c.colorHex AS colorHex,
            COALESCE(SUM(t.amount), 0) AS totalAmount,
            COUNT(t.id) AS transactionCount
        FROM categories c
        INNER JOIN transactions t ON c.id = t.categoryId
        WHERE t.type = :type AND t.date BETWEEN :startDate AND :endDate
        GROUP BY c.id
        ORDER BY totalAmount DESC
    """)
    fun getCategorySummariesBetween(
        type: TransactionType,
        startDate: LocalDate,
        endDate: LocalDate
    ): Flow<List<CategoryExpenseSummary>>

    @Query("""
        SELECT 
            c.id AS categoryId,
            c.name AS categoryName,
            c.iconName AS iconName,
            c.colorHex AS colorHex,
            COALESCE(SUM(t.amount), 0) AS totalAmount,
            COUNT(t.id) AS transactionCount
        FROM categories c
        INNER JOIN transactions t ON c.id = t.categoryId
        WHERE t.type = :type AND t.year = :year AND t.month = :month
        GROUP BY c.id
        ORDER BY totalAmount DESC
    """)
    fun getCategorySummariesForMonth(
        type: TransactionType,
        year: Int,
        month: Int
    ): Flow<List<CategoryExpenseSummary>>

    @Query("""
        SELECT 
            c.id AS categoryId,
            c.name AS categoryName,
            c.iconName AS iconName,
            c.colorHex AS colorHex,
            COALESCE(SUM(t.amount), 0) AS totalAmount,
            COUNT(t.id) AS transactionCount
        FROM categories c
        INNER JOIN transactions t ON c.id = t.categoryId
        WHERE t.type = :type AND t.year = :year
        GROUP BY c.id
        ORDER BY totalAmount DESC
    """)
    fun getCategorySummariesForYear(
        type: TransactionType,
        year: Int
    ): Flow<List<CategoryExpenseSummary>>

    @Query("""
        SELECT 
            month,
            COALESCE(SUM(CASE WHEN type = 'EXPENSE' THEN amount ELSE 0 END), 0) AS totalExpense,
            COALESCE(SUM(CASE WHEN type = 'INCOME' THEN amount ELSE 0 END), 0) AS totalIncome
        FROM transactions
        WHERE year = :year
        GROUP BY month
        ORDER BY month ASC
    """)
    fun getMonthlySummariesForYear(year: Int): Flow<List<MonthlyExpenseSummary>>

    @Query("SELECT DISTINCT year FROM transactions ORDER BY year DESC")
    fun getAvailableYears(): Flow<List<Int>>
}
