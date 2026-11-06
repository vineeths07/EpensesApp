package com.budgettracker.app.util

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Work
import androidx.compose.ui.graphics.vector.ImageVector

object CategoryIconHelper {
    fun getIconByName(iconName: String): ImageVector {
        return when (iconName) {
            "restaurant" -> Icons.Default.Restaurant
            "shopping_cart" -> Icons.Default.ShoppingCart
            "directions_car" -> Icons.Default.DirectionsCar
            "home" -> Icons.Default.Home
            "receipt_long" -> Icons.AutoMirrored.Filled.ReceiptLong
            "shopping_bag" -> Icons.Default.ShoppingBag
            "movie" -> Icons.Default.Movie
            "fitness_center" -> Icons.Default.FitnessCenter
            "school" -> Icons.Default.School
            "spa" -> Icons.Default.Spa
            "payments" -> Icons.Default.Payments
            "work" -> Icons.Default.Work
            "trending_up" -> Icons.AutoMirrored.Filled.TrendingUp
            "card_giftcard" -> Icons.Default.CardGiftcard
            "account_balance_wallet" -> Icons.Default.AccountBalanceWallet
            else -> Icons.Default.Category
        }
    }
}
