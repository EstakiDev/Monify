package dev.estaki.myFinancialApp.presentation.main

import android.content.ContentValues
import android.content.Context
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
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

internal object SmsReportWriter {
    private const val FILE_NAME = "monify-sms-report.csv"
    private const val PREFS = "debug_sms_report"
    private const val URI_KEY = "report_uri"
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

    @Synchronized
    private fun append(context: Context, sms: SmsModel) {
        val classified = SmsValidator.classify(SmsRawModel(sms.id?.toString().orEmpty(), sms.smsSender, sms.smsBody, sms.transactionDate, sms.transactionDateTime.toString()))
        val timestamp = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ROOT).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }.format(Date())
        @Suppress("DEPRECATION")
        val version = context.packageManager.getPackageInfo(context.packageName, 0).versionName
        val row = SmsReportCsv.row(listOf(timestamp, version, Build.VERSION.SDK_INT, sms.id, sms.smsSender, sms.smsBody, sms.bankName, sms.bankAccountNumber, sms.transactionType, sms.transactionAmount, sms.transactionDate, sms.transactionTime, sms.transactionDateTime, sms.bankCardBalance, sms.categoryIds.joinToString(";"), sms.description, sms.isModified, sms.isSeen, classified?.type, classified?.amount))
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) appendMediaStore(context, row) else appendLegacy(context, row)
    }

    private fun initialContents(row: String) = "\uFEFF" + SmsReportCsv.row(SmsReportCsv.header) + row

    private fun appendMediaStore(context: Context, row: String) {
        val resolver = context.contentResolver
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val saved = prefs.getString(URI_KEY, null)?.let(Uri::parse)
        val existing = saved?.takeIf { uri ->
            runCatching { resolver.query(uri, arrayOf(MediaStore.MediaColumns._ID), null, null, null)?.use { it.moveToFirst() } == true }.getOrDefault(false)
        }
        if (existing != null) {
            requireNotNull(resolver.openOutputStream(existing, "wa")).use { it.write(row.toByteArray(StandardCharsets.UTF_8)) }
            return
        }
        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, FILE_NAME)
            put(MediaStore.MediaColumns.MIME_TYPE, "text/csv")
            put(MediaStore.MediaColumns.RELATIVE_PATH, "${Environment.DIRECTORY_DOWNLOADS}/Monify/")
        }
        val uri = requireNotNull(resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values))
        try {
            requireNotNull(resolver.openOutputStream(uri, "w")).use { it.write(initialContents(row).toByteArray(StandardCharsets.UTF_8)) }
            prefs.edit().putString(URI_KEY, uri.toString()).apply()
        } catch (error: Exception) {
            resolver.delete(uri, null, null)
            throw error
        }
    }

    @Suppress("DEPRECATION")
    private fun appendLegacy(context: Context, row: String) {
        val directory = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "Monify")
        check(directory.isDirectory || directory.mkdirs())
        val file = File(directory, FILE_NAME)
        val contents = if (!file.exists() || file.length() == 0L) initialContents(row) else row
        FileOutputStream(file, true).use { it.write(contents.toByteArray(StandardCharsets.UTF_8)) }
        MediaScannerConnection.scanFile(context, arrayOf(file.absolutePath), arrayOf("text/csv"), null)
    }
}
