package dev.estaki.ui_utils

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import dev.estaki.kt_pure_utils.arabicToDecimal
import dev.estaki.kt_pure_utils.toPersianDigits

enum class AppLanguage { PERSIAN, ENGLISH }
enum class ThemeMode { SYSTEM, LIGHT, DARK }

object AppAppearance {
    private const val PREFERENCES = "appearance"
    private lateinit var context: Context

    var language by mutableStateOf(AppLanguage.PERSIAN)
        private set
    var themeMode by mutableStateOf(ThemeMode.SYSTEM)
        private set

    fun initialize(context: Context) {
        this.context = context.applicationContext
        val preferences = this.context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)
        language = AppLanguage.entries.firstOrNull { it.name == preferences.getString("language", null) }
            ?: AppLanguage.PERSIAN
        themeMode = ThemeMode.entries.firstOrNull { it.name == preferences.getString("theme", null) }
            ?: ThemeMode.SYSTEM
    }

    fun selectLanguage(value: AppLanguage) {
        language = value
        context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE).edit().putString("language", value.name).apply()
    }

    fun selectThemeMode(value: ThemeMode) {
        themeMode = value
        context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE).edit().putString("theme", value.name).apply()
    }
}

val LocalDarkTheme = staticCompositionLocalOf { false }

fun localized(text: String): String = if (AppAppearance.language == AppLanguage.ENGLISH) {
    englishTranslations[text] ?: text
} else {
    text
}

fun String.localizedDigits(): String = if (AppAppearance.language == AppLanguage.PERSIAN) {
    toPersianDigits()
} else {
    arabicToDecimal()
}

