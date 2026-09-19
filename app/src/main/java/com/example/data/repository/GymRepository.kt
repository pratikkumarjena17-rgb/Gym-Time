package com.example.data.repository

import com.example.data.local.GymDatabase
import com.example.data.model.BodyMetric
import com.example.data.model.Challenge
import com.example.data.model.MealLog
import com.example.data.model.NutritionProfile
import com.example.data.model.PersonalRecord
import com.example.data.model.ReminderSettings
import com.example.data.model.SocialPost
import com.example.data.model.WorkoutLog
import com.example.data.model.WorkoutTemplate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Calendar

class GymRepository(private val database: GymDatabase) {
    private val workoutDao = database.workoutDao()
    private val progressDao = database.progressDao()
    private val nutritionDao = database.nutritionDao()
    private val socialDao = database.socialDao()
    private val challengeDao = database.challengeDao()
    private val reminderSettingsDao = database.reminderSettingsDao()

    // Workouts
    val workoutTemplates: Flow<List<WorkoutTemplate>> = workoutDao.getAllTemplates()
    val workoutLogs: Flow<List<WorkoutLog>> = workoutDao.getAllLogs()

    // Progress
    val bodyMetrics: Flow<List<BodyMetric>> = progressDao.getAllMetrics()
    val personalRecords: Flow<List<PersonalRecord>> = progressDao.getAllPrs()

    // Nutrition
    val nutritionProfile: Flow<NutritionProfile?> = nutritionDao.getProfile()
    val allMeals: Flow<List<MealLog>> = nutritionDao.getAllMeals()

    // Community
    val socialPosts: Flow<List<SocialPost>> = socialDao.getAllPosts()
    val challenges: Flow<List<Challenge>> = challengeDao.getAllChallenges()

    // Reminders
    val reminderSettings: Flow<ReminderSettings?> = reminderSettingsDao.getSettings()

    init {
        CoroutineScope(Dispatchers.IO).launch {
            seedInitialDataIfNeeded()
        }
    }

