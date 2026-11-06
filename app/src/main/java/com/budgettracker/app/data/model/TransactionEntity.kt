package com.budgettracker.app.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(
    tableName = "transactions",
    indices = [
        Index(value = ["categoryId"]),
        Index(value = ["date"]),
        Index(value = ["year", "month"])
    ]
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val amount: Long, // Stored in minor currency units (cents), e.g. 1550 = $15.50
    val type: TransactionType = TransactionType.EXPENSE,
    val categoryId: Long,
    val date: LocalDate,
    val year: Int = date.year,
    val month: Int = date.monthValue,
    val note: String = "",
    val createdAtMillis: Long = System.currentTimeMillis(),
    val currencyCode: String = AppCurrency.getDefault().code
) {
    val currency: AppCurrency
        get() = AppCurrency.fromCode(currencyCode)
}
