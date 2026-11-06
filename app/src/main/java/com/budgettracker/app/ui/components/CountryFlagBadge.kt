package com.budgettracker.app.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.budgettracker.app.data.model.AppCurrency

@Composable
fun CountryFlagBadge(
    currency: AppCurrency,
    modifier: Modifier = Modifier,
    height: Dp = 18.dp
) {
    val width = height * 1.35f
    Box(
        modifier = modifier
            .width(width)
            .height(height)
            .clip(RoundedCornerShape(3.dp))
            .border(
                width = 0.5.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                shape = RoundedCornerShape(3.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = currency.flagResId),
            contentDescription = "${currency.code} flag",
            contentScale = ContentScale.Crop
        )
    }
}
