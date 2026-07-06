package com.walkverse.ai.data.health

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.DistanceRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.TotalCaloriesBurnedRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import java.time.Instant
import java.time.temporal.ChronoUnit

class HealthConnectManager(private val context: Context) {

    private val healthConnectClient: HealthConnectClient? by lazy {
        try {
            if (HealthConnectClient.getSdkStatus(context) == HealthConnectClient.SDK_AVAILABLE) {
                HealthConnectClient.getOrCreate(context)
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("HealthConnectManager", "Error initializing Health Connect client", e)
            null
        }
    }

    val isAvailable: Boolean
        get() = HealthConnectClient.getSdkStatus(context) == HealthConnectClient.SDK_AVAILABLE

    val requiredPermissions = setOf(
        HealthPermission.getReadPermission(StepsRecord::class),
        HealthPermission.getWritePermission(StepsRecord::class),
        HealthPermission.getReadPermission(DistanceRecord::class),
        HealthPermission.getWritePermission(DistanceRecord::class),
        HealthPermission.getReadPermission(TotalCaloriesBurnedRecord::class),
        HealthPermission.getWritePermission(TotalCaloriesBurnedRecord::class)
    )

    suspend fun hasAllPermissions(): Boolean {
        val client = healthConnectClient ?: return false
        return try {
            val granted = client.permissionController.getGrantedPermissions()
            granted.containsAll(requiredPermissions)
        } catch (e: Exception) {
            Log.e("HealthConnectManager", "Error checking permissions", e)
            false
        }
    }

    suspend fun readDailySteps(startTime: Instant, endTime: Instant): Int {
        val client = healthConnectClient ?: return 0
        return try {
            val response = client.readRecords(
                ReadRecordsRequest(
                    recordType = StepsRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                )
            )
            response.records.sumOf { it.count }.toInt()
        } catch (e: Exception) {
            Log.e("HealthConnectManager", "Error reading steps", e)
            0
        }
    }

    suspend fun readDailyDistance(startTime: Instant, endTime: Instant): Double {
        val client = healthConnectClient ?: return 0.0
        return try {
            val response = client.readRecords(
                ReadRecordsRequest(
                    recordType = DistanceRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                )
            )
            response.records.sumOf { it.distance.inKilometers }
        } catch (e: Exception) {
            Log.e("HealthConnectManager", "Error reading distance", e)
            0.0
        }
    }

    suspend fun readDailyCalories(startTime: Instant, endTime: Instant): Double {
        val client = healthConnectClient ?: return 0.0
        return try {
            val response = client.readRecords(
                ReadRecordsRequest(
                    recordType = TotalCaloriesBurnedRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                )
            )
            response.records.sumOf { it.energy.inKilocalories }
        } catch (e: Exception) {
            Log.e("HealthConnectManager", "Error reading calories", e)
            0.0
        }
    }

    suspend fun writeSteps(steps: Int, startTime: Instant, endTime: Instant) {
        val client = healthConnectClient ?: return
        try {
            val stepsRecord = StepsRecord(
                count = steps.toLong(),
                startTime = startTime,
                endTime = endTime,
                startZoneOffset = null,
                endZoneOffset = null
            )
            client.insertRecords(listOf(stepsRecord))
        } catch (e: Exception) {
            Log.e("HealthConnectManager", "Error writing steps", e)
        }
    }

    fun openHealthConnectSettings() {
        try {
            val intent = Intent(HealthConnectClient.ACTION_HEALTH_CONNECT_SETTINGS)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (e: Exception) {
            // Fallback to app details or play store if settings activity isn't found
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("market://details?id=com.google.android.apps.healthdata")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            try {
                context.startActivity(intent)
            } catch (ex: Exception) {
                Log.e("HealthConnectManager", "Could not open store link", ex)
            }
        }
    }
}
// Stub Rationale Activity required by Health Connect API
class RationaleActivity : android.app.Activity() {
    override fun onCreate(savedInstanceState: android.os.Bundle?) {
        super.onCreate(savedInstanceState)
        // Finish immediately, MainActivity handles details
        finish()
    }
}
