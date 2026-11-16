package dev.estaki.myFinancialApp.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.estaki.ui_utils.AppAppearance
import dev.estaki.ui_utils.AppLanguage

@Composable
internal fun PrivacyPolicyScreen() {
    val persian = AppAppearance.language == AppLanguage.PERSIAN
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = if (persian) "اطلاعات شما، روی دستگاه شما" else "Your data stays on your device",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = if (persian) "این صفحه توضیح می‌دهد مانیفای چه داده‌هایی را برای نمایش تراکنش‌ها و بهبود برنامه استفاده می‌کند." else "This page explains how Monify uses data to show transactions and improve the app.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        PrivacySection(
            title = if (persian) "خواندن و پردازش پیامک‌ها" else "Reading and processing SMS",
            body = if (persian) "با اجازه شما، مانیفای متن، فرستنده و زمان پیامک‌های موجود و پیامک‌های جدید را روی همین دستگاه بررسی می‌کند تا پیامک‌های احتمالی تراکنش بانکی را شناسایی کند. تشخیص خودکار ممکن است اشتباه کند یا بعضی پیامک‌ها را نشناسد. سایر پیامک‌ها تنها در صورتی به‌عنوان تراکنش ذخیره می‌شوند که خودتان آن‌ها را بررسی و دستی ثبت کنید." else "With your permission, Monify checks the text, sender and time of existing and incoming SMS on this device to identify possible bank transactions. Automatic recognition can make mistakes or miss messages. Other messages are saved as transactions only when you review and add them manually."
        )
        PrivacySection(
            title = if (persian) "نگهداری اطلاعات مالی" else "Financial data storage",
            body = if (persian) "پیامک‌های شناسایی‌شده، فرستنده، مبلغ، شماره حساب یا کارت، موجودی و تغییراتی که در برنامه وارد می‌کنید در پایگاه‌داده محلی دستگاه ذخیره می‌شوند. مانیفای مسیر مستقلی برای ارسال متن پیامک یا اطلاعات تراکنش به سرور ما ندارد و این اطلاعات را عمداً به سرویس‌های آمار و گزارش خطا ارسال نمی‌کند. پشتیبان‌گیری خودکار برنامه غیرفعال است." else "Recognized messages, their senders, amounts, account or card numbers, balances and your edits are stored in the app's local database. Monify has no separate feature that sends SMS text or transaction details to our server, and does not intentionally include them in analytics or crash reports. Automatic app backup is disabled."
        )
        PrivacySection(
            title = if (persian) "اینترنت، آمار و گزارش خطا" else "Internet, analytics and crash reports",
            body = if (persian) "دسترسی اینترنت برای Google Firebase Analytics و Crashlytics، به‌منظور آمار استفاده و بررسی خطاها به کار می‌رود. این سرویس‌ها ممکن است اطلاعات فنی مانند نسخه برنامه و سیستم‌عامل، مشخصات دستگاه، شناسه‌های دستگاه یا نصب، رویدادهای استفاده و جزئیات خطا را به گوگل ارسال کنند. گزارش خطا ممکن است شامل متن فنی خطا باشد. این اطلاعات روی سرویس‌های گوگل پردازش می‌شوند و مانند اطلاعات مالی، صرفاً محلی نیستند." else "Internet access is used by Google Firebase Analytics and Crashlytics for usage analytics and troubleshooting. These services may send technical information to Google, such as app and OS versions, device details, device or installation identifiers, usage events and crash details. Crash reports may include technical error text. Unlike financial data, this information is processed by Google's services and is not exclusively local."
        )
        PrivacySection(
            title = if (persian) "اعلان‌ها و دسترسی‌ها" else "Notifications and permissions",
            body = if (persian) "دسترسی خواندن پیامک برای بررسی پیامک‌های قبلی و دسترسی دریافت پیامک برای شناسایی پیامک‌های جدید استفاده می‌شود. اجازه اعلان برای نمایش هشدار تراکنش و یادآوری لازم است. اعلان‌ها متن پیامک یا مبلغ را نشان نمی‌دهند. می‌توانید دسترسی‌ها و اعلان‌ها را در تنظیمات دستگاه تغییر دهید؛ غیرفعال کردن آن‌ها یا محدودیت‌های پس‌زمینه دستگاه ممکن است تشخیص یا نمایش اعلان را متوقف کند." else "SMS read access is used to check past messages, and SMS receive access is used to recognize new ones. Notification permission is needed for transaction alerts and reminders. Notifications do not show the message text or amount. You can change these permissions in device settings; disabling them or device background restrictions may stop detection or notifications."
        )
        PrivacySection(
            title = if (persian) "حذف داده‌ها و گزارش دیباگ" else "Deleting data and debug reports",
            body = if (persian) "با حذف برنامه، داده‌های ذخیره‌شده در فضای خصوصی آن از دستگاه پاک می‌شوند. این کار پیامک‌های اصلی را از برنامه پیامک گوشی حذف نمی‌کند. در نسخه دیباگ، شما می‌توانید عمداً فایل گزارش حاوی متن پیامک و اطلاعات حساب را در پوشه عمومی بسازید؛ این فایل جدا از داده‌های برنامه است و ممکن است پس از حذف برنامه باقی بماند. در صورت ارسال دستی آن، گیرنده به محتوایش دسترسی خواهد داشت؛ در صورت نیاز فایل را جداگانه حذف کنید." else "Uninstalling the app removes its private local data; it does not delete the original SMS from your messaging app. In debug builds, you can deliberately create a report containing SMS text and account information in a public folder. That file is separate from app data and may remain after uninstalling. Anyone you manually send it to can read it; delete it separately if needed."
        )
        PrivacySection(
            title = if (persian) "سیاست استفاده" else "Using Monify",
            body = if (persian) "مانیفای ابزار کمکی مدیریت مالی است و جایگزین اطلاعات رسمی بانک نیست. برای تصمیم‌های مالی و بررسی صحت مبلغ یا موجودی، اطلاعات را با پیامک و حساب بانکی خود تطبیق دهید. این توضیحات در تنظیمات برنامه همیشه در دسترس است." else "Monify is a financial organization tool and is not a substitute for official bank information. Check amounts and balances against your bank account and messages before making financial decisions. This policy remains available in app settings."
        )
    }
}

@Composable
private fun PrivacySection(title: String, body: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Text(body, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 24.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
