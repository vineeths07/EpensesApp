package com.budgettracker.app.ui.monthly

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.budgettracker.app.data.model.AppCurrency
import com.budgettracker.app.data.model.CategoryBudgetProgress
import com.budgettracker.app.data.model.CategoryExpenseSummary
import com.budgettracker.app.ui.components.CategoryAvatar
import com.budgettracker.app.util.CurrencyUtils

@Composable
fun CategoryBreakdownCard(
    summaries: List<CategoryExpenseSummary> = emptyList(),
    budgetProgressList: List<CategoryBudgetProgress> = emptyList(),
    totalExpense: Long,
    currency: AppCurrency,
    onOpenSetBudget: ((Long?) -> Unit)? = null,
    isPrivacyMode: Boolean = false,
    modifier: Modifier = Modifier
) {
    val isBudgetMode = budgetProgressList.isNotEmpty() || onOpenSetBudget != null
    val overBudgetCount = budgetProgressList.count { it.isOverBudget }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Responsive Header with FlowRow
            @OptIn(ExperimentalLayoutApi::class)
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalArrangement = Arrangement.Center
            ) {
                Column(
                    modifier = Modifier.padding(end = 8.dp, bottom = 4.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = if (isBudgetMode) "Budgets & Spending" else "Spending by Category",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = if (isBudgetMode) "${budgetProgressList.size} categories tracked" else "${summaries.size} categories",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (onOpenSetBudget != null) {
                    FilledTonalButton(
                        onClick = { onOpenSetBudget(null) },
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.padding(bottom = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Set Budget",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }

            // Overspend Warning Alert Banner
            if (overBudgetCount > 0) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Alert",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "$overBudgetCount ${if (overBudgetCount == 1) "category has" else "categories have"} exceeded the monthly spending cap!",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (isBudgetMode) {
                // Render with Category Budget Progress
                if (budgetProgressList.isEmpty()) {
                    Text(
                        text = "No expenses or budgets set for this month.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        budgetProgressList.forEach { item ->
                            val isOver = item.isOverBudget
                            val hasBudget = item.isBudgetSet

                            val progressFraction = if (hasBudget) {
                                (item.spentAmount.toFloat() / (item.budgetLimit ?: 1L).toFloat()).coerceIn(0f, 1f)
                            } else {
                                if (totalExpense > 0) (item.spentAmount.toFloat() / totalExpense.toFloat()).coerceIn(0f, 1f) else 0f
                            }

                            val animatedProgress by animateFloatAsState(
                                targetValue = progressFraction,
                                label = "cat_budget_progress"
                            )

                            val progressColor = when {
                                isOver -> MaterialTheme.colorScheme.error
                                hasBudget && item.percentage >= 75 -> Color(0xFFF59E0B) // Amber warning
                                hasBudget -> Color(0xFF10B981) // Green on-track
                                else -> Color(item.colorHex)
                            }

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable(enabled = onOpenSetBudget != null) {
                                        onOpenSetBudget?.invoke(item.categoryId)
                                    }
                                    .padding(vertical = 2.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        CategoryAvatar(
                                            iconName = item.iconName,
                                            colorHex = item.colorHex,
                                            size = 32.dp,
                                            iconSize = 18.dp
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = item.categoryName,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            val subtext = if (hasBudget) {
                                                "Cap: ${CurrencyUtils.formatCents(item.budgetLimit ?: 0L, currency, mask = isPrivacyMode)}"
                                            } else {
                                                "${item.transactionCount} entries • No cap"
                                            }
                                            Text(
                                                text = subtext,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = CurrencyUtils.formatCents(item.spentAmount, currency, mask = isPrivacyMode),
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isOver) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                                        )
                                        if (isOver) {
                                            val overAmount = item.spentAmount - (item.budgetLimit ?: 0L)
                                            Text(
                                                text = "OVER by ${CurrencyUtils.formatCents(overAmount, currency, mask = isPrivacyMode)}",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.error
                                            )
                                        } else if (hasBudget) {
                                            Text(
                                                text = "${CurrencyUtils.formatCents(item.remainingAmount, currency, mask = isPrivacyMode)} left",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color(0xFF10B981),
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                // Progress Bar
                                LinearProgressIndicator(
                                    progress = { animatedProgress },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(4.dp)),
                                    color = progressColor,
                                    trackColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    val percentLabel = if (hasBudget) "${item.percentage}% used" else "No budget set"
                                    Text(
                                        text = percentLabel,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isOver) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontWeight = if (isOver) FontWeight.Bold else FontWeight.Normal
                                    )

                                    if (onOpenSetBudget != null && !hasBudget) {
                                        Text(
                                            text = "+ Set Cap",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // Fallback for Yearly screen (Summaries only)
                if (summaries.isEmpty() || totalExpense == 0L) {
                    Text(
                        text = "No expenses recorded for this period.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        summaries.forEach { item ->
                            val percentage = if (totalExpense > 0) {
                                (item.totalAmount.toFloat() / totalExpense.toFloat()).coerceIn(0f, 1f)
                            } else 0f

                            val animatedProgress by animateFloatAsState(
                                targetValue = percentage,
                                label = "category_progress"
                            )

                            val percentString = (percentage * 100).toInt()
                            val catColor = Color(item.colorHex)

                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        CategoryAvatar(
                                            iconName = item.iconName,
                                            colorHex = item.colorHex,
                                            size = 32.dp,
                                            iconSize = 18.dp
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            text = item.categoryName,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }

                                    Text(
                                        text = CurrencyUtils.formatCents(item.totalAmount, currency, mask = isPrivacyMode),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                LinearProgressIndicator(
                                    progress = { animatedProgress },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(4.dp)),
                                    color = catColor,
                                    trackColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "$percentString% of total",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "${item.transactionCount} entries",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
