package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.BodyMetric
import com.example.data.model.Challenge
import com.example.data.model.MealLog
import com.example.data.model.NutritionProfile
import com.example.data.model.PersonalRecord
import com.example.data.model.ReminderSettings
import com.example.data.model.SocialPost
import com.example.data.model.WorkoutLog
import com.example.data.model.WorkoutTemplate
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutDao {
    @Query("SELECT * FROM workout_templates ORDER BY id ASC")
    fun getAllTemplates(): Flow<List<WorkoutTemplate>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTemplates(templates: List<WorkoutTemplate>)

    @Query("SELECT * FROM workout_logs ORDER BY dateTimestamp DESC")
    fun getAllLogs(): Flow<List<WorkoutLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: WorkoutLog): Long

    @Query("DELETE FROM workout_logs WHERE id = :id")
    suspend fun deleteLogById(id: Long)
}

@Dao
interface ProgressDao {
    @Query("SELECT * FROM body_metrics ORDER BY timestamp DESC")
    fun getAllMetrics(): Flow<List<BodyMetric>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMetric(metric: BodyMetric): Long

    @Query("DELETE FROM body_metrics WHERE id = :id")
    suspend fun deleteMetric(id: Long)

    @Query("SELECT * FROM personal_records ORDER BY exerciseName ASC")
    fun getAllPrs(): Flow<List<PersonalRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdatePr(pr: PersonalRecord)
}

@Dao
interface NutritionDao {
    @Query("SELECT * FROM nutrition_profile WHERE id = 1")
    fun getProfile(): Flow<NutritionProfile?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProfile(profile: NutritionProfile)

    @Query("SELECT * FROM meal_logs ORDER BY dateTimestamp DESC")
    fun getAllMeals(): Flow<List<MealLog>>

    @Query("SELECT * FROM meal_logs WHERE dateTimestamp >= :startOfDay AND dateTimestamp <= :endOfDay ORDER BY dateTimestamp ASC")
    fun getMealsForDate(startOfDay: Long, endOfDay: Long): Flow<List<MealLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeal(meal: MealLog): Long

    @Query("DELETE FROM meal_logs WHERE id = :id")
    suspend fun deleteMeal(id: Long)
}

@Dao
interface SocialDao {
    @Query("SELECT * FROM social_posts ORDER BY timestamp DESC")
    fun getAllPosts(): Flow<List<SocialPost>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPost(post: SocialPost): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPosts(posts: List<SocialPost>)

    @Update
    suspend fun updatePost(post: SocialPost)

    @Query("UPDATE social_posts SET isLikedByMe = :isLiked, likesCount = :newCount WHERE id = :id")
    suspend fun updateLikeStatus(id: Long, isLiked: Boolean, newCount: Int)
}

@Dao
interface ChallengeDao {
    @Query("SELECT * FROM challenges ORDER BY id ASC")
    fun getAllChallenges(): Flow<List<Challenge>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChallenges(challenges: List<Challenge>)

    @Update
    suspend fun updateChallenge(challenge: Challenge)

    @Query("UPDATE challenges SET isJoined = :joined, currentProgress = currentProgress + :progressDelta WHERE id = :id")
    suspend fun addProgress(id: Long, joined: Boolean, progressDelta: Double)
}

@Dao
interface ReminderSettingsDao {
    @Query("SELECT * FROM reminder_settings WHERE id = 1")
    fun getSettings(): Flow<ReminderSettings?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSettings(settings: ReminderSettings)
}