    private suspend fun seedInitialDataIfNeeded() {
        val templates = workoutDao.getAllTemplates().first()
        if (templates.isEmpty()) {
            workoutDao.insertTemplates(
                listOf(
                    WorkoutTemplate(
                        title = "Push Day: Chest, Shoulders & Triceps",
                        description = "Hypertrophy and strength session focusing on horizontal and overhead pressing.",
                        category = "Strength",
                        difficulty = "Intermediate",
                        estimatedDurationMin = 55,
                        exerciseNames = "Barbell Bench Press, Incline Dumbbell Press, Standing Overhead Press, Cable Lateral Raise, Tricep Rope Pushdown"
                    ),
                    WorkoutTemplate(
                        title = "Pull Day: Back & Biceps",
                        description = "Target lat width, upper back thickness, and bicep peaks with compound pulling.",
                        category = "Hypertrophy",
                        difficulty = "Intermediate",
                        estimatedDurationMin = 60,
                        exerciseNames = "Deadlift, Lat Pulldown, Barbell Bent-Over Row, Face Pull, Incline Dumbbell Curl, Hammer Curl"
                    ),
                    WorkoutTemplate(
                        title = "Leg Day: Quads, Hamstrings & Calves",
                        description = "Lower body power and mass builder with squats, hinges, and targeted isolations.",
                        category = "Strength",
                        difficulty = "Advanced",
                        estimatedDurationMin = 65,
                        exerciseNames = "Barbell Back Squat, Romanian Deadlift, Leg Press, Walking Dumbbell Lunges, Standing Calf Raise"
                    ),
                    WorkoutTemplate(
                        title = "Full Body Athletic Circuit",
                        description = "High intensity functional strength and metabolic conditioning.",
                        category = "Endurance",
                        difficulty = "All Levels",
                        estimatedDurationMin = 45,
                        exerciseNames = "Kettlebell Swing, Goblet Squat, Push-Ups, Pull-Ups, Dumbbell Thrusters, Hanging Leg Raise"
                    )
                )
            )
        }

        val logs = workoutDao.getAllLogs().first()
        if (logs.isEmpty()) {
            val now = System.currentTimeMillis()
            val dayMs = 86400000L
            workoutDao.insertLog(
                WorkoutLog(
                    title = "Push Day: Chest, Shoulders & Triceps",
                    dateTimestamp = now - dayMs * 3,
                    durationMinutes = 52,
                    totalVolumeKg = 8450.0,
                    exercisesCompleted = 5,
                    caloriesBurned = 420,
                    notes = "Felt super energized! Hit a clean 90kg bench for 6 reps."
                )
            )
            workoutDao.insertLog(
                WorkoutLog(
                    title = "Pull Day: Back & Biceps",
                    dateTimestamp = now - dayMs * 2,
                    durationMinutes = 58,
                    totalVolumeKg = 9800.0,
                    exercisesCompleted = 6,
                    caloriesBurned = 460,
                    notes = "Heavy deadlifts. Form felt solid on bent-over rows."
                )
            )
            workoutDao.insertLog(
                WorkoutLog(
                    title = "Leg Day: Quads, Hamstrings & Calves",
                    dateTimestamp = now - dayMs,
                    durationMinutes = 64,
                    totalVolumeKg = 11200.0,
                    exercisesCompleted = 5,
                    caloriesBurned = 530,
                    notes = "Deep squats. Legs are definitely going to feel this tomorrow!"
                )
            )
        }

        val metrics = progressDao.getAllMetrics().first()
        if (metrics.isEmpty()) {
            val now = System.currentTimeMillis()
            val dayMs = 86400000L
            progressDao.insertMetric(BodyMetric(timestamp = now - dayMs * 28, weightKg = 77.2, bodyFatPercentage = 16.5, chestCm = 101.0, waistCm = 83.5, armCm = 36.5, notes = "Starting cut cycle"))
            progressDao.insertMetric(BodyMetric(timestamp = now - dayMs * 21, weightKg = 76.8, bodyFatPercentage = 16.1, chestCm = 101.2, waistCm = 83.0, armCm = 36.6, notes = "Good deficit consistency"))
            progressDao.insertMetric(BodyMetric(timestamp = now - dayMs * 14, weightKg = 76.1, bodyFatPercentage = 15.6, chestCm = 101.5, waistCm = 82.2, armCm = 36.8, notes = "Strength maintained"))
            progressDao.insertMetric(BodyMetric(timestamp = now - dayMs * 7, weightKg = 75.5, bodyFatPercentage = 15.2, chestCm = 102.0, waistCm = 81.5, armCm = 37.0, notes = "Visible abs definition"))
            progressDao.insertMetric(BodyMetric(timestamp = now, weightKg = 75.0, bodyFatPercentage = 14.8, chestCm = 102.5, waistCm = 81.0, armCm = 37.2, notes = "Milestone reached: 14.8% body fat!"))
        }

        val prs = progressDao.getAllPrs().first()
        if (prs.isEmpty()) {
            progressDao.insertOrUpdatePr(PersonalRecord(exerciseName = "Barbell Bench Press", maxWeightKg = 100.0, maxReps = 3, estimated1RmKg = 107.5))
            progressDao.insertOrUpdatePr(PersonalRecord(exerciseName = "Barbell Back Squat", maxWeightKg = 140.0, maxReps = 4, estimated1RmKg = 155.0))
            progressDao.insertOrUpdatePr(PersonalRecord(exerciseName = "Conventional Deadlift", maxWeightKg = 175.0, maxReps = 2, estimated1RmKg = 183.0))
            progressDao.insertOrUpdatePr(PersonalRecord(exerciseName = "Standing Overhead Press", maxWeightKg = 65.0, maxReps = 5, estimated1RmKg = 73.0))
        }

        val profile = nutritionDao.getProfile().first()
        if (profile == null) {
            nutritionDao.insertOrUpdateProfile(
                NutritionProfile(
                    goal = "Muscle Gain",
                    currentWeightKg = 75.0,
                    targetWeightKg = 80.0,
                    heightCm = 178.0,
                    age = 26,
                    gender = "Male",
                    activityLevel = "High",
                    dailyCalorieTarget = 2750,
                    proteinGramsTarget = 170,
                    carbsGramsTarget = 320,
                    fatsGramsTarget = 75
                )
            )
        }

        val meals = nutritionDao.getAllMeals().first()
        if (meals.isEmpty()) {
            val now = System.currentTimeMillis()
            nutritionDao.insertMeal(MealLog(mealType = "Breakfast", foodName = "Oatmeal with Whey Protein, Banana & Almond Butter", calories = 580, proteinG = 42.0, carbsG = 68.0, fatsG = 14.0, dateTimestamp = now))
            nutritionDao.insertMeal(MealLog(mealType = "Lunch", foodName = "Grilled Chicken Breast, Jasmine Rice & Steamed Broccoli", calories = 650, proteinG = 52.0, carbsG = 75.0, fatsG = 11.0, dateTimestamp = now))
            nutritionDao.insertMeal(MealLog(mealType = "Snack / Pre-Workout", foodName = "Greek Yogurt Bowl with Berries & Honey", calories = 310, proteinG = 24.0, carbsG = 38.0, fatsG = 4.0, dateTimestamp = now))
        }

        val posts = socialDao.getAllPosts().first()
        if (posts.isEmpty()) {
            val now = System.currentTimeMillis()
            socialDao.insertPosts(
                listOf(
                    SocialPost(
                        authorName = "Marcus Cole",
                        authorHandle = "@mcole_lifts",
                        authorAvatarHex = 0xFFEF4444,
                        timestamp = now - 3600000L * 2,
                        content = "Finally joined the 100kg Bench Press club today! 🏆 3 solid reps with paused lockout. Big thanks to my training squad for the encouragement!",
                        workoutSummary = "Bench Press: 100kg x 3 reps (New PR)",
                        achievementBadge = "100kg Bench Club 🥇",
                        likesCount = 38,
                        isLikedByMe = true,
                        commentsCount = 7
                    ),
                    SocialPost(
                        authorName = "Sarah Jenkins",
                        authorHandle = "@sarah_fit",
                        authorAvatarHex = 0xFF10B981,
                        timestamp = now - 3600000L * 5,
                        content = "Completed 18 out of 20 workouts for the September Consistency Challenge! 🔥 Crushed leg day this morning. Who else is hitting the gym today?",
                        workoutSummary = "Leg Day: 12,400 kg Total Volume",
                        achievementBadge = "Consistency Beast ⚡",
                        likesCount = 24,
                        isLikedByMe = false,
                        commentsCount = 4
                    ),
                    SocialPost(
                        authorName = "David Chen",
                        authorHandle = "@dchen_iron",
                        authorAvatarHex = 0xFF3B82F6,
                        timestamp = now - 3600000L * 12,
                        content = "New 1RM Deadlift milestone: 180kg! The progressive overload routine over the last 8 weeks is definitely paying off. Stay consistent everyone! 💪",
                        workoutSummary = "Deadlift: 180kg x 1 rep",
                        achievementBadge = "Deadlift Milestone 🎖️",
                        likesCount = 52,
                        isLikedByMe = true,
                        commentsCount = 12
                    )
                )
            )
        }

        val challengesList = challengeDao.getAllChallenges().first()
        if (challengesList.isEmpty()) {
            challengeDao.insertChallenges(
                listOf(
                    Challenge(
                        title = "50,000kg Iron Volume Challenge",
                        description = "Accumulate 50 metric tons of total lifted volume this month across all compound exercises.",
                        monthYear = "Monthly Challenge",
                        targetType = "Volume (kg)",
                        currentProgress = 29450.0,
                        targetGoal = 50000.0,
                        unit = "kg",
                        daysLeft = 11,
                        participantsCount = 428,
                        isJoined = true,
                        userRank = 18,
                        rewardBadge = "Volume Titan 🏅"
                    ),
                    Challenge(
                        title = "20 Gym Sessions in 30 Days",
                        description = "Consistency is king. Complete 20 full gym workouts within this monthly challenge window.",
                        monthYear = "Monthly Challenge",
                        targetType = "Workouts",
                        currentProgress = 14.0,
                        targetGoal = 20.0,
                        unit = "sessions",
                        daysLeft = 11,
                        participantsCount = 890,
                        isJoined = true,
                        userRank = 42,
                        rewardBadge = "Iron Regular 🎖️"
                    ),
                    Challenge(
                        title = "100-Minute Core & Plank Master",
                        description = "Log a total of 100 minutes of abdominal planks and core endurance exercises.",
                        monthYear = "Monthly Challenge",
                        targetType = "Core Endurance",
                        currentProgress = 45.0,
                        targetGoal = 100.0,
                        unit = "minutes",
                        daysLeft = 14,
                        participantsCount = 312,
                        isJoined = false,
                        userRank = 0,
                        rewardBadge = "Shield of Steel 🛡️"
                    )
                )
            )
        }

        val settings = reminderSettingsDao.getSettings().first()
        if (settings == null) {
            reminderSettingsDao.saveSettings(
                ReminderSettings(
                    dailyMotivationEnabled = true,
                    motivationHour = 8,
                    motivationMinute = 0,
                    workoutReminderEnabled = true,
                    workoutReminderHour = 18,
                    workoutReminderMinute = 0,
                    waterReminderEnabled = true,
                    preferredMotivationQuote = "Discipline is doing what needs to be done, even if you don't feel like doing it."
                )
            )
        }
    }

