package com.budgettracker.app.util

import android.content.Context
import android.content.Intent
import com.budgettracker.app.data.model.TransactionWithCategory

object DataExportUtils {
    fun generateCsv(transactions: List<TransactionWithCategory>): String {
        val sb = StringBuilder()
        sb.append("ID,Date,Type,Category,Title,Amount,Currency,Note\n")
        transactions.forEach { item ->
            val tx = item.transaction
            val catName = item.category?.name ?: "Uncategorized"
            val dollars = String.format(java.util.Locale.US, "%.2f", tx.amount.toDouble() / 100.0)
            val noteSafe = tx.note.replace("\"", "\"\"")
            val titleSafe = tx.title.replace("\"", "\"\"")
            sb.append("${tx.id},${tx.date},${tx.type},\"$catName\",\"$titleSafe\",$dollars,${tx.currencyCode},\"$noteSafe\"\n")
        }
        return sb.toString()
    }

    fun shareCsv(context: Context, csvContent: String) {
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, csvContent)
            putExtra(Intent.EXTRA_SUBJECT, "Budget_Tracker_Export_${java.time.LocalDate.now()}.csv")
            type = "text/csv"
        }
        val shareIntent = Intent.createChooser(sendIntent, "Export Financial Records")
        context.startActivity(shareIntent)
    }
}
