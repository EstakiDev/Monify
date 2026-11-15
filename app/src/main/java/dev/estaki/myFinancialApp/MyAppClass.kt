package dev.estaki.myFinancialApp

import android.app.Application
import android.util.Log
import dagger.hilt.android.HiltAndroidApp
import dev.estaki.ui_utils.AppAppearance
import timber.log.Timber

@HiltAndroidApp
class MyAppClass: Application() {
    override fun onCreate() {
        super.onCreate()
        AppAppearance.initialize(this)
        Timber.plant(Timber.DebugTree());
        Timber.tag("TAG").d("onCreate: MY_APP_CLASS")
    }
}