    // Workout operations
    suspend fun logWorkout(log: WorkoutLog): Long = workoutDao.insertLog(log)
    suspend fun deleteWorkoutLog(id: Long) = workoutDao.deleteLogById(id)

    // Progress operations
    suspend fun addBodyMetric(metric: BodyMetric): Long = progressDao.insertMetric(metric)
    suspend fun deleteBodyMetric(id: Long) = progressDao.deleteMetric(id)
    suspend fun savePersonalRecord(name: String, weight: Double, reps: Int) {
        val estimated1Rm = if (reps <= 1) weight else weight * (1.0 + reps / 30.0)
        val pr = PersonalRecord(
            exerciseName = name,
            maxWeightKg = weight,
            maxReps = reps,
            estimated1RmKg = Math.round(estimated1Rm * 10.0) / 10.0
        )
        progressDao.insertOrUpdatePr(pr)
    }

    // Nutrition operations
    suspend fun updateNutritionProfile(
        goal: String,
        weightKg: Double,
        heightCm: Double,
        age: Int,
        gender: String,
        activityLevel: String
    ) {
        // Mifflin-St Jeor Formula:
        // Men: 10 * weight(kg) + 6.25 * height(cm) - 5 * age + 5
        // Women: 10 * weight(kg) + 6.25 * height(cm) - 5 * age - 161
        val baseBmr = (10.0 * weightKg) + (6.25 * heightCm) - (5.0 * age) + if (gender.lowercase() == "female") -161.0 else 5.0
        val activityMultiplier = when (activityLevel.lowercase()) {
            "sedentary" -> 1.2
            "moderate" -> 1.45
            "high" -> 1.65
            "athlete" -> 1.85
            else -> 1.45
        }
        val tdee = baseBmr * activityMultiplier
        val calorieTarget = when (goal) {
            "Fat Loss" -> (tdee - 450).toInt()
            "Muscle Gain" -> (tdee + 350).toInt()
            else -> tdee.toInt()
        }
        // Macro distribution:
        // Protein: 2.0g per kg of bodyweight
        val protein = (weightKg * 2.0).toInt()
        val fatCalories = calorieTarget * 0.25
        val fats = (fatCalories / 9.0).toInt()
        val remainingCalories = calorieTarget - (protein * 4) - (fats * 9)
        val carbs = (remainingCalories / 4.0).coerceAtLeast(50.0).toInt()

        val updated = NutritionProfile(
            goal = goal,
            currentWeightKg = weightKg,
            targetWeightKg = if (goal == "Fat Loss") weightKg - 4.0 else weightKg + 4.0,
            heightCm = heightCm,
            age = age,
            gender = gender,
            activityLevel = activityLevel,
            dailyCalorieTarget = calorieTarget,
            proteinGramsTarget = protein,
            carbsGramsTarget = carbs,
            fatsGramsTarget = fats
        )
        nutritionDao.insertOrUpdateProfile(updated)
    }

