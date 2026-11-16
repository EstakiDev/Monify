package dev.estaki.myFinancialApp

import dev.estaki.ui_utils.localized

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import dagger.hilt.android.AndroidEntryPoint
import dev.estaki.domain.models.SmsRawModel
import dev.estaki.domain.models.BankCardModel
import dev.estaki.domain.processor.SmsProcessor
import dev.estaki.domain.usecases.CacheAllBankAccountToDb
import dev.estaki.domain.usecases.CacheSmsToDb
import dev.estaki.myFinancialApp.workers.WorkersEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class SmsReceiver : BroadcastReceiver() {
    @Inject lateinit var cacheSms: CacheSmsToDb
    @Inject lateinit var cacheAccounts: CacheAllBankAccountToDb

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return

        val pendingResult = goAsync()
        val appContext = context.applicationContext
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
                if (messages.isEmpty()) return@launch
                val sender = messages.firstOrNull()?.originatingAddress.orEmpty()
                val body = messages.joinToString(separator = "") { it.messageBody.orEmpty() }
                val receivedAt = messages.maxOfOrNull { it.timestampMillis } ?: System.currentTimeMillis()
                val raw = SmsRawModel(
                    _id = "",
                    senderName = sender,
                    description = body,
                    receiveDate = receivedAt.toString().convertToTime(),
                    receiveDateTime = receivedAt.toString()
                )
                val transaction = SmsProcessor(listOf(raw)).execute().firstOrNull() ?: return@launch

                val inserted = cacheSms(listOf(transaction)).first().firstOrNull() ?: return@launch
                if (inserted == -1L) return@launch
                NotificationHandler.showSmsNotification(
                    appContext,
                    title = localized("تراکنش بانکی جدید"),
                    message = localized("برای مشاهده جزئیات، برنامه را باز کنید.")
                )
                if (transaction.bankAccountNumber.count(Char::isDigit) >= 4) {
                    cacheAccounts(listOf(BankCardModel(
                        id = transaction.bankAccountNumber.hashCode(),
                        bankName = transaction.bankName,
                        bankAccountNumber = transaction.bankAccountNumber,
                        bankCardBalance = transaction.bankCardBalance,
                        bankCardNumber = null,
                        isItFromSms = true
                    ))).first()
                }
                WorkersEngine.scheduleSmsRemindersWithWorkManager(
                    duration = 4,
                    timeUnit = TimeUnit.HOURS,
                    context = appContext
                )
                WorkersEngine.scheduleSmsRemindersWithWorkManager(
                    duration = 12,
                    timeUnit = TimeUnit.HOURS,
                    context = appContext
                )
            } catch (exception: Exception) {
                Timber.e(exception, "Unable to process incoming SMS")
            } finally {
                pendingResult.finish()
            }
        }
    }
}
