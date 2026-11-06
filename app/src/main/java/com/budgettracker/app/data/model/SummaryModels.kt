package com.budgettracker.app.data.model

data class CategoryExpenseSummary(
    val categoryId: Long,
    val categoryName: String,
    val iconName: String,
    val colorHex: Long,
    val totalAmount: Long,
    val transactionCount: Int
)

data class MonthlyExpenseSummary(
    val month: Int,
    val totalExpense: Long,
    val totalIncome: Long
)

data class DailyExpenseSummary(
    val date: java.time.LocalDate,
    val totalExpense: Long,
    val totalIncome: Long
)
