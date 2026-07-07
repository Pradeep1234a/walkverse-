package com.walkverse.ai.ui

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.walkverse.ai.data.local.db.WalkDatabase
import com.walkverse.ai.data.local.pref.WalkPreferences
import com.walkverse.ai.data.repository.WalkRepositoryImpl
import com.walkverse.ai.data.health.HealthConnectManager
import com.walkverse.ai.domain.model.*
import com.walkverse.ai.domain.repository.WalkRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.Instant
import kotlin.random.Random

class WalkViewModel(application: Application) : AndroidViewModel(application) {

    private val db = WalkDatabase.getDatabase(application)
    private val repository: WalkRepository = WalkRepositoryImpl(db)
    private val preferences = WalkPreferences(application)
    private val healthConnectManager = HealthConnectManager(application)

    // UI state flows from DataStore
    val userName = preferences.userNameFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), "Walker")
    val dailyGoal = preferences.dailyGoalFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), 8000)
    val selectedTheme = preferences.themeFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), "ZINC")
    val darkModeEnabled = preferences.darkModeFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), true)
    val onboardingCompleted = preferences.onboardingCompletedFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), false)
    val healthConnectSyncEnabled = preferences.healthConnectEnabledFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), false)
    val streakCount = preferences.streakCountFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), 0)
    val userXp = preferences.userXpFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), 0)
    val userLevel = preferences.userLevelFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), 1)
    val gems = preferences.gemsFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), 20)

    // Real-time Activity Recognition and Confidence Score
    val currentActivity = com.walkverse.ai.data.sensor.StepDetectorService.currentActivity
    val currentConfidence = com.walkverse.ai.data.sensor.StepDetectorService.currentConfidence

    // UI state flows from Room Database
    val stepsHistory = repository.getStepsFlow().stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())
    val challenges = repository.getChallengesFlow().stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())
    val achievements = repository.getAchievementsFlow().stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())
    val petState = repository.getPetStateFlow().stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)
    val gardenPlants = repository.getGardenPlantsFlow().stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())
    val storyChapters = repository.getStoryChaptersFlow().stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    // Today's steps record
    private val _todayRecord = MutableStateFlow<DailyStepsRecord?>(null)
    val todayRecord: StateFlow<DailyStepsRecord?> = _todayRecord

    init {
        // Observe steps and updates today's record
        viewModelScope.launch {
            stepsHistory.collect { history ->
                val todayDate = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
                val today = history.firstOrNull { it.date == todayDate }
                if (today != null) {
                    _todayRecord.value = today
                } else {
                    // Create initial steps record for today if missing
                    val newRecord = DailyStepsRecord(
                        date = todayDate,
                        steps = 0,
                        goal = dailyGoal.value,
                        distanceKm = 0.0,
                        caloriesKcal = 0.0,
                        durationMinutes = 0
                    )
                    repository.insertSteps(newRecord)
                    _todayRecord.value = newRecord
                }
            }
        }

        // Periodically sync with Health Connect if enabled
        viewModelScope.launch {
            healthConnectSyncEnabled.collect { enabled ->
                if (enabled) {
                    syncHealthConnectSteps()
                }
            }
        }
    }

    fun completeOnboarding(name: String, stepGoal: Int, petType: String) {
        viewModelScope.launch {
            preferences.saveUserName(name)
            preferences.saveDailyGoal(stepGoal)
            preferences.saveOnboardingCompleted(true)

            // Setup selected pet
            val initialPet = PetState(
                name = if (petType == "CAT") "Whiskers" else if (petType == "DOG") "Rex" else if (petType == "DRAGON") "Draco" else "Sparky",
                type = petType,
                level = 1,
                xp = 0,
                xpNeeded = 100,
                hunger = 80,
                happiness = 80,
                gems = gems.value,
                lastSyncTime = System.currentTimeMillis()
            )
            repository.savePetState(initialPet)
        }
    }

    fun updateUserName(name: String) {
        viewModelScope.launch {
            preferences.saveUserName(name)
        }
    }

    fun updateDailyGoal(goal: Int) {
        viewModelScope.launch {
            preferences.saveDailyGoal(goal)
            // Update today's record in database
            val today = todayRecord.value
            if (today != null) {
                repository.insertSteps(today.copy(goal = goal))
            }
        }
    }

    fun updateTheme(theme: String) {
        viewModelScope.launch {
            preferences.saveTheme(theme)
        }
    }

    fun toggleDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            preferences.saveDarkMode(enabled)
        }
    }

    fun toggleHealthConnect(enabled: Boolean) {
        viewModelScope.launch {
            if (enabled && healthConnectManager.isAvailable) {
                // Request should be handled in UI, but we toggle setting here
                preferences.saveHealthConnectEnabled(true)
                syncHealthConnectSteps()
            } else {
                preferences.saveHealthConnectEnabled(false)
            }
        }
    }

    // Health Connect Sync
    fun syncHealthConnectSteps() {
        viewModelScope.launch {
            if (healthConnectManager.isAvailable && healthConnectManager.hasAllPermissions()) {
                val startOfDay = LocalDate.now().atStartOfDay(java.time.ZoneId.systemDefault()).toInstant()
                val now = Instant.now()
                
                val hcSteps = healthConnectManager.readDailySteps(startOfDay, now)
                val hcDistance = healthConnectManager.readDailyDistance(startOfDay, now)
                val hcCalories = healthConnectManager.readDailyCalories(startOfDay, now)

                val today = todayRecord.value
                if (today != null && hcSteps > today.steps) {
                    val deltaSteps = hcSteps - today.steps
                    val updatedRecord = today.copy(
                        steps = hcSteps,
                        distanceKm = if (hcDistance > 0) hcDistance else hcSteps * 0.00076,
                        caloriesKcal = if (hcCalories > 0) hcCalories else hcSteps * 0.042,
                        durationMinutes = hcSteps / 95
                    )
                    repository.insertSteps(updatedRecord)
                    processStepsWalked(deltaSteps, hcSteps)
                }
            }
        }
    }



    // Distribute rewards, progress challenges, achievements, pet and plants
    private suspend fun processStepsWalked(deltaSteps: Int, totalStepsToday: Int) {
        val goal = dailyGoal.value
        
        // 1. Streak updates
        val todayDate = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
        val lastActive = preferences.lastActiveDateFlow.first()
        val currentStreak = streakCount.value

        if (lastActive != todayDate) {
            preferences.saveLastActiveDate(todayDate)
            if (totalStepsToday >= goal) {
                preferences.saveStreakCount(1)
            }
        } else {
            // Check if goal was met just now
            val oldSteps = totalStepsToday - deltaSteps
            if (oldSteps < goal && totalStepsToday >= goal) {
                preferences.saveStreakCount(currentStreak + 1)
            }
        }

        // 2. XP & Gems
        val xpGain = deltaSteps / 10
        if (xpGain > 0) {
            val nextXpTotal = userXp.value + xpGain
            val limit = userLevel.value * 1000
            if (nextXpTotal >= limit) {
                preferences.saveUserLevel(userLevel.value + 1)
                preferences.saveUserXp(nextXpTotal - limit)
                preferences.saveGems(gems.value + (userLevel.value * 5))
            } else {
                preferences.saveUserXp(nextXpTotal)
            }
        }

        // Gems milestone (1 gem per 1000 steps)
        val oldMilestone = (totalStepsToday - deltaSteps) / 1000
        val newMilestone = totalStepsToday / 1000
        val gemsEarned = (newMilestone - oldMilestone).coerceAtLeast(0)
        if (gemsEarned > 0) {
            preferences.saveGems(gems.value + gemsEarned)
        }

        // 3. Challenges progress
        val list = challenges.value
        for (c in list) {
            if (!c.completed) {
                var current = c.currentValue
                if (c.type == ChallengeType.DAILY) {
                    current = totalStepsToday.coerceAtMost(c.targetValue)
                } else {
                    current = (current + deltaSteps).coerceAtMost(c.targetValue)
                }
                val isCompleted = current >= c.targetValue
                repository.updateChallenge(c.copy(currentValue = current, completed = isCompleted))
            }
        }

        // 4. Achievements progress
        val allSteps = stepsHistory.value.sumOf { it.steps }
        val goalsMet = stepsHistory.value.count { it.steps >= it.goal }
        val achievementsList = achievements.value
        for (a in achievementsList) {
            if (!a.unlocked) {
                var current = a.currentValue
                when (a.id) {
                    "ach_1", "ach_2", "ach_3" -> current = allSteps
                    "ach_4" -> current = goalsMet
                    "ach_5" -> current = petState.value?.level ?: 1
                }
                val unlocked = current >= a.targetValue
                repository.updateAchievement(a.copy(currentValue = current.coerceAtMost(a.targetValue), unlocked = unlocked))
            }
        }

        // 5. Story Chapters
        val activeChapter = storyChapters.value.firstOrNull { it.isUnlocked && !it.isCompleted }
        if (activeChapter != null) {
            val newProgress = (activeChapter.stepsCompleted + deltaSteps).coerceAtMost(activeChapter.targetSteps)
            val completed = newProgress >= activeChapter.targetSteps
            repository.updateStoryChapter(activeChapter.copy(stepsCompleted = newProgress, isCompleted = completed))
            
            if (completed) {
                val nextId = "story_${activeChapter.id.substringAfter("story_").toInt() + 1}"
                val next = storyChapters.value.firstOrNull { it.id == nextId }
                if (next != null) {
                    repository.updateStoryChapter(next.copy(isUnlocked = true))
                }
            }
        }

        // 6. Garden growth
        val plantList = gardenPlants.value
        for (plant in plantList) {
            if (plant.growthProgress < 1.0f) {
                val progressIncrement = deltaSteps.toFloat() / 10000f // 10k steps to grow fully
                val nextProgress = (plant.growthProgress + progressIncrement).coerceAtMost(1.0f)
                repository.updatePlant(plant.copy(growthProgress = nextProgress, stepsContributed = plant.stepsContributed + deltaSteps))
            }
        }

        // 7. Virtual Pet
        val pet = petState.value
        if (pet != null) {
            val petXpGain = deltaSteps / 5
            var newPetXp = pet.xp + petXpGain
            var newLevel = pet.level
            var petLimit = newLevel * 100

            while (newPetXp >= petLimit) {
                newLevel++
                newPetXp -= petLimit
                petLimit = newLevel * 100
            }

            val stepsFactor = deltaSteps / 1000f
            val hungerDecrease = (stepsFactor * 3).toInt().coerceAtLeast(0)
            val happinessDecrease = (stepsFactor * 2).toInt().coerceAtLeast(0)

            val newHunger = (pet.hunger - hungerDecrease).coerceAtLeast(0)
            val newHappiness = (pet.happiness - happinessDecrease).coerceAtLeast(0)

            repository.savePetState(
                pet.copy(
                    level = newLevel,
                    xp = newPetXp,
                    xpNeeded = petLimit,
                    hunger = newHunger,
                    happiness = newHappiness,
                    lastSyncTime = System.currentTimeMillis()
                )
            )
        }
    }

    // UI actions: Claim rewards
    fun claimChallengeRewards(challenge: Challenge) {
        viewModelScope.launch {
            if (challenge.completed && !challenge.claimed) {
                repository.updateChallenge(challenge.copy(claimed = true))
                preferences.saveGems(gems.value + challenge.rewardGems)
                
                // Add user XP
                val currentXp = userXp.value
                val level = userLevel.value
                val newXp = currentXp + challenge.rewardXp
                val limit = level * 1000
                if (newXp >= limit) {
                    preferences.saveUserLevel(level + 1)
                    preferences.saveUserXp(newXp - limit)
                } else {
                    preferences.saveUserXp(newXp)
                }
            }
        }
    }

    // UI actions: Virtual Pet Care
    fun feedPet() {
        viewModelScope.launch {
            val pet = petState.value ?: return@launch
            if (gems.value >= 5 && pet.hunger < 100) {
                preferences.saveGems(gems.value - 5)
                repository.savePetState(
                    pet.copy(
                        hunger = (pet.hunger + 25).coerceAtMost(100),
                        happiness = (pet.happiness + 10).coerceAtMost(100),
                        xp = pet.xp + 20
                    )
                )
            }
        }
    }

    fun playWithPet() {
        viewModelScope.launch {
            val pet = petState.value ?: return@launch
            if (gems.value >= 3 && pet.happiness < 100) {
                preferences.saveGems(gems.value - 3)
                repository.savePetState(
                    pet.copy(
                        happiness = (pet.happiness + 20).coerceAtMost(100),
                        xp = pet.xp + 15
                    )
                )
            }
        }
    }

    // UI actions: Walk Garden
    fun plantSeed(species: String, row: Int, col: Int) {
        viewModelScope.launch {
            if (gems.value >= 15) {
                preferences.saveGems(gems.value - 15)
                val id = "plant_${System.currentTimeMillis()}"
                val newPlant = GardenPlant(
                    id = id,
                    name = "${species.lowercase().replaceFirstChar { it.uppercase() }}",
                    species = species,
                    growthProgress = 0.0f,
                    plantedAt = System.currentTimeMillis(),
                    stepsContributed = 0,
                    row = row,
                    col = col
                )
                repository.insertPlant(newPlant)
            }
        }
    }

    fun waterPlant(plant: GardenPlant) {
        viewModelScope.launch {
            if (gems.value >= 2 && plant.growthProgress < 1.0f) {
                preferences.saveGems(gems.value - 2)
                repository.updatePlant(
                    plant.copy(
                        growthProgress = (plant.growthProgress + 0.15f).coerceAtMost(1.0f)
                    )
                )
            }
        }
    }

    fun harvestPlant(plant: GardenPlant) {
        viewModelScope.launch {
            if (plant.growthProgress >= 1.0f) {
                repository.deletePlant(plant)
                // Reward user
                val harvestGemsReward = when (plant.species) {
                    "ROSE" -> 25
                    "SUNFLOWER" -> 30
                    "TULIP" -> 35
                    "BONSAI" -> 50
                    else -> 20
                }
                preferences.saveGems(gems.value + harvestGemsReward)

                // Increment achievement count for Green Thumb (ach_6)
                val ach6 = achievements.value.firstOrNull { it.id == "ach_6" }
                if (ach6 != null && !ach6.unlocked) {
                    val count = ach6.currentValue + 1
                    repository.updateAchievement(ach6.copy(currentValue = count, unlocked = count >= ach6.targetValue))
                }
            }
        }
    }
}
