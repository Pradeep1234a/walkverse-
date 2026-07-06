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
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import kotlin.random.Random

class StepDetectorService : Service(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var stepCounterSensor: Sensor? = null
    private var stepDetectorSensor: Sensor? = null

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private lateinit var database: WalkDatabase
    private lateinit var preferences: WalkPreferences

    private var initialStepsFromDb = 0
    private var sensorBaseline = -1f
    private var todayDateStr = ""
    
    // Filtering fields for step count accuracy
    private var lastStepTime = 0L
    private var stepBuffer = 0
    private var isWalkingActive = false
    private var lastRawSensorValue = -1f

    private val STEP_INTERVAL_MIN = 300L // Min 300ms spacing between steps
    private val STEP_INTERVAL_MAX = 2000L // Max 2s spacing to maintain walking active
    private val MIN_CONSECUTIVE_STEPS = 6 // Buffer 6 steps before recording

    private val NOTIFICATION_ID = 5005
    private val CHANNEL_ID = "walkverse_sensor_tracking"

    override fun onCreate() {
        super.onCreate()
        Log.d("StepDetectorService", "Service onCreate")
        
        database = WalkDatabase.getDatabase(applicationContext)
        preferences = WalkPreferences(applicationContext)
        todayDateStr = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        stepDetectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)

        // Load today's starting steps from DB
        serviceScope.launch {
            val record = database.stepsDao().getStepsForDate(todayDateStr)
            initialStepsFromDb = record?.steps ?: 0
            updateNotification(initialStepsFromDb)
        }

        // Register sensors
        stepCounterSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
            Log.d("StepDetectorService", "Registered TYPE_STEP_COUNTER")
        }
        
        // Register step detector as backup/immediate updates
        stepDetectorSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
            Log.d("StepDetectorService", "Registered TYPE_STEP_DETECTOR")
        }

        if (stepCounterSensor == null && stepDetectorSensor == null) {
            Log.e("StepDetectorService", "No step sensors available on this device!")
        }
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

    private fun registerStepEvent(currentTime: Long): Int {
        val timeDiff = currentTime - lastStepTime
        
        // 1. Reject high-frequency noise (e.g. vibration, shaking)
        if (timeDiff < STEP_INTERVAL_MIN) {
            return 0
        }
        
        var stepsToApply = 0
        
        // 2. Continuous walking filter
        if (timeDiff > STEP_INTERVAL_MAX) {
            // User stopped walking or took an isolated action (jump, bump, drop)
            isWalkingActive = false
            stepBuffer = 1 // Start a new verification sequence
        } else {
            if (isWalkingActive) {
                // User is actively walking, count immediately
                stepsToApply = 1
            } else {
                stepBuffer++
                if (stepBuffer >= MIN_CONSECUTIVE_STEPS) {
                    isWalkingActive = true
                    stepsToApply = stepBuffer // Flush all buffered steps!
                    stepBuffer = 0
                }
            }
        }
        
        lastStepTime = currentTime
        return stepsToApply
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return

        serviceScope.launch {
            val currentDate = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
            
            // Check if day transitioned, if so reset baselines
            if (currentDate != todayDateStr) {
                todayDateStr = currentDate
                initialStepsFromDb = 0
                sensorBaseline = if (event.sensor.type == Sensor.TYPE_STEP_COUNTER) event.values[0] else -1f
                lastRawSensorValue = if (event.sensor.type == Sensor.TYPE_STEP_COUNTER) event.values[0] else -1f
                lastStepTime = 0L
                stepBuffer = 0
                isWalkingActive = false
            }

            if (event.sensor.type == Sensor.TYPE_STEP_COUNTER) {
                val sensorValue = event.values[0]
                Log.d("StepDetectorService", "Sensor TYPE_STEP_COUNTER raw: $sensorValue")

                if (lastRawSensorValue == -1f) {
                    lastRawSensorValue = sensorValue
                }

                // Handle reboot: sensor resets to 0
                if (sensorValue < lastRawSensorValue) {
                    lastRawSensorValue = sensorValue
                    val record = database.stepsDao().getStepsForDate(todayDateStr)
                    initialStepsFromDb = record?.steps ?: 0
                }

                val sensorDelta = (sensorValue - lastRawSensorValue).toInt()
                lastRawSensorValue = sensorValue

                // Reject negative numbers or unrealistic high delta (e.g. vehicular speeds > 15 steps per check)
                if (sensorDelta <= 0 || sensorDelta > 15) {
                    return@launch
                }

                // Simulate delta events passing through our walking filter
                val currentTime = System.currentTimeMillis()
                var stepsToApply = 0
                for (i in 0 until sensorDelta) {
                    val simulatedTime = lastStepTime + 500L
                    val currentSimTime = if (simulatedTime < currentTime) simulatedTime else currentTime
                    stepsToApply += registerStepEvent(currentSimTime)
                }

                if (stepsToApply > 0) {
                    val currentTodayRecord = database.stepsDao().getStepsForDate(todayDateStr)
                    val currentStepsInDb = currentTodayRecord?.steps ?: 0
                    val nextSteps = currentStepsInDb + stepsToApply
                    updateTodayStepsInDb(nextSteps, stepsToApply)
                }

            } else if (event.sensor.type == Sensor.TYPE_STEP_DETECTOR) {
                // If step counter sensor isn't available, rely on detector pulses passing through filter
                if (stepCounterSensor == null) {
                    val currentTime = System.currentTimeMillis()
                    val stepsToApply = registerStepEvent(currentTime)
                    if (stepsToApply > 0) {
                        val currentTodayRecord = database.stepsDao().getStepsForDate(todayDateStr)
                        val currentStepsInDb = currentTodayRecord?.steps ?: 0
                        val nextSteps = currentStepsInDb + stepsToApply
                        updateTodayStepsInDb(nextSteps, stepsToApply)
                    }
                }
            }
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

        // Process games/rewards milestones in real time as the user walks!
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
