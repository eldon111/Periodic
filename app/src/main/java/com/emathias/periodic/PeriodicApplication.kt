package com.emathias.periodic

import android.app.Application
import android.util.Log
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager

import com.emathias.periodic.worker.ScheduledItemWorker
import dagger.hilt.EntryPoint
import dagger.hilt.EntryPoints
import dagger.hilt.InstallIn
import dagger.hilt.android.HiltAndroidApp
import dagger.hilt.components.SingletonComponent
import java.util.concurrent.TimeUnit
import javax.inject.Inject


@HiltAndroidApp
class PeriodicApplication : Application(), Configuration.Provider {



    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface HiltWorkerFactoryEntryPoint {
        fun workerFactory(): HiltWorkerFactory
    }

    override val workManagerConfiguration: Configuration =
        Configuration.Builder()
            .setWorkerFactory(
                EntryPoints.get(this, HiltWorkerFactoryEntryPoint::class.java).workerFactory()
            )
            .build()

    val periodicWorkRequest =
        PeriodicWorkRequestBuilder<ScheduledItemWorker>(15, TimeUnit.MINUTES).build()
    val oneTimeWorkRequest =
        OneTimeWorkRequestBuilder<ScheduledItemWorker>().build()

    override fun onCreate() {
        super.onCreate()

        WorkManager
            .getInstance(applicationContext)
            .enqueue(oneTimeWorkRequest)
    }

    companion object {
        private const val TAG = "PeriodicApplication"
    }
}