package dev.estaki.myFinancialApp.workers

import android.content.Context
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object WorkersEngine {
    fun scheduleSmsRemindersWithWorkManager(duration: Long, timeUnit: TimeUnit, context: Context) {
        val request = OneTimeWorkRequestBuilder<SmsReminderWorker>()
            .setInitialDelay(duration, timeUnit)
            .build()
        WorkManager.getInstance(context).enqueue(request)
    }
}
