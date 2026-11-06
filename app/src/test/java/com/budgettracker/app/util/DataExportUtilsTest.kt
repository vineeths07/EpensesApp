package com.budgettracker.app.util

import com.budgettracker.app.data.model.CategoryEntity
import com.budgettracker.app.data.model.TransactionEntity
import com.budgettracker.app.data.model.TransactionType
import com.budgettracker.app.data.model.TransactionWithCategory
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class DataExportUtilsTest {
    @Test
    fun testGenerateCsv_producesValidCsv() {
        val category = CategoryEntity(id = 1L, name = "Food & Dining", iconName = "restaurant", colorHex = 0xFFFF0000)
        val tx = TransactionWithCategory(
            transaction = TransactionEntity(
                id = 101L,
                title = "Dinner with \"Friends\"",
                amount = 2550L,
                type = TransactionType.EXPENSE,
                categoryId = 1L,
                date = LocalDate.of(2026, 10, 1),
                note = "Pizza & Pasta",
                currencyCode = "USD"
            ),
            category = category
        )

        val csv = DataExportUtils.generateCsv(listOf(tx))

        assertTrue(csv.contains("ID,Date,Type,Category,Title,Amount,Currency,Note"))
        assertTrue(csv.contains("101,2026-10-01,EXPENSE,\"Food & Dining\""))
        assertTrue(csv.contains("\"Dinner with \"\"Friends\"\"\""))
        assertTrue(csv.contains("25.50,USD,\"Pizza & Pasta\""))
    }
}
