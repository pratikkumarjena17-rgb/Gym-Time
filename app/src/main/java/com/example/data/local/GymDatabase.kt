package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.BodyMetric
import com.example.data.model.Challenge
import com.example.data.model.MealLog
import com.example.data.model.NutritionProfile
import com.example.data.model.PersonalRecord
import com.example.data.model.ReminderSettings
import com.example.data.model.SocialPost
import com.example.data.model.WorkoutLog
import com.example.data.model.WorkoutTemplate

@Database(
    entities = [
        WorkoutTemplate::class,
        WorkoutLog::class,
        BodyMetric::class,
        PersonalRecord::class,
        NutritionProfile::class,
        MealLog::class,
        SocialPost::class,
        Challenge::class,
        ReminderSettings::class
    ],
    version = 1,
    exportSchema = false
)
abstract class GymDatabase : RoomDatabase() {
    abstract fun workoutDao(): WorkoutDao
    abstract fun progressDao(): ProgressDao
    abstract fun nutritionDao(): NutritionDao
    abstract fun socialDao(): SocialDao
    abstract fun challengeDao(): ChallengeDao
    abstract fun reminderSettingsDao(): ReminderSettingsDao

    companion object {
        @Volatile
        private var INSTANCE: GymDatabase? = null

        fun getDatabase(context: Context): GymDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    GymDatabase::class.java,
                    "gym_tracker_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
