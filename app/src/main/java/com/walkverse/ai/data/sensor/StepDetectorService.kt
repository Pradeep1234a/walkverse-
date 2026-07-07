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
import com.walkverse.ai.domain.model.ChallengeType
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import kotlin.math.sqrt

class StepDetectorService : Service(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var stepCounterSensor: Sensor? = null
    private var stepDetectorSensor: Sensor? = null
    private var accelerometer: Sensor? = null
    private var gyroscope: Sensor? = null

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private lateinit var database: WalkDatabase
    private lateinit var preferences: WalkPreferences

    private var initialStepsFromDb = 0
    private var todayDateStr = ""

    // Low-frequency sliding windows for motion validation (30 samples at ~5Hz = 6 seconds)
    private val accelWindow = mutableListOf<Float>()
    private val gyroWindow = mutableListOf<Float>()
    private val WINDOW_SIZE = 30

    // Adaptive user calibration parameter (user's average walking variance)
    private var adaptiveWalkVarianceThreshold = 0.5f
    
    // Reboot-resilient step counter calibration baseline
    private var lastRawSensorValue = -1f

    // Track when walking motion was last validated
    private var lastWalkMotionTime = 0L

    private var lastStepTime = 0L
    private var isWalkingActive = false

    private val NOTIFICATION_ID = 5005
    private val CHANNEL_ID = "walkverse_sensor_tracking"

    companion object {
        // Expose live fused activity states to the UI
        private val _currentActivity = MutableStateFlow("Stationary")
        val currentActivity = _currentActivity.asStateFlow()

        private val _currentConfidence = MutableStateFlow(1.0f)
        val currentConfidence = _currentConfidence.asStateFlow()

        // Inputs from Google Activity Recognition Client
        private val _apiActivity = MutableStateFlow("Stationary")
        private val _apiConfidence = MutableStateFlow(1.0f)

        fun updateActivityRecognitionState(activity: String, confidence: Float) {
            _apiActivity.value = activity
            _apiConfidence.value = confidence
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
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

        // Load today's starting steps from DB
        serviceScope.launch {
            val record = database.stepsDao().getStepsForDate(todayDateStr)
            initialStepsFromDb = record?.steps ?: 0
            updateNotification(initialStepsFromDb)
        }

        // Restore raw sensor baseline from local SharedPreferences
        val prefs = getSharedPreferences("walkverse_sensor_prefs", Context.MODE_PRIVATE)
        lastRawSensorValue = prefs.getFloat("last_raw_sensor_value", -1f)

        // Register hardware step sensors
        stepCounterSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
            Log.d("StepDetectorService", "Authoritative TYPE_STEP_COUNTER registered.")
        }
        stepDetectorSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
            Log.d("StepDetectorService", "Authoritative TYPE_STEP_DETECTOR registered.")
        }
        
        // Register Accelerometer/Gyroscope at low-power NORMAL rate strictly for gate validation
        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
        gyroscope?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }

        // Request Activity Recognition updates from Google Play Services
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
            Sensor.TYPE_ACCELEROMETER -> {
                val ax = event.values[0]
                val ay = event.values[1]
                val az = event.values[2]
                val mag = sqrt(ax * ax + ay * ay + az * az)
                synchronized(accelWindow) {
                    accelWindow.add(mag)
                    if (accelWindow.size > WINDOW_SIZE) accelWindow.removeAt(0)
                }
                validateMotionActivity()
            }
            Sensor.TYPE_GYROSCOPE -> {
                val rx = event.values[0]
                val ry = event.values[1]
                val rz = event.values[2]
                val mag = sqrt(rx * rx + ry * ry + rz * rz)
                synchronized(gyroWindow) {
                    gyroWindow.add(mag)
                    if (gyroWindow.size > WINDOW_SIZE) gyroWindow.removeAt(0)
                }
            }
            Sensor.TYPE_STEP_COUNTER -> {
                processAuthoritativeStepCounter(event.values[0])
            }
            Sensor.TYPE_STEP_DETECTOR -> {
                processFallbackStepDetector()
            }
        }
    }

    /**
     * Low-power motion validation logic. Updates currentActivity and currentConfidence
     * strictly to serve as a validation gate for the hardware sensors.
     */
    private fun validateMotionActivity() {
        val accelList = synchronized(accelWindow) { accelWindow.toList() }
        val gyroList = synchronized(gyroWindow) { gyroWindow.toList() }

        if (accelList.size < WINDOW_SIZE || gyroList.size < WINDOW_SIZE) {
            _currentActivity.value = "Calibrating..."
            _currentConfidence.value = 0.5f
            return
        }

        // Calculate Acceleration Stats
        val accelMean = accelList.average().toFloat()
        var accelVar = 0.0f
        for (v in accelList) {
            accelVar += (v - accelMean) * (v - accelMean)
        }
        accelVar /= WINDOW_SIZE

        // Calculate Gyroscope Stats (swaying rotations)
        val gyroMean = gyroList.average().toFloat()

        // Zero-crossing frequency of acceleration magnitude to estimate step cadence
        var crossings = 0
        var lastSign = false
        for (i in 0 until accelList.size) {
            val diff = accelList[i] - accelMean
            val sign = diff > 0
            if (i > 0 && sign != lastSign && Math.abs(diff) > 0.15f) {
                crossings++
            }
            lastSign = sign
        }
        
        // 30 samples at ~5Hz (approx 6 seconds). Cadence frequency = crossings / 12
        val estimatedFrequencyHz = crossings / 12.0f

        var localActivity = "Stationary"
        var localConfidence = 0.95f

        when {
            // 1. Stationary: Very low acceleration variance and low rotation
            accelVar < 0.08f && gyroMean < 0.05f -> {
                localActivity = "Stationary"
                localConfidence = (1.0f - (accelVar * 4f + gyroMean * 3f)).coerceIn(0.8f, 1.0f)
            }

            // 2. Shaking / Vibrating: Chaotic high variance, high-frequency crossings
            accelVar > 15.0f || (accelVar > 8.0f && estimatedFrequencyHz > 3.8f) -> {
                localActivity = "Shaking/Vibrating"
                localConfidence = (accelVar / 30f).coerceIn(0.7f, 0.99f)
            }

            // 3. Vehicular Travel: Linear acceleration bumps, but lacks human gait arm-sway
            accelVar > 0.08f && accelVar < 0.65f && gyroMean < 0.16f -> {
                localActivity = "In Vehicle"
                localConfidence = (0.8f + (0.15f * (0.16f - gyroMean))).coerceIn(0.6f, 0.95f)
            }

            // 4. Running: High rhythmic variance and fast walking cadence
            accelVar >= 6.0f && estimatedFrequencyHz >= 2.2f && estimatedFrequencyHz <= 4.8f -> {
                localActivity = "Running"
                localConfidence = (0.8f + (estimatedFrequencyHz / 12f)).coerceIn(0.7f, 0.98f)
            }

            // 5. Walking: Moderate rhythmic variance, human cadence
            accelVar >= (adaptiveWalkVarianceThreshold * 0.5f) && accelVar < 6.0f && 
            estimatedFrequencyHz >= 0.8f && estimatedFrequencyHz <= 2.2f -> {
                localActivity = "Walking"
                localConfidence = (0.75f + (0.2f * (1f - Math.abs(estimatedFrequencyHz - 1.5f)))).coerceIn(0.6f, 0.98f)
                
                // Continuous calibration: adapt walk threshold slowly based on user's active variance
                adaptiveWalkVarianceThreshold = (0.95f * adaptiveWalkVarianceThreshold) + (0.05f * accelVar)
            }

            // Default
            else -> {
                localActivity = "Stationary"
                localConfidence = 0.8f
            }
        }

        // FUSE LOCAL SENSOR ANALYSIS WITH THE GOOGLE PLAY SERVICES ACTIVITY RECOGNITION API
        val apiAct = _apiActivity.value
        val apiConf = _apiConfidence.value

        var fusedActivity = localActivity
        var fusedConfidence = localConfidence

        if (apiAct == "In Vehicle" && apiConf > 0.65f) {
            // Highly confident vehicle detection overrides local walking classification
            fusedActivity = "In Vehicle"
            fusedConfidence = apiConf
        } else if (apiAct == "Walking" && apiConf > 0.7f && localActivity == "Calibrating...") {
            // API detects walking while local is still collecting samples
            fusedActivity = "Walking"
            fusedConfidence = apiConf
        } else if (localActivity == "Shaking/Vibrating") {
            // Direct physics shaking override
            fusedActivity = "Shaking/Vibrating"
            fusedConfidence = localConfidence
        } else if (apiAct == "Stationary" && apiConf > 0.85f && localActivity != "Walking" && localActivity != "Running") {
            fusedActivity = "Stationary"
            fusedConfidence = apiConf
        }

        _currentActivity.value = fusedActivity
        _currentConfidence.value = fusedConfidence

        if (fusedActivity == "Walking" || fusedActivity == "Running") {
            lastWalkMotionTime = System.currentTimeMillis()
        }
    }

    /**
     * Authoritative step counter algorithm. The hardware counter is the single source of truth.
     * We consume raw steps, validate them against the motion activity gate, and save them.
     */
    private fun processAuthoritativeStepCounter(sensorValue: Float) {
        serviceScope.launch {
            val currentDate = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
            checkDayTransition(currentDate, sensorValue)

            if (lastRawSensorValue == -1f) {
                lastRawSensorValue = sensorValue
                saveRawSensorValue(sensorValue)
            }

            // Handle reboot: raw resets to 0
            if (sensorValue < lastRawSensorValue) {
                lastRawSensorValue = sensorValue
                saveRawSensorValue(sensorValue)
                val record = database.stepsDao().getStepsForDate(todayDateStr)
                initialStepsFromDb = record?.steps ?: 0
            }

            val sensorDelta = (sensorValue - lastRawSensorValue).toInt()
            
            if (sensorDelta <= 0) return@launch

            // Authoritative consumption: consume raw delta immediately to prevent duplication
            lastRawSensorValue = sensorValue
            saveRawSensorValue(sensorValue)

            // Reject impossible spikes (e.g. driving/shaking > 25 steps per check interval)
            if (sensorDelta > 25) {
                Log.d("StepDetectorService", "Rejected hardware spike delta: $sensorDelta")
                return@launch
            }

            // Authoritative Gate Validation:
            // Reject step deltas if user is In Vehicle, Stationary, Shaking/Vibrating,
            // unless walking motion occurred within the last 5 seconds (to prevent start lag).
            val activity = _currentActivity.value
            val timeSinceLastWalkMotion = System.currentTimeMillis() - lastWalkMotionTime
            val isGaitValid = activity == "Walking" || activity == "Running" || activity == "Calibrating..." || timeSinceLastWalkMotion < 5000L

            if (!isGaitValid) {
                Log.d("StepDetectorService", "Hardware steps discarded by validation layer: $sensorDelta (fusedActivity: $activity)")
                return@launch
            }

            // Hardware steps are authoritative: add sensorDelta directly to today's database steps!
            val currentTodayRecord = database.stepsDao().getStepsForDate(todayDateStr)
            val currentStepsInDb = currentTodayRecord?.steps ?: 0
            val nextSteps = currentStepsInDb + sensorDelta
            updateTodayStepsInDb(nextSteps, sensorDelta)
        }
    }

    /**
     * Fallback step detector algorithm. ONLY utilized on devices where TYPE_STEP_COUNTER is missing.
     */
    private fun processFallbackStepDetector() {
        if (stepCounterSensor == null) {
            serviceScope.launch {
                val currentDate = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
                checkDayTransition(currentDate, -1f)

                // Gate validation check
                val activity = _currentActivity.value
                val timeSinceLastWalkMotion = System.currentTimeMillis() - lastWalkMotionTime
                val isGaitValid = activity == "Walking" || activity == "Running" || activity == "Calibrating..." || timeSinceLastWalkMotion < 5000L

                if (!isGaitValid) {
                    return@launch
                }

                // Minimum step frequency interval (300ms)
                val currentTime = System.currentTimeMillis()
                if (currentTime - lastStepTime < 300L) {
                    return@launch
                }

                // Apply fallback step detector count
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
            isWalkingActive = false
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

        // Process game milestones in real time
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
            // Check if goal was met just now
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

        // Gems milestone (1 gem per 1000 steps)
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
