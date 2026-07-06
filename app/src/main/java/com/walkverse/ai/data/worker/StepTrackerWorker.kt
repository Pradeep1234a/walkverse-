package com.walkverse.ai.data.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.walkverse.ai.data.local.db.WalkDatabase
import com.walkverse.ai.data.local.db.DailyStepsEntity
import com.walkverse.ai.data.local.pref.WalkPreferences
import com.walkverse.ai.data.health.HealthConnectManager
import com.walkverse.ai.domain.model.ChallengeType
import kotlinx.coroutines.flow.first
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import kotlin.random.Random

class StepTrackerWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    private val database = WalkDatabase.getDatabase(appContext)
    private val preferences = WalkPreferences(appContext)
    private val healthConnectManager = HealthConnectManager(appContext)

    companion object {
        private const val CHANNEL_ID = "walkverse_alerts"
        private const val NOTIFICATION_ID = 1001
    }

    override suspend fun doWork(): Result {
        Log.d("StepTrackerWorker", "Running step tracking background sync...")
        
        try {
            val todayDate = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
            val currentStepsRecord = database.stepsDao().getStepsForDate(todayDate)

            var stepsWalkedToday = currentStepsRecord?.steps ?: 0
            val dailyGoal = preferences.dailyGoalFlow.first()

            // 1. Sync with Health Connect if enabled and available
            val hcEnabled = preferences.healthConnectEnabledFlow.first()
            if (hcEnabled && healthConnectManager.isAvailable && healthConnectManager.hasAllPermissions()) {
                val startOfDay = LocalDate.now().atStartOfDay(java.time.ZoneId.systemDefault()).toInstant()
                val now = Instant.now()
                val hcSteps = healthConnectManager.readDailySteps(startOfDay, now)
                
                if (hcSteps > stepsWalkedToday) {
                    stepsWalkedToday = hcSteps
                }
            } else {
                // If Health Connect is not used, simulate background step activity randomly for the demo/offline usage
                // (e.g. user walked 100-300 steps in the last interval)
                val simulateIncrement = Random.nextInt(150, 450)
                stepsWalkedToday += simulateIncrement
            }

            // Calculate distance, calories, duration
            // Average step is 0.75m. So distance = steps * 0.00075 km
            val distanceKm = stepsWalkedToday * 0.00076
            // Average calories is 0.04 kcal per step
            val caloriesKcal = stepsWalkedToday * 0.042
            // Average walking speed is 100 steps per minute
            val durationMinutes = stepsWalkedToday / 95

            val newRecord = DailyStepsEntity(
                date = todayDate,
                steps = stepsWalkedToday,
                goal = dailyGoal,
                distanceKm = distanceKm,
                caloriesKcal = caloriesKcal,
                durationMinutes = durationMinutes
            )

            // Save record
            database.stepsDao().insertSteps(newRecord)

            // Calculate step delta since last sync to distribute XP, grow plants, etc.
            val lastRecordedSteps = currentStepsRecord?.steps ?: 0
            val stepDelta = (stepsWalkedToday - lastRecordedSteps).coerceAtLeast(0)

            if (stepDelta > 0) {
                processStepMilestones(stepDelta, stepsWalkedToday, dailyGoal, todayDate)
            }

            return Result.success()
        } catch (e: Exception) {
            Log.e("StepTrackerWorker", "Error in background step sync", e)
            return Result.retry()
        }
    }

    private suspend fun processStepMilestones(stepDelta: Int, totalStepsToday: Int, dailyGoal: Int, todayDate: String) {
        // 1. Update User Streak & Last Active Date
        val lastActiveDate = preferences.lastActiveDateFlow.first()
        val currentStreak = preferences.streakCountFlow.first()

        if (lastActiveDate != todayDate) {
            if (lastActiveDate.isNotEmpty()) {
                val lastDate = LocalDate.parse(lastActiveDate)
                val dayDiff = ChronoUnit.DAYS.between(lastDate, LocalDate.now())
                if (dayDiff == 1L) {
                    // Consecutive day
                    if (totalStepsToday >= dailyGoal) {
                        preferences.saveStreakCount(currentStreak + 1)
                        sendNotification("Streak Extended!", "You are on a ${currentStreak + 1}-day streak! Keep walking!")
                    }
                } else if (dayDiff > 1L) {
                    // Streak broken
                    preferences.saveStreakCount(if (totalStepsToday >= dailyGoal) 1 else 0)
                }
            } else {
                if (totalStepsToday >= dailyGoal) {
                    preferences.saveStreakCount(1)
                }
            }
            preferences.saveLastActiveDate(todayDate)
        } else {
            // Check if user just hit their goal today to increase streak
            if (lastRecordedGoalWasMet(totalStepsToday - stepDelta, totalStepsToday, dailyGoal)) {
                preferences.saveStreakCount(currentStreak + 1)
                sendNotification("Daily Goal Met! 🎉", "You've reached your daily goal of $dailyGoal steps! Streak extended to ${currentStreak + 1}!")
            }
        }

        // 2. Award User XP and Level Up
        val earnedXp = stepDelta / 10 // 1 XP per 10 steps
        if (earnedXp > 0) {
            val currentXp = preferences.userXpFlow.first()
            val currentLevel = preferences.userLevelFlow.first()
            val newXpTotal = currentXp + earnedXp
            val xpNeededForNextLevel = currentLevel * 1000

            if (newXpTotal >= xpNeededForNextLevel) {
                preferences.saveUserLevel(currentLevel + 1)
                preferences.saveUserXp(newXpTotal - xpNeededForNextLevel)
                preferences.saveGems(preferences.gemsFlow.first() + (currentLevel * 5)) // Level up reward
                sendNotification("Leveled Up! 🚀", "Congratulations! You reached Level ${currentLevel + 1}! Earned ${currentLevel * 5} Gems.")
            } else {
                preferences.saveUserXp(newXpTotal)
            }
        }

        // 3. Award Gems for Steps Walked
        val oldGemsMilestone = (totalStepsToday - stepDelta) / 1000
        val newGemsMilestone = totalStepsToday / 1000
        val gemsGained = (newGemsMilestone - oldGemsMilestone).coerceAtLeast(0)
        if (gemsGained > 0) {
            val currentGems = preferences.gemsFlow.first()
            preferences.saveGems(currentGems + gemsGained)
        }

        // 4. Update Challenges Progress
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
                    if (isCompletedNow) {
                        sendNotification("Challenge Completed! 🏆", "You completed: ${challenge.title}! Claim your rewards now.")
                    }
                }
            }
        }

        // 5. Update Achievements Progress
        val achievements = database.achievementsDao().getAllAchievementsFlow().first()
        val totalStepsAllTime = database.stepsDao().getAllSteps().sumOf { it.steps } + stepDelta
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
                    "ach_6" -> {
                        // Handled when plants are harvested
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
                    if (isUnlockedNow) {
                        sendNotification("Achievement Unlocked! 🎖️", "Unlocked badge: ${ach.title}!")
                    }
                }
            }
        }

        // 6. Update Story Chapter Progress
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
                sendNotification("Story Adventure Completed! 📖", "You finished ${activeChapter.title}! The next chapter is unlocked.")
                // Unlock next chapter
                val nextChapterId = "story_${activeChapter.id.substringAfter("story_").toInt() + 1}"
                val nextChapter = storyChapters.firstOrNull { it.id == nextChapterId }
                if (nextChapter != null) {
                    database.storyDao().updateChapter(nextChapter.copy(isUnlocked = true))
                }
            }
        }

        // 7. Update Virtual Pet
        val pet = database.petStateDao().getPetState()
        if (pet != null) {
            val petXpGained = stepDelta / 5 // 1 pet XP per 5 steps
            var newPetXp = pet.xp + petXpGained
            var newPetLevel = pet.level
            var petXpNeeded = pet.xpNeeded

            while (newPetXp >= petXpNeeded) {
                newPetLevel++
                newPetXp -= petXpNeeded
                petXpNeeded = newPetLevel * 100
                sendNotification("Pet Leveled Up! 🐱", "${pet.name} reached level $newPetLevel!")
            }

            // Deplete hunger and happiness slowly
            // For every 1000 steps, reduce hunger by 3 and happiness by 2 (pet gets tired/hungry)
            val stepsFactor = stepDelta / 1000f
            val hungerReduction = (stepsFactor * 4).toInt().coerceAtLeast(0)
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

        // 8. Update Garden Plants
        val plants = database.gardenDao().getAllPlantsFlow().first()
        for (plant in plants) {
            if (plant.growthProgress < 1.0f) {
                // Growth rate depends on steps. E.g. fully grown in 10,000 steps.
                val growthRate = stepDelta.toFloat() / 10000f
                val newProgress = (plant.growthProgress + growthRate).coerceAtMost(1.0f)
                val plantSteps = plant.stepsContributed + stepDelta

                database.gardenDao().updatePlant(
                    plant.copy(
                        growthProgress = newProgress,
                        stepsContributed = plantSteps
                    )
                )

                if (newProgress >= 1.0f) {
                    sendNotification("Plant Fully Grown! 🌸", "Your ${plant.species.lowercase()} has bloomed in the walk garden!")
                    
                    // Update achievement 6 progress
                    val ach6 = database.achievementsDao().getAllAchievementsFlow().first().firstOrNull { it.id == "ach_6" }
                    if (ach6 != null && !ach6.unlocked) {
                        val newCount = ach6.currentValue + 1
                        val isUnlockedNow = newCount >= ach6.targetValue
                        database.achievementsDao().updateAchievement(
                            ach6.copy(
                                currentValue = newCount,
                                unlocked = isUnlockedNow
                            )
                        )
                        if (isUnlockedNow) {
                            sendNotification("Achievement Unlocked! 🎖️", "Unlocked badge: ${ach6.title}!")
                        }
                    }
                }
            }
        }
    }

    private fun lastRecordedGoalWasMet(oldSteps: Int, newSteps: Int, goal: Int): Boolean {
        return oldSteps < goal && newSteps >= goal
    }

    private fun sendNotification(title: String, message: String) {
        val context = applicationContext
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "WalkVerse AI Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifies about walking milestones, challenges, and rewards."
            }
            notificationManager.createNotificationChannel(channel)
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(android.R.drawable.ic_dialog_info) // Fallback standard icon
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        try {
            notificationManager.notify(Random.nextInt(), builder.build())
        } catch (e: SecurityException) {
            Log.e("StepTrackerWorker", "Missing notification permission", e)
        }
    }
}
