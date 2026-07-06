package com.walkverse.ai.data.repository

import com.walkverse.ai.data.local.db.*
import com.walkverse.ai.domain.model.*
import com.walkverse.ai.domain.repository.WalkRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class WalkRepositoryImpl(
    private val database: WalkDatabase
) : WalkRepository {

    private val stepsDao = database.stepsDao()
    private val challengesDao = database.challengesDao()
    private val achievementsDao = database.achievementsDao()
    private val petStateDao = database.petStateDao()
    private val gardenDao = database.gardenDao()
    private val storyDao = database.storyDao()

    init {
        // Pre-populate data asynchronously on creation
        CoroutineScope(Dispatchers.IO).launch {
            initializeDefaultsIfNeeded()
        }
    }

    private suspend fun initializeDefaultsIfNeeded() {
        // 1. Initial challenges
        val existingChallenges = challengesDao.getAllChallengesFlow().firstOrNull()
        if (existingChallenges.isNullOrEmpty()) {
            val defaults = listOf(
                ChallengeEntity("daily_1", "Quick Stroll", "Walk 1,000 steps today.", 1000, 0, "DAILY", 20, 5, false, false),
                ChallengeEntity("daily_2", "Active Walk", "Walk 5,000 steps today.", 5000, 0, "DAILY", 50, 10, false, false),
                ChallengeEntity("daily_3", "10K Club", "Walk 10,000 steps today.", 10000, 0, "DAILY", 100, 20, false, false),
                ChallengeEntity("weekly_1", "Weekend Warrior", "Walk 15,000 steps in a week.", 15000, 0, "WEEKLY", 150, 30, false, false),
                ChallengeEntity("weekly_2", "Mega Marathoner", "Walk 50,000 steps in a week.", 50000, 0, "WEEKLY", 350, 80, false, false)
            )
            challengesDao.insertChallenges(defaults)
        }

        // 2. Initial achievements
        val existingAchievements = achievementsDao.getAllAchievementsFlow().firstOrNull()
        if (existingAchievements.isNullOrEmpty()) {
            val defaults = listOf(
                AchievementEntity("ach_1", "First Steps", "Reach a total of 1,000 steps.", 1000, 0, false, "DirectionsWalk", 20),
                AchievementEntity("ach_2", "Hiker Pro", "Reach a total of 10,000 steps.", 10000, 0, false, "Terrain", 50),
                AchievementEntity("ach_3", "Globe Trotter", "Reach a total of 100,000 steps.", 100000, 0, false, "Public", 150),
                AchievementEntity("ach_4", "Goal Achiever", "Reach your daily step goal 5 times.", 5, 0, false, "EmojiEvents", 100),
                AchievementEntity("ach_5", "Pet Pal", "Level up your virtual companion to level 5.", 5, 0, false, "Pets", 100),
                AchievementEntity("ach_6", "Green Thumb", "Grow your first plant to completion in Walk Garden.", 1, 0, false, "LocalFlorist", 100)
            )
            achievementsDao.insertAchievements(defaults)
        }

        // 3. Initial story chapters
        val existingStories = storyDao.getAllChaptersFlow().firstOrNull()
        if (existingStories.isNullOrEmpty()) {
            val defaults = listOf(
                StoryChapterEntity("story_1", "Chapter 1: The Whispering Woods", "Embark on your journey into the mysterious woods.", 5000, 0, 
                    "You step into the ancient canopy. The leaves rustle in a gentle breeze, whispering secrets of hikers past. Keep walking to discover where the path leads...", 
                    isUnlocked = true, isCompleted = false),
                StoryChapterEntity("story_2", "Chapter 2: The Shadowy Cavern", "Explore a dark, echoing cavern full of crystals.", 15000, 0, 
                    "The cavern air is cool and damp. Echoes of water droplets ring off the walls. Sparkly crystal clusters light up your path. To venture deeper, you must walk further...", 
                    isUnlocked = false, isCompleted = false),
                StoryChapterEntity("story_3", "Chapter 3: The Sunlit Peak", "Climb the Mountain of Whispers to witness the sunrise.", 30000, 0, 
                    "The summit is finally in sight. Below you lies the vast WalkVerse valley. The golden sun shines down upon your achievements. You did it!", 
                    isUnlocked = false, isCompleted = false)
            )
            storyDao.insertChapters(defaults)
        }

        // 4. Initial pet state
        val existingPet = petStateDao.getPetState()
        if (existingPet == null) {
            val defaultPet = PetStateEntity(
                name = "Buddy",
                type = "CAT",
                level = 1,
                xp = 0,
                xpNeeded = 100,
                hunger = 80,
                happiness = 80,
                gems = 10,
                lastSyncTime = System.currentTimeMillis()
            )
            petStateDao.insertOrUpdate(defaultPet)
        }
    }

    // Steps
    override fun getStepsFlow(): Flow<List<DailyStepsRecord>> {
        return stepsDao.getAllStepsFlow().map { list -> list.map { it.toDomain() } }
    }

    override suspend fun getStepsForDate(date: String): DailyStepsRecord? {
        return stepsDao.getStepsForDate(date)?.toDomain()
    }

    override suspend fun insertSteps(record: DailyStepsRecord) {
        stepsDao.insertSteps(DailyStepsEntity.fromDomain(record))
    }

    override suspend fun getAllStepsDirect(): List<DailyStepsRecord> {
        return stepsDao.getAllSteps().map { it.toDomain() }
    }

    // Challenges
    override fun getChallengesFlow(): Flow<List<Challenge>> {
        return challengesDao.getAllChallengesFlow().map { list -> list.map { it.toDomain() } }
    }

    override suspend fun updateChallenge(challenge: Challenge) {
        challengesDao.updateChallenge(ChallengeEntity.fromDomain(challenge))
    }

    override suspend fun getChallengeById(id: String): Challenge? {
        return challengesDao.getChallengeById(id)?.toDomain()
    }

    override suspend fun populateChallenges(challenges: List<Challenge>) {
        challengesDao.insertChallenges(challenges.map { ChallengeEntity.fromDomain(it) })
    }

    // Achievements
    override fun getAchievementsFlow(): Flow<List<Achievement>> {
        return achievementsDao.getAllAchievementsFlow().map { list -> list.map { it.toDomain() } }
    }

    override suspend fun updateAchievement(achievement: Achievement) {
        achievementsDao.updateAchievement(AchievementEntity.fromDomain(achievement))
    }

    override suspend fun populateAchievements(achievements: List<Achievement>) {
        achievementsDao.insertAchievements(achievements.map { AchievementEntity.fromDomain(it) })
    }

    // Pet State
    override fun getPetStateFlow(): Flow<PetState?> {
        return petStateDao.getPetStateFlow().map { it?.toDomain() }
    }

    override suspend fun getPetStateDirect(): PetState? {
        return petStateDao.getPetState()?.toDomain()
    }

    override suspend fun savePetState(pet: PetState) {
        petStateDao.insertOrUpdate(PetStateEntity.fromDomain(pet))
    }

    // Garden
    override fun getGardenPlantsFlow(): Flow<List<GardenPlant>> {
        return gardenDao.getAllPlantsFlow().map { list -> list.map { it.toDomain() } }
    }

    override suspend fun insertPlant(plant: GardenPlant) {
        gardenDao.insertPlant(GardenPlantEntity.fromDomain(plant))
    }

    override suspend fun updatePlant(plant: GardenPlant) {
        gardenDao.updatePlant(GardenPlantEntity.fromDomain(plant))
    }

    override suspend fun deletePlant(plant: GardenPlant) {
        gardenDao.deletePlant(GardenPlantEntity.fromDomain(plant))
    }

    // Story
    override fun getStoryChaptersFlow(): Flow<List<StoryChapter>> {
        return storyDao.getAllChaptersFlow().map { list -> list.map { it.toDomain() } }
    }

    override suspend fun updateStoryChapter(chapter: StoryChapter) {
        storyDao.updateChapter(StoryChapterEntity.fromDomain(chapter))
    }

    override suspend fun populateStoryChapters(chapters: List<StoryChapter>) {
        storyDao.insertChapters(chapters.map { StoryChapterEntity.fromDomain(it) })
    }
}
