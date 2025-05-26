package com.emathias.periodic.config

import android.content.Context
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AwsCredentialManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val sharedPrefs = context.getSharedPreferences("aws_config", Context.MODE_PRIVATE)

    /**
     * Set AWS credentials securely
     * In production, consider using Android Keystore for additional security
     */
    fun setCredentials(accessKeyId: String, secretAccessKey: String) {
        sharedPrefs.edit {
            putString("aws_access_key_id", accessKeyId)
            putString("aws_secret_access_key", secretAccessKey)
        }
    }

    /**
     * Check if credentials are configured
     */
    fun areCredentialsConfigured(): Boolean {
        val accessKeyId = sharedPrefs.getString("aws_access_key_id", "")
        val secretAccessKey = sharedPrefs.getString("aws_secret_access_key", "")
        return !accessKeyId.isNullOrEmpty() && !secretAccessKey.isNullOrEmpty()
    }

    /**
     * Clear stored credentials
     */
    fun clearCredentials() {
        sharedPrefs.edit {
            remove("aws_access_key_id")
            remove("aws_secret_access_key")
        }
    }

    /**
     * Get the configured region
     */
    fun getRegion(): String {
        return sharedPrefs.getString("aws_region", "us-east-1") ?: "us-east-1"
    }

    /**
     * Set the AWS region
     */
    fun setRegion(region: String) {
        sharedPrefs.edit {
            putString("aws_region", region)
        }
    }
}
