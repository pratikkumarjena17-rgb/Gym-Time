package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

// --- WORKOUT TRACKING ---

@Entity(tableName = "workout_templates")
data class WorkoutTemplate(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String,
    val category: String, // e.g., "Strength", "Hypertrophy", "Endurance"
    val difficulty: String, // "Beginner", "Intermediate", "Advanced"
    val estimatedDurationMin: Int,
    val exerciseNames: String // Comma-separated
)

@Entity(tableName = "workout_logs")
data class WorkoutLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val dateTimestamp: Long = System.currentTimeMillis(),
    val durationMinutes: Int,
    val totalVolumeKg: Double,
    val exercisesCompleted: Int,
    val caloriesBurned: Int,
    val notes: String = "",
    val detailsJson: String = "" // Serialized exercise sets
)

data class SetLog(
    val setNumber: Int,
    val weightKg: Double,
    val reps: Int,
    val isCompleted: Boolean = false,
    val isPr: Boolean = false
)

data class ExerciseLog(
    val exerciseName: String,
    val muscleGroup: String,
    val sets: List<SetLog>
)

// --- PROGRESS MONITORING ---

@Entity(tableName = "body_metrics")
data class BodyMetric(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val weightKg: Double,
    val bodyFatPercentage: Double? = null,
    val chestCm: Double? = null,
    val waistCm: Double? = null,
    val armCm: Double? = null,
    val notes: String = ""
)

@Entity(tableName = "personal_records")
data class PersonalRecord(
    @PrimaryKey val exerciseName: String,
    val maxWeightKg: Double,
    val maxReps: Int,
    val estimated1RmKg: Double,
    val achievedTimestamp: Long = System.currentTimeMillis()
)

// --- NUTRITION PLANNER ---

@Entity(tableName = "nutrition_profile")
data class NutritionProfile(
    @PrimaryKey val id: Int = 1,
    val goal: String = "Muscle Gain", // "Muscle Gain", "Fat Loss", "Maintenance"
    val currentWeightKg: Double = 75.0,
    val targetWeightKg: Double = 80.0,
    val heightCm: Double = 178.0,
    val age: Int = 26,
    val gender: String = "Male",
    val activityLevel: String = "Moderate", // "Sedentary", "Moderate", "High", "Athlete"
    val dailyCalorieTarget: Int = 2650,
    val proteinGramsTarget: Int = 165,
    val carbsGramsTarget: Int = 300,
    val fatsGramsTarget: Int = 70,
    val waterLitersTarget: Double = 3.5
)

@Entity(tableName = "meal_logs")
data class MealLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val mealType: String, // "Breakfast", "Lunch", "Dinner", "Snack / Pre-Workout"
    val foodName: String,
    val calories: Int,
    val proteinG: Double,
    val carbsG: Double,
    val fatsG: Double,
    val dateTimestamp: Long = System.currentTimeMillis()
)

// --- SOCIAL ACHIEVEMENTS & COMMUNITY ---

@Entity(tableName = "social_posts")
data class SocialPost(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val authorName: String,
    val authorHandle: String,
    val authorAvatarHex: Long = 0xFFFF6B00,
    val timestamp: Long = System.currentTimeMillis(),
    val content: String,
    val workoutSummary: String? = null,
    val achievementBadge: String? = null,
    val likesCount: Int = 0,
    val isLikedByMe: Boolean = false,
    val commentsCount: Int = 0
)

// --- MONTHLY CHALLENGES ---

@Entity(tableName = "challenges")
data class Challenge(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String,
    val monthYear: String, // e.g. "October 2026"
    val targetType: String, // "Workouts", "Volume (kg)", "Cardio (km)", "Streak (days)"
    val currentProgress: Double,
    val targetGoal: Double,
    val unit: String,
    val daysLeft: Int,
    val participantsCount: Int,
    val isJoined: Boolean = false,
    val userRank: Int = 12,
    val rewardBadge: String
) {
    val isCompleted: Boolean get() = currentProgress >= targetGoal
}

// --- CELEBRATION & MILESTONES ---

enum class CelebrationType {
    MONTHLY_CHALLENGE,
    FITNESS_MILESTONE,
    PERSONAL_RECORD
}

data class CelebrationEvent(
    val type: CelebrationType,
    val title: String,
    val subtitle: String,
    val badgeIcon: String,
    val statHighlight: String,
    val description: String,
    val xpEarned: Int = 500,
    val rankOrTier: String = "Gold Tier",
    val timestamp: Long = System.currentTimeMillis()
)

data class FitnessMilestone(
    val id: String,
    val title: String,
    val description: String,
    val category: String, // "Strength", "Volume", "Consistency", "Body Recomp"
    val badgeIcon: String,
    val targetValue: Double,
    val currentValue: Double,
    val unit: String,
    val isAchieved: Boolean,
    val xpReward: Int = 350,
    val tier: String = "Gold"
)

// --- MOTIVATION & REMINDERS ---

@Entity(tableName = "reminder_settings")
data class ReminderSettings(
    @PrimaryKey val id: Int = 1,
    val dailyMotivationEnabled: Boolean = true,
    val motivationHour: Int = 8,
    val motivationMinute: Int = 0,
    val workoutReminderEnabled: Boolean = true,
    val workoutReminderHour: Int = 18,
    val workoutReminderMinute: Int = 0,
    val waterReminderEnabled: Boolean = false,
    val preferredMotivationQuote: String = "The body achieves what the mind believes."
)
