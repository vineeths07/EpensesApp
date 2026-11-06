package com.budgettracker.app.ui.daily

import com.budgettracker.app.data.model.TransactionType
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
class DailyExpenseViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: TransactionRepositoryImpl
    private lateinit var viewModel: DailyExpenseViewModel
    private lateinit var fakeDao: FakeTransactionDao
    private lateinit var fakeCategoryDao: FakeCategoryDao

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeDao = FakeTransactionDao()
        fakeCategoryDao = FakeCategoryDao()
        repository = TransactionRepositoryImpl(fakeDao, fakeCategoryDao, FakeBudgetDao(), ioDispatcher = testDispatcher)
        viewModel = DailyExpenseViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testInitialDateAndNavigation() = runTest {
        backgroundScope.launch { viewModel.uiState.collect() }
        val targetDate = LocalDate.of(2026, 10, 1)
        viewModel.selectDate(targetDate)
        advanceUntilIdle()

        viewModel.previousDay()
        advanceUntilIdle()
        assertEquals(targetDate.minusDays(1), viewModel.uiState.value.selectedDate)

        viewModel.nextDay()
        advanceUntilIdle()
        assertEquals(targetDate, viewModel.uiState.value.selectedDate)
    }

    @Test
    fun testAddTransaction_updatesTotals() = runTest {
        backgroundScope.launch { viewModel.uiState.collect() }
        val targetDate = LocalDate.of(2026, 10, 1)
        viewModel.selectDate(targetDate)
        advanceUntilIdle()

        viewModel.addTransaction(
            title = "Groceries",
            amountCents = 2500L,
            type = TransactionType.EXPENSE,
            categoryId = 1L,
            date = targetDate,
            note = "Supermarket"
        )
        advanceUntilIdle()

        assertEquals(1, fakeDao.transactions.size)
        assertEquals("Groceries", fakeDao.transactions.first().title)
        assertEquals(2500L, fakeDao.transactions.first().amount)
    }
}
