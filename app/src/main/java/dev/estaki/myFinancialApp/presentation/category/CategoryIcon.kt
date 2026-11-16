package dev.estaki.myFinancialApp.presentation.category

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.painterResource
import dev.estaki.monify.R

/** Icons for the built-in category IDs. Unknown categories keep a neutral fallback. */
@Composable
internal fun categoryIcon(id: Long): Painter = when (id) {
    4L, 18L -> painterResource(dev.estaki.ui_utils.R.drawable.baseline_directions_car_24)
    12L, 13L, 19L -> painterResource(R.drawable.ic_category_food)
    14L -> painterResource(R.drawable.ic_category_fitness)
    else -> rememberVectorPainter(categoryVector(id))
}

private fun categoryVector(id: Long) = when (id) {
    1L, 20L -> Icons.Filled.ShoppingCart
    2L, 5L -> Icons.Filled.Phone
    3L -> Icons.Filled.Share
    17L -> Icons.Filled.Build
    6L, 16L -> Icons.Filled.Star
    7L -> Icons.Filled.Home
    8L -> Icons.Filled.List
    9L -> Icons.Filled.Place
    10L -> Icons.Filled.DateRange
    11L, 15L -> Icons.Filled.Face
    21L -> Icons.Filled.Favorite
    22L -> Icons.Filled.PlayArrow
    else -> Icons.Filled.List
}