    suspend fun addMeal(meal: MealLog): Long = nutritionDao.insertMeal(meal)
    suspend fun deleteMeal(id: Long) = nutritionDao.deleteMeal(id)

    // Social operations
    suspend fun createSocialPost(content: String, workoutSummary: String?, achievementBadge: String?) {
        val newPost = SocialPost(
            authorName = "You (Alex R.)",
            authorHandle = "@alex_gains",
            authorAvatarHex = 0xFFFF6B00,
            timestamp = System.currentTimeMillis(),
            content = content,
            workoutSummary = workoutSummary,
            achievementBadge = achievementBadge,
            likesCount = 1,
            isLikedByMe = true,
            commentsCount = 0
        )
        socialDao.insertPost(newPost)
    }

    suspend fun togglePostLike(post: SocialPost) {
        val willBeLiked = !post.isLikedByMe
        val newLikesCount = if (willBeLiked) post.likesCount + 1 else (post.likesCount - 1).coerceAtLeast(0)
        socialDao.updateLikeStatus(post.id, willBeLiked, newLikesCount)
    }

    // Challenge operations
    suspend fun toggleChallengeJoin(challenge: Challenge) {
        val updated = challenge.copy(isJoined = !challenge.isJoined)
        challengeDao.updateChallenge(updated)
    }

    suspend fun logChallengeProgress(challengeId: Long, additionalProgress: Double) {
        challengeDao.addProgress(challengeId, joined = true, progressDelta = additionalProgress)
    }

    // Settings operations
    suspend fun updateReminderSettings(settings: ReminderSettings) {
        reminderSettingsDao.saveSettings(settings)
    }

    companion object {
        val MOTIVATION_QUOTES = listOf(
            "The body achieves what the mind believes.",
            "Discipline is doing what needs to be done, even if you don't feel like doing it.",
            "No matter how slow you go, you are still lapping everybody on the couch.",
            "Success starts with self-discipline. Show up for yourself today.",
            "The pain you feel today will be the strength you feel tomorrow.",
            "Action cures fear. One rep at a time.",
            "Consistency is the code that unlocks elite performance.",
            "You don't have to be extreme, just consistent."
        )
    }
}
