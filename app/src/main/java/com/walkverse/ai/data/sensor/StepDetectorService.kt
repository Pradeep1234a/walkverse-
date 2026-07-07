package com.walkverse.ai.data.sensor

import android.app.*
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.walkverse.ai.MainActivity
import com.walkverse.ai.data.local.db.WalkDatabase
import com.walkverse.ai.data.local.db.DailyStepsEntity
import com.walkverse.ai.data.local.pref.WalkPreferences
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class StepDetectorService : Service(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var stepCounterSensor: Sensor? = null
    private var stepDetectorSensor: Sensor? = null

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private lateinit var database: WalkDatabase
    private lateinit var preferences: WalkPreferences

    private var initialStepsFromDb = 0
    private var todayDateStr = ""
    
    // Reboot-resilient step counter baseline
    private var lastRawSensorValue = -1f

    // Fallback step detector timing threshold
    private var lastStepTime = 0L

    private val NOTIFICATION_ID = 5005
    private val CHANNEL_ID = "walkverse_sensor_tracking"

    companion object {
        // Expose live activity states to the UI
        private val _currentActivity = MutableStateFlow("Stationary")
        val currentActivity = _currentActivity.asStateFlow()

        private val _currentConfidence = MutableStateFlow(1.0f)
        val currentConfidence = _currentConfidence.asStateFlow()

        fun updateActivityRecognitionState(activity: String, confidence: Float) {
            _currentActivity.value = activity
            _currentConfidence.value = confidence
            Log.d("StepDetectorService", "Activity Recognition State: $activity (Confidence: $confidence)")
        }
    }

    override fun onCreate() {
        super.onCreate()
        Log.d("StepDetectorService", "Service onCreate")
        
        database = WalkDatabase.getDatabase(applicationContext)
        preferences = WalkPreferences(applicationContext)
        todayDateStr = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        stepDetectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)

        // Load starting steps
        serviceScope.launch {
            val record = database.stepsDao().getStepsForDate(todayDateStr)
            initialStepsFromDb = record?.steps ?: 0
            updateNotification(initialStepsFromDb)
        }

        // Restore raw baseline from local SharedPreferences
        val prefs = getSharedPreferences("walkverse_sensor_prefs", Context.MODE_PRIVATE)
        lastRawSensorValue = prefs.getFloat("last_raw_sensor_value", -1f)

        // Register hardware pedometer
        stepCounterSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
            Log.d("StepDetectorService", "Registered hardware TYPE_STEP_COUNTER.")
        }
        
        // Register step detector fallback only
        stepDetectorSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
            Log.d("StepDetectorService", "Registered fallback TYPE_STEP_DETECTOR.")
        }

        // Request Activity Recognition updates
        requestActivityRecognitionUpdates()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("StepDetectorService", "Service onStartCommand")
        
        createNotificationChannel()
        val notification = buildNotification(initialStepsFromDb)
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID, 
                notification, 
                android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_HEALTH
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }

        return START_STICKY
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return

        when (event.sensor.type) {
            Sensor.TYPE_STEP_COUNTER -> {
                processAuthoritativeStepCounter(event.values[0])
            }
            Sensor.TYPE_STEP_DETECTOR -> {
                processFallbackStepDetector()
            }
        }
    }

    /**
     * Authoritative step counter algorithm. The hardware sensor counts steps.
     * We calculate the baseline delta, apply the validation gate, and write to storage.
     */
    private fun processAuthoritativeStepCounter(sensorValue: Float) {
        serviceScope.launch {
            val currentDate = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
            checkDayTransition(currentDate, sensorValue)

            if (lastRawSensorValue == -1f) {
                lastRawSensorValue = sensorValue
                saveRawSensorValue(sensorValue)
            }

            // Handle device reboot
            if (sensorValue < lastRawSensorValue) {
                lastRawSensorValue = sensorValue
                saveRawSensorValue(sensorValue)
                val record = database.stepsDao().getStepsForDate(todayDateStr)
                initialStepsFromDb = record?.steps ?: 0
            }

            val sensorDelta = (sensorValue - lastRawSensorValue).toInt()
            if (sensorDelta <= 0) return@launch

            // Consume delta immediately to avoid double counting
            lastRawSensorValue = sensorValue
            saveRawSensorValue(sensorValue)

            // Reject impossible spikes
            if (sensorDelta > 30) {
                Log.d("StepDetectorService", "Rejected raw hardware spike delta: $sensorDelta")
                return@launch
            }

            // Validation Gate: Ignore steps if user is travelling in a vehicle, cycling, or stationary
            val activity = _currentActivity.value
            val isGaitValid = activity == "Walking" || activity == "Running" || activity == "Stationary" || activity == "Calibrating..."

            if (activity == "In Vehicle" || activity == "On Bicycle") {
                Log.d("StepDetectorService", "Discarded hardware steps due to vehicular activity: $sensorDelta")
                return@launch
            }

            // Apply valid delta
            val currentTodayRecord = database.stepsDao().getStepsForDate(todayDateStr)
            val currentStepsInDb = currentTodayRecord?.steps ?: 0
            val nextSteps = currentStepsInDb + sensorDelta
            updateTodayStepsInDb(nextSteps, sensorDelta)
        }
    }

    /**
     * Fallback step detector. Only used when TYPE_STEP_COUNTER is absent.
     */
    private fun processFallbackStepDetector() {
        if (stepCounterSensor == null) {
            serviceScope.launch {
                val currentDate = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
                checkDayTransition(currentDate, -1f)

                // Validation Gate
                val activity = _currentActivity.value
                if (activity == "In Vehicle" || activity == "On Bicycle") {
                    return@launch
                }

                val currentTime = System.currentTimeMillis()
                if (currentTime - lastStepTime < 300L) {
                    return@launch
                }

                lastStepTime = currentTime
                
                val currentTodayRecord = database.stepsDao().getStepsForDate(todayDateStr)
                val currentStepsInDb = currentTodayRecord?.steps ?: 0
                val nextSteps = currentStepsInDb + 1
                updateTodayStepsInDb(nextSteps, 1)
            }
        }
    }

    private fun checkDayTransition(currentDate: String, sensorValue: Float) {
        if (currentDate != todayDateStr) {
            todayDateStr = currentDate
            initialStepsFromDb = 0
            
            if (sensorValue != -1f) {
                lastRawSensorValue = sensorValue
                saveRawSensorValue(sensorValue)
            }
            
            lastStepTime = 0L
        }
    }

    private fun saveRawSensorValue(value: Float) {
        val prefs = getSharedPreferences("walkverse_sensor_prefs", Context.MODE_PRIVATE)
        prefs.edit().putFloat("last_raw_sensor_value", value).apply()
    }

    private fun requestActivityRecognitionUpdates() {
        try {
            val client = com.google.android.gms.location.ActivityRecognition.getClient(this)
            val intent = Intent(this, ActivityRecognitionReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                this,
                1001,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            )
            client.requestActivityUpdates(3000L, pendingIntent)
            Log.d("StepDetectorService", "Activity Recognition updates requested.")
        } catch (e: SecurityException) {
            Log.e("StepDetectorService", "Missing Activity Recognition permission: ${e.message}")
        } catch (e: Exception) {
            Log.e("StepDetectorService", "Error initiating Activity Recognition updates: ${e.message}")
        }
    }

    private fun removeActivityRecognitionUpdates() {
        try {
            val client = com.google.android.gms.location.ActivityRecognition.getClient(this)
            val intent = Intent(this, ActivityRecognitionReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                this,
                1001,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            )
            client.removeActivityUpdates(pendingIntent)
            Log.d("StepDetectorService", "Activity Recognition updates removed.")
        } catch (e: Exception) {
            Log.e("StepDetectorService", "Error terminating Activity Recognition updates: ${e.message}")
        }
    }

    private suspend fun updateTodayStepsInDb(steps: Int, stepDelta: Int) {
        val goal = preferences.dailyGoalFlow.first()
        val distanceKm = steps * 0.00076
        val caloriesKcal = steps * 0.042
        val durationMinutes = steps / 95

        val updatedRecord = DailyStepsEntity(
            date = todayDateStr,
            steps = steps,
            goal = goal,
            distanceKm = distanceKm,
            caloriesKcal = caloriesKcal,
            durationMinutes = durationMinutes
        )
        database.stepsDao().insertSteps(updatedRecord)
        updateNotification(steps)

        // Process story, garden, challenges, pet milestones
        processStepMilestones(stepDelta, steps, goal)
    }

    private suspend fun processStepMilestones(stepDelta: Int, totalStepsToday: Int, dailyGoal: Int) {
        // 1. Update Streak & Last Active Date
        val lastActiveDate = preferences.lastActiveDateFlow.first()
        val currentStreak = preferences.streakCountFlow.first()

        if (lastActiveDate != todayDateStr) {
            preferences.saveLastActiveDate(todayDateStr)
            if (totalStepsToday >= dailyGoal) {
                preferences.saveStreakCount(1)
            }
        } else {
            val oldSteps = totalStepsToday - stepDelta
            if (oldSteps < dailyGoal && totalStepsToday >= dailyGoal) {
                preferences.saveStreakCount(currentStreak + 1)
            }
        }

        // 2. User Level & XP
        val earnedXp = stepDelta / 10
        if (earnedXp > 0) {
            val currentXp = preferences.userXpFlow.first()
            val currentLevel = preferences.userLevelFlow.first()
            val newXpTotal = currentXp + earnedXp
            val xpNeededForNextLevel = currentLevel * 1000

            if (newXpTotal >= xpNeededForNextLevel) {
                preferences.saveUserLevel(currentLevel + 1)
                preferences.saveUserXp(newXpTotal - xpNeededForNextLevel)
                preferences.saveGems(preferences.gemsFlow.first() + (currentLevel * 5))
            } else {
                preferences.saveUserXp(newXpTotal)
            }
        }

        // Gems milestone
        val oldGemsMilestone = (totalStepsToday - stepDelta) / 1000
        val newGemsMilestone = totalStepsToday / 1000
        val gemsGained = (newGemsMilestone - oldGemsMilestone).coerceAtLeast(0)
        if (gemsGained > 0) {
            val currentGems = preferences.gemsFlow.first()
            preferences.saveGems(currentGems + gemsGained)
        }

        // 3. Update Challenges
        val challenges = database.challengesDao().getAllChallengesFlow().first()
        for (challenge in challenges) {
            if (!challenge.completed) {
                var newProgress = challenge.currentValue
                if (challenge.type == "DAILY") {
                    newProgress = totalStepsToday.coerceAtMost(challenge.targetValue)
                } else if (challenge.type == "WEEKLY") {
                    newProgress = (challenge.currentValue + stepDelta).coerceAtMost(challenge.targetValue)
                }

                val isCompletedNow = newProgress >= challenge.targetValue
                if (newProgress != challenge.currentValue || isCompletedNow) {
                    database.challengesDao().updateChallenge(
                        challenge.copy(
                            currentValue = newProgress,
                            completed = isCompletedNow
                        )
                    )
                }
            }
        }

        // 4. Update Achievements
        val achievements = database.achievementsDao().getAllAchievementsFlow().first()
        val totalStepsAllTime = database.stepsDao().getAllSteps().sumOf { it.steps }
        val totalGoalsMetAllTime = database.stepsDao().getAllSteps().count { it.steps >= it.goal }

        for (ach in achievements) {
            if (!ach.unlocked) {
                var currentValue = ach.currentValue
                when (ach.id) {
                    "ach_1", "ach_2", "ach_3" -> currentValue = totalStepsAllTime
                    "ach_4" -> currentValue = totalGoalsMetAllTime
                    "ach_5" -> {
                        val pet = database.petStateDao().getPetState()
                        if (pet != null) {
                            currentValue = pet.level
                        }
                    }
                }

                val isUnlockedNow = currentValue >= ach.targetValue
                if (currentValue != ach.currentValue || isUnlockedNow) {
                    database.achievementsDao().updateAchievement(
                        ach.copy(
                            currentValue = currentValue.coerceAtMost(ach.targetValue),
                            unlocked = isUnlockedNow
                        )
                    )
                }
            }
        }

        // 5. Update Story Chapters
        val storyChapters = database.storyDao().getAllChaptersFlow().first()
        val activeChapter = storyChapters.firstOrNull { it.isUnlocked && !it.isCompleted }
        if (activeChapter != null) {
            val newStepsCompleted = (activeChapter.stepsCompleted + stepDelta).coerceAtMost(activeChapter.targetSteps)
            val isCompletedNow = newStepsCompleted >= activeChapter.targetSteps

            database.storyDao().updateChapter(
                activeChapter.copy(
                    stepsCompleted = newStepsCompleted,
                    isCompleted = isCompletedNow
                )
            )

            if (isCompletedNow) {
                val nextChapterId = "story_${activeChapter.id.substringAfter("story_").toInt() + 1}"
                val nextChapter = storyChapters.firstOrNull { it.id == nextChapterId }
                if (nextChapter != null) {
                    database.storyDao().updateChapter(nextChapter.copy(isUnlocked = true))
                }
            }
        }

        // 6. Update Garden Plants
        val plants = database.gardenDao().getAllPlantsFlow().first()
        for (plant in plants) {
            if (plant.growthProgress < 1.0f) {
                val growthRate = stepDelta.toFloat() / 10000f
                val newProgress = (plant.growthProgress + growthRate).coerceAtMost(1.0f)
                database.gardenDao().updatePlant(
                    plant.copy(
                        growthProgress = newProgress,
                        stepsContributed = plant.stepsContributed + stepDelta
                    )
                )
            }
        }

        // 7. Update Virtual Pet
        val pet = database.petStateDao().getPetState()
        if (pet != null) {
            val petXpGained = stepDelta / 5
            var newPetXp = pet.xp + petXpGained
            var newPetLevel = pet.level
            var petXpNeeded = pet.xpNeeded

            while (newPetXp >= petXpNeeded) {
                newPetLevel++
                newPetXp -= petXpNeeded
                petXpNeeded = newPetLevel * 100
            }

            val stepsFactor = stepDelta / 1000f
            val hungerReduction = (stepsFactor * 3).toInt().coerceAtLeast(0)
            val happinessReduction = (stepsFactor * 2).toInt().coerceAtLeast(0)

            val newHunger = (pet.hunger - hungerReduction).coerceIn(0, 100)
            val newHappiness = (pet.happiness - happinessReduction).coerceIn(0, 100)

            database.petStateDao().insertOrUpdate(
                pet.copy(
                    level = newPetLevel,
                    xp = newPetXp,
                    xpNeeded = petXpNeeded,
                    hunger = newHunger,
                    happiness = newHappiness,
                    lastSyncTime = System.currentTimeMillis()
                )
            )
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        Log.d("StepDetectorService", "Service onDestroy")
        sensorManager.unregisterListener(this)
        removeActivityRecognitionUpdates()
        serviceScope.cancel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "WalkVerse Active Tracking",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }

    private fun buildNotification(steps: Int): Notification {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("WalkVerse Active Tracking")
            .setContentText("Steps today: ${String.format("%,d", steps)}")
            .setSmallIcon(android.R.drawable.ic_menu_compass) // Fallback system icon
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    private fun updateNotification(steps: Int) {
        val notification = buildNotification(steps)
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, notification)
    }
}
