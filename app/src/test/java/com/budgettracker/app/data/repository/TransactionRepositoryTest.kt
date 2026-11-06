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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

class FakeTransactionDao : TransactionDao {
    val transactions = mutableListOf<TransactionEntity>()

    override suspend fun insert(transaction: TransactionEntity): Long {
        val id = (transactions.size + 1).toLong()
        transactions.add(transaction.copy(id = id))
        return id
    }

    override suspend fun update(transaction: TransactionEntity) {
        val index = transactions.indexOfFirst { it.id == transaction.id }
        if (index != -1) {
            transactions[index] = transaction
        }
    }

    override suspend fun delete(transaction: TransactionEntity) {
        transactions.removeAll { it.id == transaction.id }
    }

    override suspend fun deleteById(id: Long) {
        transactions.removeAll { it.id == id }
    }

    override suspend fun remapOrphanedTransactions() {
        // No-op for fake test dao
    }

    override fun getTransactionById(id: Long): Flow<TransactionWithCategory?> {
        val found = transactions.find { it.id == id }?.let {
            TransactionWithCategory(transaction = it, category = null)
        }
        return flowOf(found)
    }

    override fun getAllTransactions(): Flow<List<TransactionWithCategory>> {
        return flowOf(transactions.map { TransactionWithCategory(transaction = it, category = null) })
    }

    override fun getRecentTransactions(limit: Int): Flow<List<TransactionWithCategory>> {
        return flowOf(transactions.take(limit).map { TransactionWithCategory(transaction = it, category = null) })
    }

    override fun getTransactionsForDate(date: LocalDate): Flow<List<TransactionWithCategory>> {
        return flowOf(transactions.filter { it.date == date }.map { TransactionWithCategory(it, null) })
    }

    override fun getTransactionsBetween(startDate: LocalDate, endDate: LocalDate): Flow<List<TransactionWithCategory>> {
        return flowOf(transactions.filter { !it.date.isBefore(startDate) && !it.date.isAfter(endDate) }.map { TransactionWithCategory(it, null) })
    }

    override fun getTransactionsForMonth(year: Int, month: Int): Flow<List<TransactionWithCategory>> {
        return flowOf(transactions.filter { it.year == year && it.month == month }.map { TransactionWithCategory(it, null) })
    }

    override fun getTransactionsForYear(year: Int): Flow<List<TransactionWithCategory>> {
        return flowOf(transactions.filter { it.year == year }.map { TransactionWithCategory(it, null) })
    }

    override fun getTotalAmountForDate(type: TransactionType, date: LocalDate): Flow<Long> {
        val total = transactions.filter { it.type == type && it.date == date }.sumOf { it.amount }
        return flowOf(total)
    }

    override fun getTotalAmountForMonth(type: TransactionType, year: Int, month: Int): Flow<Long> {
        val total = transactions.filter { it.type == type && it.year == year && it.month == month }.sumOf { it.amount }
        return flowOf(total)
    }

    override fun getTotalAmountForYear(type: TransactionType, year: Int): Flow<Long> {
        val total = transactions.filter { it.type == type && it.year == year }.sumOf { it.amount }
        return flowOf(total)
    }

    override fun getTotalAmountBetween(type: TransactionType, startDate: LocalDate, endDate: LocalDate): Flow<Long> {
        val total = transactions.filter { it.type == type && !it.date.isBefore(startDate) && !it.date.isAfter(endDate) }.sumOf { it.amount }
        return flowOf(total)
    }

    override fun getCategorySummariesBetween(
        type: TransactionType,
        startDate: LocalDate,
        endDate: LocalDate
    ): Flow<List<CategoryExpenseSummary>> = flowOf(emptyList())

    override fun getCategorySummariesForMonth(
        type: TransactionType,
        year: Int,
        month: Int
    ): Flow<List<CategoryExpenseSummary>> = flowOf(emptyList())

    override fun getCategorySummariesForYear(
        type: TransactionType,
        year: Int
    ): Flow<List<CategoryExpenseSummary>> = flowOf(emptyList())

    override fun getMonthlySummariesForYear(year: Int): Flow<List<MonthlyExpenseSummary>> = flowOf(emptyList())

    override fun getAvailableYears(): Flow<List<Int>> {
        return flowOf(transactions.map { it.year }.distinct().sortedDescending())
    }
}

class FakeCategoryDao : CategoryDao {
    val categories = mutableListOf<CategoryEntity>()

    override fun getAllCategories(): Flow<List<CategoryEntity>> = flowOf(categories)

    override fun getCategoriesByType(type: TransactionType): Flow<List<CategoryEntity>> {
        return flowOf(categories.filter { it.type == type })
    }

    override suspend fun getCategoryById(id: Long): CategoryEntity? = categories.find { it.id == id }

    override suspend fun getCategoryCount(): Int = categories.size

    override suspend fun deleteDuplicateCategories() {
        val distinct = categories.distinctBy { it.name to it.type }
        categories.clear()
        categories.addAll(distinct)
    }

    override suspend fun insert(category: CategoryEntity): Long {
        val id = (categories.size + 1).toLong()
        categories.add(category.copy(id = id))
        return id
    }

    override suspend fun insertAll(categories: List<CategoryEntity>) {
        categories.forEach { insert(it) }
    }

