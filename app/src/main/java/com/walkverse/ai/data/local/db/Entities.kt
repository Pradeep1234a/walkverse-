package com.walkverse.ai.data.local.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.walkverse.ai.domain.model.Challenge
import com.walkverse.ai.domain.model.ChallengeType
import com.walkverse.ai.domain.model.Achievement
import com.walkverse.ai.domain.model.PetState
import com.walkverse.ai.domain.model.GardenPlant
import com.walkverse.ai.domain.model.StoryChapter
import com.walkverse.ai.domain.model.DailyStepsRecord

@Entity(tableName = "daily_steps")
data class DailyStepsEntity(
    @PrimaryKey val date: String, // "YYYY-MM-DD"
    val steps: Int,
    val goal: Int,
    val distanceKm: Double,
    val caloriesKcal: Double,
    val durationMinutes: Int
) {
    fun toDomain() = DailyStepsRecord(date, steps, goal, distanceKm, caloriesKcal, durationMinutes)
    
    companion object {
        fun fromDomain(record: DailyStepsRecord) = DailyStepsEntity(
            record.date, record.steps, record.goal, record.distanceKm, record.caloriesKcal, record.durationMinutes
        )
    }
}

@Entity(tableName = "challenges")
data class ChallengeEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val targetValue: Int,
    val currentValue: Int,
    val type: String, // "DAILY", "WEEKLY"
    val rewardXp: Int,
    val rewardGems: Int,
    val completed: Boolean,
    val claimed: Boolean
) {
    fun toDomain() = Challenge(
        id, title, description, targetValue, currentValue,
        ChallengeType.valueOf(type), rewardXp, rewardGems, completed, claimed
    )

    companion object {
        fun fromDomain(c: Challenge) = ChallengeEntity(
            c.id, c.title, c.description, c.targetValue, c.currentValue,
            c.type.name, c.rewardXp, c.rewardGems, c.completed, c.claimed
        )
    }
}

@Entity(tableName = "achievements")
data class AchievementEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val targetValue: Int,
    val currentValue: Int,
    val unlocked: Boolean,
    val iconName: String,
    val rewardXp: Int
) {
    fun toDomain() = Achievement(id, title, description, targetValue, currentValue, unlocked, iconName, rewardXp)

    companion object {
        fun fromDomain(a: Achievement) = AchievementEntity(
            a.id, a.title, a.description, a.targetValue, a.currentValue, a.unlocked, a.iconName, a.rewardXp
        )
    }
}

@Entity(tableName = "pet_state")
data class PetStateEntity(
    @PrimaryKey val id: Int = 1, // Only 1 row for pet state
    val name: String,
    val type: String,
    val level: Int,
    val xp: Int,
    val xpNeeded: Int,
    val hunger: Int,
    val happiness: Int,
    val gems: Int,
    val lastSyncTime: Long
) {
    fun toDomain() = PetState(name, type, level, xp, xpNeeded, hunger, happiness, gems, lastSyncTime)

    companion object {
        fun fromDomain(p: PetState) = PetStateEntity(
            name = p.name, type = p.type, level = p.level, xp = p.xp, xpNeeded = p.xpNeeded,
            hunger = p.hunger, happiness = p.happiness, gems = p.gems, lastSyncTime = p.lastSyncTime
        )
    }
}

@Entity(tableName = "garden_plants")
data class GardenPlantEntity(
    @PrimaryKey val id: String,
    val name: String,
    val species: String,
    val growthProgress: Float,
    val plantedAt: Long,
    val stepsContributed: Int,
    val row: Int,
    val col: Int
) {
    fun toDomain() = GardenPlant(id, name, species, growthProgress, plantedAt, stepsContributed, row, col)

    companion object {
        fun fromDomain(p: GardenPlant) = GardenPlantEntity(
            p.id, p.name, p.species, p.growthProgress, p.plantedAt, p.stepsContributed, p.row, p.col
        )
    }
}

@Entity(tableName = "story_chapters")
data class StoryChapterEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val targetSteps: Int,
    val stepsCompleted: Int,
    val narrative: String,
    val isUnlocked: Boolean,
    val isCompleted: Boolean
) {
    fun toDomain() = StoryChapter(id, title, description, targetSteps, stepsCompleted, narrative, isUnlocked, isCompleted)

    companion object {
        fun fromDomain(s: StoryChapter) = StoryChapterEntity(
            s.id, s.title, s.description, s.targetSteps, s.stepsCompleted, s.narrative, s.isUnlocked, s.isCompleted
        )
    }
}
