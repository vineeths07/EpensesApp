package com.budgettracker.app.ui.yearly

import com.budgettracker.app.data.model.AppCurrency
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
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class YearlyExpenseViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: TransactionRepositoryImpl
    private lateinit var viewModel: YearlyExpenseViewModel
    private lateinit var fakeDao: FakeTransactionDao
    private lateinit var fakeCategoryDao: FakeCategoryDao

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeDao = FakeTransactionDao()
        fakeCategoryDao = FakeCategoryDao()
        repository = TransactionRepositoryImpl(fakeDao, fakeCategoryDao, FakeBudgetDao(), ioDispatcher = testDispatcher)
        viewModel = YearlyExpenseViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testYearNavigation() = runTest {
        backgroundScope.launch { viewModel.uiState.collect() }

        viewModel.selectYear(2026)
        advanceUntilIdle()
        assertEquals(2026, viewModel.uiState.value.selectedYear)

        viewModel.nextYear()
        advanceUntilIdle()
        assertEquals(2027, viewModel.uiState.value.selectedYear)

        viewModel.previousYear()
        advanceUntilIdle()
        assertEquals(2026, viewModel.uiState.value.selectedYear)
    }

    @Test
    fun testYearlyMetricsCalculations() {
        val tx1 = TransactionWithCategory(
            transaction = TransactionEntity(
                id = 1L,
                title = "Jan Spend",
                amount = 1000L,
                type = TransactionType.EXPENSE,
                categoryId = 1L,
                date = LocalDate.of(2026, 1, 15),
                month = 1,
                currencyCode = "USD"
            ),
            category = null
        )
        val tx2 = TransactionWithCategory(
            transaction = TransactionEntity(
                id = 2L,
                title = "Feb Spend (Peak)",
                amount = 2500L,
                type = TransactionType.EXPENSE,
                categoryId = 1L,
                date = LocalDate.of(2026, 2, 10),
                month = 2,
                currencyCode = "USD"
            ),
            category = null
        )
        val tx3 = TransactionWithCategory(
            transaction = TransactionEntity(
                id = 3L,
                title = "Mar Spend",
                amount = 500L,
                type = TransactionType.EXPENSE,
                categoryId = 1L,
                date = LocalDate.of(2026, 3, 5),
                month = 3,
                currencyCode = "USD"
            ),
            category = null
        )
        val incomeTx = TransactionWithCategory(
            transaction = TransactionEntity(
                id = 4L,
                title = "Annual Income",
                amount = 12000L,
                type = TransactionType.INCOME,
                categoryId = 2L,
                date = LocalDate.of(2026, 6, 1),
                month = 6,
                currencyCode = "USD"
            ),
            category = null
        )

        val uiState = YearlyUiState(
            selectedYear = 2026,
            transactions = listOf(tx1, tx2, tx3, incomeTx),
            viewingCurrency = AppCurrency.USD
        )

        assertEquals(4000L, uiState.totalExpense) // 1000 + 2500 + 500
        assertEquals(12000L, uiState.totalIncome)
        assertEquals(8000L, uiState.netSavings)
        assertEquals(4000L / 12L, uiState.averageMonthlyExpense)
        assertEquals(1000L, uiState.averageMonthlyIncome)

        val peak = uiState.peakSpendingMonth
        assertNotNull(peak)
        assertEquals(2, peak?.month)
        assertEquals(2500L, peak?.totalExpense)
    }
}
