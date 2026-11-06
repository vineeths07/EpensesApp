package com.budgettracker.app.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.budgettracker.app.data.converter.Converters
import com.budgettracker.app.data.dao.BudgetDao
import com.budgettracker.app.data.dao.CategoryDao
import com.budgettracker.app.data.dao.TransactionDao
import com.budgettracker.app.data.model.BudgetEntity
import com.budgettracker.app.data.model.CategoryEntity
import com.budgettracker.app.data.model.TransactionEntity
import com.budgettracker.app.data.model.TransactionType

@Database(
    entities = [
        TransactionEntity::class,
        CategoryEntity::class,
        BudgetEntity::class
    ],
    version = 4,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun categoryDao(): CategoryDao
    abstract fun budgetDao(): BudgetDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "budget_tracker.db"
                )
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                INSTANCE = instance
                instance
            }
        }

        val defaultCategories = listOf(
            // Expense Categories with Fixed IDs
            CategoryEntity(id = 1L, name = "Food & Dining", iconName = "restaurant", colorHex = 0xFFFF7043, type = TransactionType.EXPENSE),
            CategoryEntity(id = 2L, name = "Groceries", iconName = "shopping_cart", colorHex = 0xFF66BB6A, type = TransactionType.EXPENSE),
            CategoryEntity(id = 3L, name = "Transportation", iconName = "directions_car", colorHex = 0xFF42A5F5, type = TransactionType.EXPENSE),
            CategoryEntity(id = 4L, name = "Housing & Rent", iconName = "home", colorHex = 0xFFAB47BC, type = TransactionType.EXPENSE),
            CategoryEntity(id = 5L, name = "Utilities & Bills", iconName = "receipt_long", colorHex = 0xFFFFA726, type = TransactionType.EXPENSE),
            CategoryEntity(id = 6L, name = "Shopping", iconName = "shopping_bag", colorHex = 0xFFEC407A, type = TransactionType.EXPENSE),
            CategoryEntity(id = 7L, name = "Entertainment", iconName = "movie", colorHex = 0xFF26A69A, type = TransactionType.EXPENSE),
            CategoryEntity(id = 8L, name = "Health & Fitness", iconName = "fitness_center", colorHex = 0xFFEF5350, type = TransactionType.EXPENSE),
            CategoryEntity(id = 9L, name = "Education", iconName = "school", colorHex = 0xFF5C6BC0, type = TransactionType.EXPENSE),
            CategoryEntity(id = 10L, name = "Personal Care", iconName = "spa", colorHex = 0xFF8D6E63, type = TransactionType.EXPENSE),
            CategoryEntity(id = 11L, name = "General Expense", iconName = "category", colorHex = 0xFF78909C, type = TransactionType.EXPENSE),

            // Income Categories with Fixed IDs
            CategoryEntity(id = 12L, name = "Salary", iconName = "payments", colorHex = 0xFF43A047, type = TransactionType.INCOME),
            CategoryEntity(id = 13L, name = "Freelance & Business", iconName = "work", colorHex = 0xFF00ACC1, type = TransactionType.INCOME),
            CategoryEntity(id = 14L, name = "Investments", iconName = "trending_up", colorHex = 0xFF3949AB, type = TransactionType.INCOME),
            CategoryEntity(id = 15L, name = "Gifts & Allowance", iconName = "card_giftcard", colorHex = 0xFFD81B60, type = TransactionType.INCOME),
            CategoryEntity(id = 16L, name = "Other Income", iconName = "account_balance_wallet", colorHex = 0xFF7CB342, type = TransactionType.INCOME)
        )
    }
}
