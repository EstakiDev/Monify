package dev.estaki.myFinancialApp.workers

import dev.estaki.ui_utils.localized

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import dev.estaki.myFinancialApp.NotificationHandler

class SmsReminderWorker(context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams) {
    override fun doWork(): Result {
        NotificationHandler.showSmsNotification(
            applicationContext,
            title = localized("یادآوری تراکنش بانکی"),
            message = localized("اگر تراکنشی انجام داده‌اید، جزئیات آن را در برنامه بررسی کنید.")
        )
        return Result.success()
    }
}