private val englishTranslations = mapOf(
    "مانیفای" to "Monify",
    "حساب‌ها و تراکنش‌ها، یک‌جا" to "Accounts and transactions, together",
    "حریم خصوصی و سیاست‌ها" to "Privacy policy",
    "مانیفای با اجازه شما پیامک‌های موجود و جدید را روی دستگاه بررسی می‌کند تا تراکنش‌های بانکی را نمایش دهد. پیامک‌های شناسایی‌شده در دستگاه ذخیره می‌شوند. برای آگاهی از نحوه پردازش داده و گزارش خطا، حریم خصوصی را بخوانید." to "With your permission, Monify checks existing and incoming SMS on this device to show bank transactions. Recognized messages are stored on your device. Read the privacy policy to learn about data processing and crash reports.",
    "یک نگاه روشن به دخل و خرجت" to "A clearer view of your money",
    "بزن بریم" to "Continue",
    "متاسفانه شما دسترسی های اجباری مورد نیاز مانیفای رو تایید نکردید، لطفا دسترسی ها را تایید کنید." to "Please allow the permissions Monify needs to work.",
    "متاسفانه بدلیل تلاش بیش از حد برای دریافت دسترسی ها، دریافت به صورت مستقیم امان پذیر نیست، لطفا با زدن دکنه زیر به تنظیمات اپلیکیشن رفته و از آن قسمت دسترسی های مورد نیاز اپ را تایید کنید." to "Open app settings to grant the required permissions.",
    "افزودن کارت اعتباری" to "Add bank card",
    "کارت اعتباری" to "Bank card",
    "نام بانک" to "Bank name",
    " موجودی: " to " Balance: ",
    "مبلغ" to "Amount",
    "باشه" to "OK",
    "انصراف" to "Cancel",
    "تایید" to "Confirm",
    "تایید دسترسی" to "Allow access",
    "سلام رفیق\n اپلیکیشن برای این که بتونه پیامک تراکنش های بانکی تو رو بهت نشون بده نیاز داره که تو این دسترسی رو تایید کنی." to "Allow SMS access to see your bank transactions in the app.",
    "درحال بارگذاری..." to "Loading...",
    "ویرایش جزئیات کارت" to "Edit card details",
    "افزودن کارت" to "Add card",
    "هشدار" to "Warning",
    "از حذف کردن این کارت بانکی اطمینان دارید؟" to "Delete this bank card?",
    "شماره حساب" to "Account number",
    "شماره کارت" to "Card number",
    "ذخیره" to "Save",
    "نام بانک نمیتواند خالی باشد" to "Bank name is required",
    "شماره کارت نمیتواند خالی باشد" to "Card number is required",
    "شماره کارت نمیتواند کمتر از 16 رقم باشد" to "Card number must contain 16 digits",
    "شماره حساب نمیتواند خالی باشد" to "Account number is required",
    "متاسفانه عملیات مورد نظر با خطا مواجه شد! \n لطفا مقادیر خالی را پرکنید. " to "Please fill in the required fields.",
    "متاسفانه عملیات مورد نظر با خطا مواجه شد!" to "Something went wrong. Please try again.",
    "از حذف کردن این تراکنش بانکی اطمینان دارید؟" to "Delete this transaction?",
    "ویرایش اطلاعات تراکنش" to "Edit transaction",
    "افزودن تراکنش جدید" to "Add transaction",
    "دخل" to "Income",
    "خرج" to "Expense",
    "ريال" to "IRR",
    "ریال" to "IRR",
    "تاریخ" to "Date",
    "ساعت" to "Time",
    "انتخاب حساب" to "Select account",
    "انتخاب کنید" to "Select",
    "حسابی برای انتخاب وجود ندارد" to "No accounts available",
    "حساب انتخاب‌شده" to "Selected account",
    "؟" to "?",
    "توضیحات تراکنشت رو اینجا وارد کن..." to "Enter transaction details...",
    "توضیحات" to "Description",
    "دسته بندی" to "Category",
    "مبلغ نمیتواند خالی باشد" to "Amount is required",
    "تاریخ نمیتواند خالی باشد" to "Date is required",
    "ساعت تراکنش نمیتواند خالی باشد" to "Time is required",
    "واریز" to "Deposit",
    "برداشت" to "Withdrawal",
    "حساب بانکی‌ای برای نمایش پیدا نشد. می‌توانید کارت و تراکنش را به‌صورت دستی اضافه کنید." to "No bank accounts found. You can add a card and transactions manually.",
    "تعداد تراکنش های این حساب: " to "Transactions in this account: ",
    "افزودن تراکنش بانکی پس از تعریف یک کارت اعتباری و شماره حساب امکان پذیر خواهد بود. \n لطفا ابتدا کارت اعتباری را اضافه کنید. " to "Add a bank card and account before adding a transaction.",
    "در حال به‌روزرسانی" to "Updating",
    "مجموع واریز" to "Total deposits",
    "مجموع برداشت" to "Total withdrawals",
    "مبلغ بعضی تراکنش‌ها در مجموع محاسبه نشده است." to "Some transaction amounts were excluded from the totals.",
    "حساب‌های من" to "My accounts",
    "برای دیدن حساب‌ها، کارت‌ها را ورق بزن" to "Swipe cards to see your accounts",
    "تغییرات مورد نظر شما ذخیره شد." to "Your changes were saved.",
    "خطا در دریافت اطلاعات کارت" to "Unable to load card details",
    "تراکنش بانکی جدید" to "New bank transaction",
    "برای مشاهده جزئیات، برنامه را باز کنید." to "Open the app to see the details.",
    "یادآوری تراکنش بانکی" to "Bank transaction reminder",
    "اگر تراکنشی انجام داده‌اید، جزئیات آن را در برنامه بررسی کنید." to "Check the app for your transaction details.",
    "خرید خوراکی" to "Groceries",
    "خرید شارژ" to "Mobile credit",
    "بسته اینترنت" to "Internet package",
    "خودرو" to "Car",
    "لوازم جانبی موبایل" to "Phone accessories",
    "پوشاک" to "Clothing",
    "اجاره خانه" to "Rent",
    "پرداخت قبض" to "Bills",
    " هزینه سفر" to "Travel expenses",
    "بلیط سفر" to "Travel tickets",
    "تفریح با دوستان" to "Entertainment with friends",
    "شام بیرون از منزل" to "Dinner out",
    "ناهار بیرون از منزل" to "Lunch out",
    "ورزش و باشگاه" to "Sports and gym",
    "آرایشگاه و خدمات زیبایی" to "Beauty services",
    "خرید لوازم آرایشی و بهداشتی" to "Beauty products",
    "لوازم جانبی کامپیوتر و لپ تاپ" to "Computer accessories",
    "تاکسی اینترنتی" to "Ride-hailing",
    "سفارش غذا" to "Food delivery",
    "خرید سوپرمارکتی" to "Supermarket",
    "دیت" to "Date night",
    "سینما" to "Cinema"
)
