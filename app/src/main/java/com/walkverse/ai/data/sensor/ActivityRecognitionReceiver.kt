package com.walkverse.ai.data.sensor

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.location.ActivityRecognitionResult
import com.google.android.gms.location.DetectedActivity

class ActivityRecognitionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (ActivityRecognitionResult.hasResult(intent)) {
            val result = ActivityRecognitionResult.extractResult(intent) ?: return
            val probableActivity = result.mostProbableActivity
            val confidence = probableActivity.confidence / 100f
            
            val activityName = when (probableActivity.type) {
                DetectedActivity.WALKING -> "Walking"
                DetectedActivity.RUNNING -> "Running"
                DetectedActivity.ON_FOOT -> "Walking"
                DetectedActivity.IN_VEHICLE -> "In Vehicle"
                DetectedActivity.ON_BICYCLE -> "In Vehicle"
                DetectedActivity.STILL -> "Stationary"
                DetectedActivity.TILTING -> "Stationary"
                else -> "Stationary"
            }

            Log.d("ActivityRecognition", "Detected: $activityName, Confidence: $confidence")
            StepDetectorService.updateActivityRecognitionState(activityName, confidence)
        }
    }
}