    override suspend fun update(category: CategoryEntity) {
        val index = categories.indexOfFirst { it.id == category.id }
        if (index != -1) categories[index] = category
    }

    override suspend fun delete(category: CategoryEntity) {
        categories.removeAll { it.id == category.id }
    }
}

class FakeBudgetDao : BudgetDao {
    val budgets = mutableListOf<BudgetEntity>()

    override fun getAllBudgets(): Flow<List<BudgetEntity>> = flowOf(budgets)

    override fun getBudgetForCategory(categoryId: Long): Flow<BudgetEntity?> =
        flowOf(budgets.find { it.categoryId == categoryId })

    override suspend fun setBudget(budget: BudgetEntity): Long {
        budgets.removeAll { it.categoryId == budget.categoryId }
        val id = (budgets.size + 1).toLong()
        budgets.add(budget.copy(id = id))
        return id
    }

    override suspend fun deleteBudgetForCategory(categoryId: Long) {
        budgets.removeAll { it.categoryId == categoryId }
    }

    override suspend fun deleteBudgetById(id: Long) {
        budgets.removeAll { it.id == id }
    }
}

class TransactionRepositoryTest {
    private lateinit var fakeTransactionDao: FakeTransactionDao
    private lateinit var fakeCategoryDao: FakeCategoryDao
    private lateinit var fakeBudgetDao: FakeBudgetDao
    private lateinit var repository: TransactionRepositoryImpl

    @Before
    fun setup() {
        fakeTransactionDao = FakeTransactionDao()
        fakeCategoryDao = FakeCategoryDao()
        fakeBudgetDao = FakeBudgetDao()
        repository = TransactionRepositoryImpl(fakeTransactionDao, fakeCategoryDao, fakeBudgetDao)
    }

    @Test
    fun testInsertTransaction_automaticallyPopulatesYearAndMonth() = runTest {
        val testDate = LocalDate.of(2026, 5, 14)
        val insertedId = repository.insertTransaction(
            title = " Lunch at Cafe ",
            amount = 1450L,
            type = TransactionType.EXPENSE,
            categoryId = 1L,
            date = testDate,
            note = "Burger and juice"
        )

        assertEquals(1L, insertedId)
        val saved = fakeTransactionDao.transactions.first()
        assertEquals("Lunch at Cafe", saved.title)
        assertEquals(1450L, saved.amount)
        assertEquals(2026, saved.year)
        assertEquals(5, saved.month)
    }

    @Test
    fun testGetTotalAmountForDate_calculatesAccurately() = runTest {
        val testDate = LocalDate.of(2026, 10, 1)
        repository.insertTransaction("Coffee", 450L, TransactionType.EXPENSE, 1L, testDate)
        repository.insertTransaction("Groceries", 3200L, TransactionType.EXPENSE, 2L, testDate)
        repository.insertTransaction("Freelance", 10000L, TransactionType.INCOME, 3L, testDate)

        val totalExpense = repository.getTotalAmountForDate(TransactionType.EXPENSE, testDate).first()
        val totalIncome = repository.getTotalAmountForDate(TransactionType.INCOME, testDate).first()

        assertEquals(3650L, totalExpense)
        assertEquals(10000L, totalIncome)
    }

    @Test
    fun testEnsureDefaultCategories_populatesWhenEmpty() = runTest {
        assertEquals(0, fakeCategoryDao.getCategoryCount())
        repository.ensureDefaultCategories()
        assertEquals(true, fakeCategoryDao.getCategoryCount() > 0)
    }

    @Test
    fun testEnsureDefaultCategories_idempotentNeverDuplicates() = runTest {
        assertEquals(0, fakeCategoryDao.getCategoryCount())
        repository.ensureDefaultCategories()
        val countAfterFirst = fakeCategoryDao.getCategoryCount()

        // Call again
        repository.ensureDefaultCategories()
        val countAfterSecond = fakeCategoryDao.getCategoryCount()

        assertEquals(countAfterFirst, countAfterSecond)
        assertEquals(AppDatabase.defaultCategories.size, countAfterSecond)
    }

    @Test
    fun testDeleteAndRestoreTransaction() = runTest {
        val testDate = LocalDate.of(2026, 10, 1)
        repository.insertTransaction("Snack", 300L, TransactionType.EXPENSE, 1L, testDate)
        assertEquals(1, fakeTransactionDao.transactions.size)
        val original = fakeTransactionDao.transactions.first()

        // Delete
        repository.deleteTransaction(original)
        assertEquals(0, fakeTransactionDao.transactions.size)

        // Restore
        repository.restoreTransaction(original)
        assertEquals(1, fakeTransactionDao.transactions.size)
        assertEquals("Snack", fakeTransactionDao.transactions.first().title)
    }

    @Test
    fun testSetAndDeleteBudget() = runTest {
        repository.setBudget(categoryId = 1L, monthlyLimit = 40000L, currencyCode = "USD")
        assertEquals(1, fakeBudgetDao.budgets.size)
        val saved = fakeBudgetDao.budgets.first()
        assertEquals(1L, saved.categoryId)
        assertEquals(40000L, saved.monthlyLimit)

        // Delete budget
        repository.deleteBudgetForCategory(1L)
        assertEquals(0, fakeBudgetDao.budgets.size)
    }
}
