package com.emathias.periodic

import android.app.Application
import android.util.Log
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.emathias.periodic.config.AwsCredentialManager
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

    @Inject
    lateinit var awsCredentialManager: AwsCredentialManager

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

        // Initialize AWS credentials on app startup
        initializeAwsCredentials()

        WorkManager
            .getInstance(applicationContext)
            .enqueue(oneTimeWorkRequest)
    }

    /**
     * Initialize AWS credentials from BuildConfig if not already configured
     */
    private fun initializeAwsCredentials() {
        try {
            if (!awsCredentialManager.areCredentialsConfigured()) {
                Log.i(TAG, "Initializing AWS credentials from BuildConfig")

                awsCredentialManager.setCredentials(
                    accessKeyId = BuildConfig.AWS_ACCESS_KEY_ID,
                    secretAccessKey = BuildConfig.AWS_SECRET_ACCESS_KEY
                )

                awsCredentialManager.setRegion(BuildConfig.AWS_REGION)

                Log.i(TAG, "AWS credentials initialized successfully")
            } else {
                Log.i(TAG, "AWS credentials already configured")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize AWS credentials", e)
            // Don't crash the app if AWS setup fails
            // The user can still configure credentials manually through the setup screen
        }
    }

    companion object {
        private const val TAG = "PeriodicApplication"
    }
}