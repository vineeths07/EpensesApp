package com.budgettracker.app.data.converter

import com.budgettracker.app.data.model.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

class ConvertersTest {
    private val converters = Converters()

    @Test
    fun testLocalDateConversion() {
        val date = LocalDate.of(2026, 10, 1)
        val epochDay = converters.localDateToEpochDay(date)
        val convertedBack = converters.fromEpochDay(epochDay)

        assertEquals(date, convertedBack)
        assertEquals(2026, convertedBack?.year)
        assertEquals(10, convertedBack?.monthValue)
        assertEquals(1, convertedBack?.dayOfMonth)
    }

    @Test
    fun testNullLocalDateConversion() {
        assertNull(converters.localDateToEpochDay(null))
        assertNull(converters.fromEpochDay(null))
    }

    @Test
    fun testTransactionTypeConversion() {
        val expense = TransactionType.EXPENSE
        val income = TransactionType.INCOME

        assertEquals("EXPENSE", converters.fromTransactionType(expense))
        assertEquals("INCOME", converters.fromTransactionType(income))

        assertEquals(TransactionType.EXPENSE, converters.toTransactionType("EXPENSE"))
        assertEquals(TransactionType.INCOME, converters.toTransactionType("INCOME"))
    }
}
