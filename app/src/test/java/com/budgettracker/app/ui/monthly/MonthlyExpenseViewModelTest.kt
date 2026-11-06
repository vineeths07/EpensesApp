package com.budgettracker.app.ui.monthly

import com.budgettracker.app.data.model.AppCurrency
import com.budgettracker.app.data.model.BudgetEntity
import com.budgettracker.app.data.model.CategoryEntity
import com.budgettracker.app.data.model.TransactionEntity
import com.budgettracker.app.data.model.TransactionType
import com.budgettracker.app.data.model.TransactionWithCategory
import com.budgettracker.app.data.repository.FakeBudgetDao
import com.budgettracker.app.data.repository.FakeCategoryDao
import com.budgettracker.app.data.repository.FakeTransactionDao
import com.budgettracker.app.data.repository.TransactionRepositoryImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class MonthlyExpenseViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: TransactionRepositoryImpl
    private lateinit var viewModel: MonthlyExpenseViewModel
    private lateinit var fakeDao: FakeTransactionDao
    private lateinit var fakeCategoryDao: FakeCategoryDao
    private lateinit var fakeBudgetDao: FakeBudgetDao

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeDao = FakeTransactionDao()
        fakeCategoryDao = FakeCategoryDao()
        fakeBudgetDao = FakeBudgetDao()
        repository = TransactionRepositoryImpl(fakeDao, fakeCategoryDao, fakeBudgetDao, ioDispatcher = testDispatcher)
        viewModel = MonthlyExpenseViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testMonthNavigation() = runTest {
        backgroundScope.launch { viewModel.uiState.collect() }

        viewModel.selectMonth(2026, 10)
        advanceUntilIdle()
        assertEquals(2026, viewModel.uiState.value.selectedYear)
        assertEquals(10, viewModel.uiState.value.selectedMonth)

        viewModel.nextMonth()
        advanceUntilIdle()
        assertEquals(2026, viewModel.uiState.value.selectedYear)
        assertEquals(11, viewModel.uiState.value.selectedMonth)

        viewModel.previousMonth()
        advanceUntilIdle()
        assertEquals(2026, viewModel.uiState.value.selectedYear)
        assertEquals(10, viewModel.uiState.value.selectedMonth)

        // Year boundary transition: Jan 2026 -> Dec 2025
        viewModel.selectMonth(2026, 1)
        advanceUntilIdle()
        viewModel.previousMonth()
        advanceUntilIdle()
        assertEquals(2025, viewModel.uiState.value.selectedYear)
        assertEquals(12, viewModel.uiState.value.selectedMonth)
    }

    @Test
    fun testSavingsRateCalculation() {
        val expenseTx = TransactionWithCategory(
            transaction = TransactionEntity(
                id = 1L,
                title = "Expense",
                amount = 3000L,
                type = TransactionType.EXPENSE,
                categoryId = 1L,
                date = LocalDate.of(2026, 10, 1),
                currencyCode = "USD"
            ),
            category = null
        )
        val incomeTx = TransactionWithCategory(
            transaction = TransactionEntity(
                id = 2L,
                title = "Income",
                amount = 5000L,
                type = TransactionType.INCOME,
                categoryId = 2L,
                date = LocalDate.of(2026, 10, 1),
                currencyCode = "USD"
            ),
            category = null
        )

        val uiState = MonthlyUiState(
            transactions = listOf(expenseTx, incomeTx),
            viewingCurrency = AppCurrency.USD
        )
        assertEquals(3000L, uiState.totalExpense)
        assertEquals(5000L, uiState.totalIncome)
        assertEquals(2000L, uiState.netSavings)
        // (5000 - 3000) * 100 / 5000 = 40%
        assertEquals(40, uiState.savingsRate)
    }

    @Test
    fun testCategoryBudgetCalculation_detectsOverspend() {
        val category = CategoryEntity(id = 1L, name = "Dining", iconName = "restaurant", colorHex = 0xFFFF0000)
        val budget = BudgetEntity(categoryId = 1L, monthlyLimit = 40000L, currencyCode = "USD") // $400 limit
        val overspendTx = TransactionWithCategory(
            transaction = TransactionEntity(
                id = 1L,
                title = "Dinner",
                amount = 45000L, // $450 spent
                type = TransactionType.EXPENSE,
                categoryId = 1L,
                date = LocalDate.of(2026, 10, 1),
                currencyCode = "USD"
            ),
            category = category
        )

        val uiState = MonthlyUiState(
            transactions = listOf(overspendTx),
            budgets = listOf(budget),
            allExpenseCategories = listOf(category),
            viewingCurrency = AppCurrency.USD
        )

        assertEquals(45000L, uiState.totalExpense)
        assertEquals(40000L, uiState.totalBudget)
        assertEquals(1, uiState.overBudgetCount)

        val progress = uiState.categoryBudgetProgressList.first()
        assertEquals(true, progress.isOverBudget)
        assertEquals(45000L, progress.spentAmount)
        assertEquals(40000L, progress.budgetLimit)
    }
}
