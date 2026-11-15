package dev.estaki.myFinancialApp.presentation.main

import android.content.ContentValues
import android.content.Context
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import dev.estaki.data.db.dao.SmsDao
import dev.estaki.domain.models.BankCardModel
import dev.estaki.domain.models.SmsModel
import dev.estaki.domain.models.SmsRawModel
import dev.estaki.domain.validator.SmsValidator
import java.io.File
import java.io.FileOutputStream
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

internal object SmsReportCsv {
    val header = listOf("reported_at_utc", "app_version", "android_sdk", "id", "sender", "sms_body", "bank_name", "account_number", "transaction_type", "amount", "date", "time", "timestamp_ms", "balance", "category_ids", "description", "modified", "seen", "classifier_type", "classifier_amount")

    fun row(values: List<Any?>): String = values.joinToString(",") { value ->
        "\"" + (value?.toString() ?: "").replace("\"", "\"\"") + "\""
    } + "\r\n"
}

@EntryPoint
@InstallIn(SingletonComponent::class)
internal interface CardReportEntryPoint {
    fun smsDao(): SmsDao
}

internal object SmsReportWriter {
    private const val FILE_NAME = "monify-sms-report.csv"
    private const val PREFS = "debug_sms_report"
    private const val URI_KEY = "report_uri"
    private const val CARD_FILE_NAME = "monify-bank-card-report.csv"
    private const val CARD_URI_KEY = "card_report_uri"
    private val cardHeader = listOf(
        "reported_at_utc", "app_version", "android_sdk", "card_id", "bank_name",
        "account_number", "card_number", "card_balance", "from_sms", "linked_sms_count",
        "sms_id", "sender", "sms_body", "stored_type", "stored_amount", "sms_date",
        "sms_timestamp_ms", "sms_balance", "sms_modified", "sms_removed", "classifier_type", "classifier_amount"
    )
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun write(context: Context, sms: SmsModel) {
        scope.launch {
            val result = runCatching { append(context, sms) }
            withContext(Dispatchers.Main) {
                val message = if (result.isSuccess) "گزارش در Download/Monify ذخیره شد" else "ذخیره گزارش ناموفق بود"
                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            }
        }
    }

    fun writeCard(context: Context, card: BankCardModel) {
        scope.launch {
            val result = runCatching {
                val smsDao = EntryPointAccessors.fromApplication(context, CardReportEntryPoint::class.java).smsDao()
                val messages = smsDao.readAll().filter { it.bankAccountNumber == card.bankAccountNumber }
                val timestamp = reportTimestamp()
                val version = appVersion(context)
                val cardFields = listOf(timestamp, version, Build.VERSION.SDK_INT, card.id, card.bankName,
                    card.bankAccountNumber, card.bankCardNumber, card.bankCardBalance, card.isItFromSms, messages.size)
                val rows = if (messages.isEmpty()) {
                    SmsReportCsv.row(cardFields + List(12) { null })
                } else messages.joinToString("") { sms ->
                    val classified = SmsValidator.classify(SmsRawModel(
                        sms.id?.toString().orEmpty(), sms.smsSender, sms.smsBody, sms.transactionDate,
                        sms.transactionDateTime.toString()
                    ))
                    SmsReportCsv.row(cardFields + listOf(sms.id, sms.smsSender, sms.smsBody, sms.transactionType,
                        sms.transactionAmount, sms.transactionDate, sms.transactionDateTime, sms.bankCardBalance,
                        sms.isModified, sms.isRemoved, classified?.type, classified?.amount))
                }
                appendFile(context, CARD_FILE_NAME, CARD_URI_KEY, cardHeader, rows)
            }
            withContext(Dispatchers.Main) {
                val message = if (result.isSuccess) "گزارش کارت در Download/Monify ذخیره شد" else "ذخیره گزارش کارت ناموفق بود"
                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun append(context: Context, sms: SmsModel) {
        val classified = SmsValidator.classify(SmsRawModel(sms.id?.toString().orEmpty(), sms.smsSender, sms.smsBody, sms.transactionDate, sms.transactionDateTime.toString()))
        val timestamp = reportTimestamp()
        val version = appVersion(context)
        val row = SmsReportCsv.row(listOf(timestamp, version, Build.VERSION.SDK_INT, sms.id, sms.smsSender, sms.smsBody, sms.bankName, sms.bankAccountNumber, sms.transactionType, sms.transactionAmount, sms.transactionDate, sms.transactionTime, sms.transactionDateTime, sms.bankCardBalance, sms.categoryIds.joinToString(";"), sms.description, sms.isModified, sms.isSeen, classified?.type, classified?.amount))
        appendFile(context, FILE_NAME, URI_KEY, SmsReportCsv.header, row)
    }

    private fun reportTimestamp() = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ROOT).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }.format(Date())

    @Suppress("DEPRECATION")
    private fun appVersion(context: Context) = context.packageManager.getPackageInfo(context.packageName, 0).versionName

    @Synchronized
    private fun appendFile(context: Context, fileName: String, uriKey: String, header: List<String>, rows: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) appendMediaStore(context, fileName, uriKey, header, rows)
        else appendLegacy(context, fileName, header, rows)
    }

    private fun initialContents(header: List<String>, rows: String) = "\uFEFF" + SmsReportCsv.row(header) + rows

    private fun appendMediaStore(context: Context, fileName: String, uriKey: String, header: List<String>, rows: String) {
        val resolver = context.contentResolver
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val saved = prefs.getString(uriKey, null)?.let(Uri::parse)
        val existing = saved?.takeIf { uri ->
            runCatching { resolver.query(uri, arrayOf(MediaStore.MediaColumns._ID), null, null, null)?.use { it.moveToFirst() } == true }.getOrDefault(false)
        }
        if (existing != null) {
            requireNotNull(resolver.openOutputStream(existing, "wa")).use { it.write(rows.toByteArray(StandardCharsets.UTF_8)) }
            return
        }
        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, "text/csv")
            put(MediaStore.MediaColumns.RELATIVE_PATH, "${Environment.DIRECTORY_DOWNLOADS}/Monify/")
        }
        val uri = requireNotNull(resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values))
        try {
            requireNotNull(resolver.openOutputStream(uri, "w")).use { it.write(initialContents(header, rows).toByteArray(StandardCharsets.UTF_8)) }
            prefs.edit().putString(uriKey, uri.toString()).apply()
        } catch (error: Exception) {
            resolver.delete(uri, null, null)
            throw error
        }
    }

    @Suppress("DEPRECATION")
    private fun appendLegacy(context: Context, fileName: String, header: List<String>, rows: String) {
        val directory = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "Monify")
        check(directory.isDirectory || directory.mkdirs())
        val file = File(directory, fileName)
        val contents = if (!file.exists() || file.length() == 0L) initialContents(header, rows) else rows
        FileOutputStream(file, true).use { it.write(contents.toByteArray(StandardCharsets.UTF_8)) }
        MediaScannerConnection.scanFile(context, arrayOf(file.absolutePath), arrayOf("text/csv"), null)
    }
}
