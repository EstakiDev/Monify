package dev.estaki.myFinancialApp.presentation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.platform.LocalContext
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import dev.estaki.monify.R
import dev.estaki.ui_utils.AppAppearance
import dev.estaki.ui_utils.AppLanguage
import dev.estaki.ui_utils.ThemeMode

@Composable
internal fun AppearanceSettings(onDismiss: () -> Unit, onPrivacyClick: () -> Unit) {
    val persian = AppAppearance.language == AppLanguage.PERSIAN
    val context = LocalContext.current
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 24.dp).widthIn(max = 440.dp),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            tonalElevation = 6.dp
        ) {
            Column(modifier = Modifier.fillMaxWidth().heightIn(max = 680.dp).verticalScroll(rememberScrollState())) {
                SettingsHeader(persian = persian, onDismiss = onDismiss)
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    SettingsSection(
                        title = if (persian) "زبان برنامه" else "App language",
                        subtitle = if (persian) "زبانی که با آن راحت‌تری" else "Choose your preferred language"
                    ) {
                        SettingsChoice(
                            title = "فارسی",
                            subtitle = "Persian",
                            selected = AppAppearance.language == AppLanguage.PERSIAN,
                            badge = { LanguageBadge("فا") },
                            onClick = { AppAppearance.selectLanguage(AppLanguage.PERSIAN) }
                        )
                        SettingsChoice(
                            title = "English",
                            subtitle = if (persian) "انگلیسی" else "English",
                            selected = AppAppearance.language == AppLanguage.ENGLISH,
                            badge = { LanguageBadge("En") },
                            onClick = { AppAppearance.selectLanguage(AppLanguage.ENGLISH) }
                        )
                    }
                    SettingsSection(
                        title = if (persian) "ظاهر برنامه" else "Appearance",
                        subtitle = if (persian) "فضای دلخواهت را انتخاب کن" else "Make Monify feel like yours"
                    ) {
                        SettingsChoice(
                            title = if (persian) "هماهنگ با دستگاه" else "System default",
                            subtitle = if (persian) "تغییر خودکار با تنظیمات دستگاه" else "Follow your device settings",
                            selected = AppAppearance.themeMode == ThemeMode.SYSTEM,
                            badge = { ThemeBadge(ThemeMode.SYSTEM) },
                            onClick = { AppAppearance.selectThemeMode(ThemeMode.SYSTEM) }
                        )
                        SettingsChoice(
                            title = if (persian) "روشن" else "Light",
                            subtitle = if (persian) "روشن و شفاف" else "Bright and clear",
                            selected = AppAppearance.themeMode == ThemeMode.LIGHT,
                            badge = { ThemeBadge(ThemeMode.LIGHT) },
                            onClick = { AppAppearance.selectThemeMode(ThemeMode.LIGHT) }
                        )
                        SettingsChoice(
                            title = if (persian) "تاریک" else "Dark",
                            subtitle = if (persian) "آرام و کم‌نور" else "Calm and dim",
                            selected = AppAppearance.themeMode == ThemeMode.DARK,
                            badge = { ThemeBadge(ThemeMode.DARK) },
                            onClick = { AppAppearance.selectThemeMode(ThemeMode.DARK) }
                        )
                    }
                    SettingsLink(
                        title = if (persian) "اعلان‌ها" else "Notifications",
                        subtitle = if (persian) "مدیریت اجازه نمایش اعلان" else "Manage notification permission",
                        onClick = {
                            val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                            } else {
                                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", context.packageName, null))
                            }
                            context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                        }
                    )
                    SettingsLink(
                        title = if (persian) "حریم خصوصی و سیاست‌ها" else "Privacy policy",
                        subtitle = if (persian) "داده‌های شما چگونه استفاده می‌شوند" else "How your data is used",
                        onClick = onPrivacyClick
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsLink(title: String, subtitle: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .clickable(onClick = onClick).heightIn(min = 60.dp).padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
            Text(subtitle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text("›", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
private fun SettingsHeader(persian: Boolean, onDismiss: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth()
            .background(Brush.horizontalGradient(listOf(Color(0xFF1A3041), Color(0xFF345667))))
            .padding(start = 16.dp, end = 8.dp, top = 16.dp, bottom = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Image(painterResource(R.drawable.monify_mark), contentDescription = null, modifier = Modifier.size(48.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = if (persian) "تنظیمات مانیفای" else "Monify settings",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = if (persian) "همه‌چیز، به سلیقه تو" else "Made personal for you",
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFFD3E0E4)
            )
        }
        IconButton(onClick = onDismiss) {
            Icon(Icons.Default.Close, contentDescription = if (persian) "بستن تنظیمات" else "Close settings", tint = Color.White)
        }
    }
}

@Composable
private fun SettingsSection(title: String, subtitle: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Column(modifier = Modifier.padding(bottom = 4.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Text(subtitle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Column(modifier = Modifier.selectableGroup(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            content()
        }
    }
}

@Composable
private fun SettingsChoice(
    title: String,
    subtitle: String,
    selected: Boolean,
    badge: @Composable () -> Unit,
    onClick: () -> Unit
) {
    val background by animateColorAsState(
        if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainer,
        tween(220), label = "setting background"
    )
    val outline by animateColorAsState(
        if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.55f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
        tween(220), label = "setting outline"
    )
    val shape = RoundedCornerShape(16.dp)
    Row(
        modifier = Modifier.fillMaxWidth().clip(shape).background(background).border(1.dp, outline, shape)
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick)
            .heightIn(min = 60.dp).padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        badge()
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
            Text(subtitle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
        Box(
            modifier = Modifier.size(22.dp).clip(CircleShape)
                .background(if (selected) MaterialTheme.colorScheme.primary else Color.Transparent)
                .border(1.dp, if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (selected) Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(15.dp), tint = MaterialTheme.colorScheme.onPrimary)
        }
    }
}

@Composable
private fun LanguageBadge(label: String) {
    Box(
        modifier = Modifier.size(40.dp).clip(RoundedCornerShape(13.dp)).background(MaterialTheme.colorScheme.secondaryContainer),
        contentAlignment = Alignment.Center
    ) {
        Text(label, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSecondaryContainer)
    }
}

@Composable
private fun ThemeBadge(mode: ThemeMode) {
    val foreground = MaterialTheme.colorScheme.onSecondaryContainer
    val background = MaterialTheme.colorScheme.secondaryContainer
    Box(
        modifier = Modifier.size(40.dp).clip(RoundedCornerShape(13.dp)).background(background),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(22.dp)) {
            val unit = size.minDimension
            when (mode) {
                ThemeMode.SYSTEM -> {
                    drawRoundRect(foreground, topLeft = Offset(unit * 0.15f, unit * 0.08f), size = Size(unit * 0.7f, unit * 0.7f), cornerRadius = CornerRadius(unit * 0.15f))
                    drawRoundRect(background, topLeft = Offset(unit * 0.23f, unit * 0.16f), size = Size(unit * 0.54f, unit * 0.48f), cornerRadius = CornerRadius(unit * 0.08f))
                    drawCircle(foreground, radius = unit * 0.035f, center = Offset(unit * 0.5f, unit * 0.71f))
                }
                ThemeMode.LIGHT -> {
                    drawCircle(foreground, radius = unit * 0.22f)
                    repeat(8) { index ->
                        val angle = Math.PI * index / 4
                        val direction = Offset(kotlin.math.cos(angle).toFloat(), kotlin.math.sin(angle).toFloat())
                        drawLine(foreground, center + direction * (unit * 0.33f), center + direction * (unit * 0.44f), strokeWidth = unit * 0.08f)
                    }
                }
                ThemeMode.DARK -> {
                    drawCircle(foreground, radius = unit * 0.37f, center = Offset(unit * 0.48f, unit * 0.52f))
                    drawCircle(background, radius = unit * 0.32f, center = Offset(unit * 0.67f, unit * 0.33f))
                }
            }
        }
    }
}
