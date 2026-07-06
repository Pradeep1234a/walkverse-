package com.walkverse.ai.data.local.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface StepsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSteps(steps: DailyStepsEntity)

    @Query("SELECT * FROM daily_steps WHERE date = :date LIMIT 1")
    suspend fun getStepsForDate(date: String): DailyStepsEntity?

    @Query("SELECT * FROM daily_steps ORDER BY date DESC")
    fun getAllStepsFlow(): Flow<List<DailyStepsEntity>>

    @Query("SELECT * FROM daily_steps ORDER BY date DESC")
    suspend fun getAllSteps(): List<DailyStepsEntity>
}

@Dao
interface ChallengesDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChallenges(challenges: List<ChallengeEntity>)

    @Update
    suspend fun updateChallenge(challenge: ChallengeEntity)

    @Query("SELECT * FROM challenges")
    fun getAllChallengesFlow(): Flow<List<ChallengeEntity>>

    @Query("SELECT * FROM challenges WHERE id = :id LIMIT 1")
    suspend fun getChallengeById(id: String): ChallengeEntity?
}

@Dao
interface AchievementsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAchievements(achievements: List<AchievementEntity>)

    @Update
    suspend fun updateAchievement(achievement: AchievementEntity)

    @Query("SELECT * FROM achievements")
    fun getAllAchievementsFlow(): Flow<List<AchievementEntity>>
}

@Dao
interface PetStateDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(pet: PetStateEntity)

    @Query("SELECT * FROM pet_state WHERE id = 1 LIMIT 1")
    fun getPetStateFlow(): Flow<PetStateEntity?>

    @Query("SELECT * FROM pet_state WHERE id = 1 LIMIT 1")
    suspend fun getPetState(): PetStateEntity?
}

@Dao
interface GardenDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlant(plant: GardenPlantEntity)

    @Update
    suspend fun updatePlant(plant: GardenPlantEntity)

    @Delete
    suspend fun deletePlant(plant: GardenPlantEntity)

    @Query("SELECT * FROM garden_plants")
    fun getAllPlantsFlow(): Flow<List<GardenPlantEntity>>
}

@Dao
interface StoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChapters(chapters: List<StoryChapterEntity>)

    @Update
    suspend fun updateChapter(chapter: StoryChapterEntity)

    @Query("SELECT * FROM story_chapters ORDER BY id ASC")
    fun getAllChaptersFlow(): Flow<List<StoryChapterEntity>>
}
