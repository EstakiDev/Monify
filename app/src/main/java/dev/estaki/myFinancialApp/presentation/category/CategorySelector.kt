package dev.estaki.myFinancialApp.presentation.category

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.estaki.domain.models.CategoryModel
import dev.estaki.ui_utils.AppAppearance
import dev.estaki.ui_utils.AppLanguage
import dev.estaki.ui_utils.localized
import dev.estaki.ui_utils.localizedDigits
import dev.estaki.ui_utils.ui.theme.AppSpacing

@Composable
internal fun CategorySelector(categories: List<CategoryModel>, onToggle: (Long) -> Unit) {
    val selectedCount = categories.count { it.isChecked }
    val persian = AppAppearance.language == AppLanguage.PERSIAN
    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.Small)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = AppSpacing.Large),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(localized("دسته بندی"), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Text(
                    if (persian) "می‌توانید چند دسته انتخاب کنید" else "You can select multiple categories",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (selectedCount > 0) {
                Text(
                    (if (persian) "$selectedCount انتخاب‌شده" else "$selectedCount selected").localizedDigits(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        if (categories.isEmpty()) {
            Text(
                if (persian) "دسته‌ای موجود نیست." else "No categories available.",
                modifier = Modifier.padding(horizontal = AppSpacing.Large, vertical = AppSpacing.Small),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            LazyHorizontalGrid(
                rows = GridCells.Fixed(2),
                modifier = Modifier.fillMaxWidth().height(112.dp),
                contentPadding = PaddingValues(horizontal = AppSpacing.Large),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.Small),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.Small)
            ) {
                items(categories, key = { it.id }) { category ->
                    val selected = category.isChecked
                    val foreground = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                    Surface(
                        onClick = { onToggle(category.id) },
                        modifier = Modifier.semantics { this.selected = selected; role = Role.Checkbox },
                        shape = RoundedCornerShape(16.dp),
                        color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerLow,
                        border = BorderStroke(1.dp, if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.4f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier.size(30.dp).background(
                                    if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.14f) else MaterialTheme.colorScheme.surfaceContainerHighest,
                                    CircleShape
                                ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(painter = categoryIcon(category.id), contentDescription = null, modifier = Modifier.size(17.dp), tint = foreground)
                            }
                            Text(
                                text = localized(category.title.trim()),
                                modifier = Modifier.widthIn(max = 200.dp),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                color = foreground,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (selected) Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(16.dp), tint = foreground)
                        }
                    }
                }
            }
        }
    }
}
