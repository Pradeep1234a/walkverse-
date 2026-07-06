package com.walkverse.ai.domain.model

import java.time.LocalDate

enum class ChallengeType {
    DAILY, WEEKLY
}

data class DailyStepsRecord(
    val date: String, // "YYYY-MM-DD"
    val steps: Int,
    val goal: Int,
    val distanceKm: Double,
    val caloriesKcal: Double,
    val durationMinutes: Int
) {
    val progress: Float
        get() = if (goal > 0) (steps.toFloat() / goal.toFloat()).coerceIn(0f, 1f) else 0f
}

data class Challenge(
    val id: String,
    val title: String,
    val description: String,
    val targetValue: Int,
    val currentValue: Int,
    val type: ChallengeType,
    val rewardXp: Int,
    val rewardGems: Int,
    val completed: Boolean,
    val claimed: Boolean
) {
    val progress: Float
        get() = if (targetValue > 0) (currentValue.toFloat() / targetValue.toFloat()).coerceIn(0f, 1f) else 0f
}

data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val targetValue: Int,
    val currentValue: Int,
    val unlocked: Boolean,
    val iconName: String,
    val rewardXp: Int
) {
    val progress: Float
        get() = if (targetValue > 0) (currentValue.toFloat() / targetValue.toFloat()).coerceIn(0f, 1f) else 0f
}

data class PetState(
    val name: String,
    val type: String, // "CAT", "DOG", "DRAGON", "ROBOT"
    val level: Int,
    val xp: Int,
    val xpNeeded: Int,
    val hunger: Int, // 0 - 100 (100 means full)
    val happiness: Int, // 0 - 100 (100 means happy)
    val gems: Int,
    val lastSyncTime: Long
) {
    val mood: String
        get() = when {
            happiness > 80 && hunger > 80 -> "Ecstatic 😊"
            happiness > 50 && hunger > 50 -> "Happy 🙂"
            hunger < 30 -> "Hungry 🍲"
            happiness < 30 -> "Bored 🧸"
            else -> "Fine 😐"
        }
}

data class GardenPlant(
    val id: String,
    val name: String,
    val species: String, // "ROSE", "SUNFLOWER", "TULIP", "CACTUS", "BONSAI"
    val growthProgress: Float, // 0.0 to 1.0
    val plantedAt: Long,
    val stepsContributed: Int,
    val row: Int,
    val col: Int
) {
    val stage: Int
        get() = when {
            growthProgress >= 1.0f -> 4 // Fully Grown
            growthProgress >= 0.6f -> 3 // Bud
            growthProgress >= 0.2f -> 2 // Sprout
            else -> 1 // Seed
        }
}

data class StoryChapter(
    val id: String,
    val title: String,
    val description: String,
    val targetSteps: Int,
    val stepsCompleted: Int,
    val narrative: String,
    val isUnlocked: Boolean,
    val isCompleted: Boolean
) {
    val progress: Float
        get() = if (targetSteps > 0) (stepsCompleted.toFloat() / targetSteps.toFloat()).coerceIn(0f, 1f) else 0f
}
