package com.example.panicshield

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import com.google.firebase.FirebaseApp
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
import androidx.work.Configuration


@HiltAndroidApp
class PanicShieldApplication : Application() , Configuration.Provider{

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
    get() = Configuration.Builder()
        .setWorkerFactory(workerFactory)
        .build()

    override fun onCreate() {
        super.onCreate()
    }
}