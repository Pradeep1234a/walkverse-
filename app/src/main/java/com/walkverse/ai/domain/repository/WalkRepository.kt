package com.walkverse.ai.domain.repository

import com.walkverse.ai.domain.model.*
import kotlinx.coroutines.flow.Flow

interface WalkRepository {
    // Steps
    fun getStepsFlow(): Flow<List<DailyStepsRecord>>
    suspend fun getStepsForDate(date: String): DailyStepsRecord?
    suspend fun insertSteps(record: DailyStepsRecord)
    suspend fun getAllStepsDirect(): List<DailyStepsRecord>

    // Challenges
    fun getChallengesFlow(): Flow<List<Challenge>>
    suspend fun updateChallenge(challenge: Challenge)
    suspend fun getChallengeById(id: String): Challenge?
    suspend fun populateChallenges(challenges: List<Challenge>)

    // Achievements
    fun getAchievementsFlow(): Flow<List<Achievement>>
    suspend fun updateAchievement(achievement: Achievement)
    suspend fun populateAchievements(achievements: List<Achievement>)

    // Pet State
    fun getPetStateFlow(): Flow<PetState?>
    suspend fun getPetStateDirect(): PetState?
    suspend fun savePetState(pet: PetState)

    // Garden
    fun getGardenPlantsFlow(): Flow<List<GardenPlant>>
    suspend fun insertPlant(plant: GardenPlant)
    suspend fun updatePlant(plant: GardenPlant)
    suspend fun deletePlant(plant: GardenPlant)

    // Story chapters
    fun getStoryChaptersFlow(): Flow<List<StoryChapter>>
    suspend fun updateStoryChapter(chapter: StoryChapter)
    suspend fun populateStoryChapters(chapters: List<StoryChapter>)
}
