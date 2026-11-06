package com.budgettracker.app.data.model

data class CategoryBudgetProgress(
    val categoryId: Long,
    val categoryName: String,
    val iconName: String,
    val colorHex: Long,
    val spentAmount: Long, // in viewing currency
    val budgetLimit: Long?, // in viewing currency (null if no budget set)
    val transactionCount: Int
) {
    val isBudgetSet: Boolean
        get() = budgetLimit != null && budgetLimit > 0L

    val isOverBudget: Boolean
        get() = isBudgetSet && spentAmount > (budgetLimit ?: 0L)

    val remainingAmount: Long
        get() = if (budgetLimit != null) budgetLimit - spentAmount else 0L

    val progressFraction: Float
        get() = if (isBudgetSet) {
            (spentAmount.toFloat() / (budgetLimit ?: 1L).toFloat()).coerceIn(0f, 1f)
        } else 0f

    val percentage: Int
        get() = if (isBudgetSet && (budgetLimit ?: 0L) > 0L) {
            ((spentAmount * 100L) / (budgetLimit ?: 1L)).toInt()
        } else 0
}
