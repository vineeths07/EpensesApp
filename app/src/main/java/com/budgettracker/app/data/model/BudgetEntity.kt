package com.budgettracker.app.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "budgets",
    indices = [
        Index(value = ["categoryId"], unique = true)
    ]
)
data class BudgetEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val categoryId: Long,
    val monthlyLimit: Long, // Minor units (cents), e.g. 40000 = $400.00
    val currencyCode: String = AppCurrency.getDefault().code
) {
    val currency: AppCurrency
        get() = AppCurrency.fromCode(currencyCode)
}
