package dev.estaki.myFinancialApp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import dagger.hilt.android.AndroidEntryPoint
import dev.estaki.domain.models.SmsRawModel
import dev.estaki.domain.processor.SmsProcessor
import dev.estaki.domain.usecases.UpsertSms
import dev.estaki.myFinancialApp.workers.WorkersEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class SmsReceiver : BroadcastReceiver() {
    @Inject lateinit var upsertSms: UpsertSms

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

                // Persist before scheduling nonessential reminders so a scheduling failure cannot
                // lose the transaction itself.
                upsertSms(transaction)
                NotificationHandler.showSmsNotification(
                    appContext,
                    title = "تراکنش بانکی جدید",
                    message = "برای مشاهده جزئیات، برنامه را باز کنید."
                )
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
