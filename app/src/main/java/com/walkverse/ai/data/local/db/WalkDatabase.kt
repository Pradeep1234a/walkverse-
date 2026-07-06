package com.walkverse.ai.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        DailyStepsEntity::class,
        ChallengeEntity::class,
        AchievementEntity::class,
        PetStateEntity::class,
        GardenPlantEntity::class,
        StoryChapterEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class WalkDatabase : RoomDatabase() {
    abstract fun stepsDao(): StepsDao
    abstract fun challengesDao(): ChallengesDao
    abstract fun achievementsDao(): AchievementsDao
    abstract fun petStateDao(): PetStateDao
    abstract fun gardenDao(): GardenDao
    abstract fun storyDao(): StoryDao

    companion object {
        @Volatile
        private var INSTANCE: WalkDatabase? = null

        fun getDatabase(context: Context): WalkDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    WalkDatabase::class.java,
                    "walkverse_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
