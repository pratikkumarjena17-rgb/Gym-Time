package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.GymDatabase
import com.example.data.model.BodyMetric
import com.example.data.model.CelebrationEvent
import com.example.data.model.CelebrationType
import com.example.data.model.Challenge
import com.example.data.model.FitnessMilestone
import com.example.data.model.MealLog
import com.example.data.model.NutritionProfile
import com.example.data.model.PersonalRecord
import com.example.data.model.ReminderSettings
import com.example.data.model.SocialPost
import com.example.data.model.WorkoutLog
import com.example.data.model.WorkoutTemplate
import com.example.data.repository.GymRepository
import com.example.notification.NotificationHelper
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ActiveExerciseState(
    val exerciseName: String,
    val sets: List<ActiveSetState>
)

data class ActiveSetState(
    val setNumber: Int,
    val weightKg: String,
    val reps: String,
    val isCompleted: Boolean = false,
    val isPr: Boolean = false
)

data class ActiveWorkoutSession(
    val title: String,
    val startTime: Long = System.currentTimeMillis(),
    val durationSeconds: Int = 0,
    val exercises: List<ActiveExerciseState> = emptyList(),
    val notes: String = "",
    val restTimerRemainingSeconds: Int = 0,
    val isRestTimerActive: Boolean = false
)

class GymViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: GymRepository

    val workoutTemplates: StateFlow<List<WorkoutTemplate>>
    val workoutLogs: StateFlow<List<WorkoutLog>>
    val bodyMetrics: StateFlow<List<BodyMetric>>
    val personalRecords: StateFlow<List<PersonalRecord>>
    val nutritionProfile: StateFlow<NutritionProfile?>
    val allMeals: StateFlow<List<MealLog>>
    val socialPosts: StateFlow<List<SocialPost>>
    val challenges: StateFlow<List<Challenge>>
    val reminderSettings: StateFlow<ReminderSettings?>

    private val _celebrationEvent = MutableStateFlow<CelebrationEvent?>(null)
    val celebrationEvent: StateFlow<CelebrationEvent?> = _celebrationEvent.asStateFlow()

    val fitnessMilestones: StateFlow<List<FitnessMilestone>>

    private val _activeWorkout = MutableStateFlow<ActiveWorkoutSession?>(null)
    val activeWorkout: StateFlow<ActiveWorkoutSession?> = _activeWorkout.asStateFlow()

    private val _selectedTab = MutableStateFlow(0) // 0: Workouts, 1: Progress, 2: Nutrition, 3: Community, 4: Reminders
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    private var workoutTimerJob: Job? = null
    private var restTimerJob: Job? = null

    init {
        val database = GymDatabase.getDatabase(application)
        repository = GymRepository(database)

        workoutTemplates = repository.workoutTemplates.stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
        )
        workoutLogs = repository.workoutLogs.stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
        )
        bodyMetrics = repository.bodyMetrics.stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
        )
        personalRecords = repository.personalRecords.stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
        )
        nutritionProfile = repository.nutritionProfile.stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000), null
        )
        allMeals = repository.allMeals.stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
        )
        socialPosts = repository.socialPosts.stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
        )
        challenges = repository.challenges.stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
        )
        reminderSettings = repository.reminderSettings.stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000), null
        )

        fitnessMilestones = combine(
            personalRecords,
            workoutLogs,
            bodyMetrics
        ) { prs, logs, metrics ->
            val totalVolume = logs.sumOf { it.totalVolumeKg }
            val maxLift = prs.maxOfOrNull { it.maxWeightKg } ?: 0.0
            val squatPr = prs.find { it.exerciseName.contains("Squat", ignoreCase = true) }?.maxWeightKg ?: 0.0
            val deadliftPr = prs.find { it.exerciseName.contains("Deadlift", ignoreCase = true) }?.maxWeightKg ?: 0.0
            val benchPr = prs.find { it.exerciseName.contains("Bench", ignoreCase = true) }?.maxWeightKg ?: 0.0
            val totalCalories = logs.sumOf { it.caloriesBurned }
            val minBodyFat = metrics.mapNotNull { it.bodyFatPercentage }.minOrNull() ?: 20.0

            listOf(
                FitnessMilestone(
                    id = "century_club",
                    title = "Century Club (100kg+ Lift)",
                    description = "Lift 100kg or more on any compound barbell exercise.",
                    category = "Strength",
                    badgeIcon = "🏆",
                    targetValue = 100.0,
                    currentValue = maxLift,
                    unit = "kg",
                    isAchieved = maxLift >= 100.0,
                    xpReward = 500,
                    tier = "Diamond"
                ),
                FitnessMilestone(
                    id = "volume_titan",
                    title = "25,000kg Volume Titan",
                    description = "Accumulate over 25,000 kg of total logged training volume.",
                    category = "Volume",
                    badgeIcon = "⚡",
                    targetValue = 25000.0,
                    currentValue = totalVolume,
                    unit = "kg",
                    isAchieved = totalVolume >= 25000.0,
                    xpReward = 450,
                    tier = "Gold"
                ),
                FitnessMilestone(
                    id = "consistency_warrior",
                    title = "Consistency Habit (3+ Workouts)",
                    description = "Complete and log 3 full training sessions.",
                    category = "Consistency",
                    badgeIcon = "🔥",
                    targetValue = 3.0,
                    currentValue = logs.size.toDouble(),
                    unit = "sessions",
                    isAchieved = logs.size >= 3,
                    xpReward = 300,
                    tier = "Gold"
                ),
                FitnessMilestone(
                    id = "heavy_squat",
                    title = "Heavy Squat Milestone (140kg)",
                    description = "Crush a 140kg barbell back squat for working reps.",
                    category = "Strength",
                    badgeIcon = "🎖️",
                    targetValue = 140.0,
                    currentValue = squatPr,
                    unit = "kg",
                    isAchieved = squatPr >= 140.0,
                    xpReward = 400,
                    tier = "Diamond"
                ),
                FitnessMilestone(
                    id = "deadlift_titan",
                    title = "Deadlift Titan (175kg)",
                    description = "Lock out a 175kg conventional deadlift from the floor.",
                    category = "Strength",
                    badgeIcon = "👑",
                    targetValue = 175.0,
                    currentValue = deadliftPr,
                    unit = "kg",
                    isAchieved = deadliftPr >= 175.0,
                    xpReward = 450,
                    tier = "Diamond"
                ),
                FitnessMilestone(
                    id = "sub_15_bf",
                    title = "Sub-15% Body Fat Shred",
                    description = "Reach a lean body fat percentage of 15% or lower.",
                    category = "Body Recomp",
                    badgeIcon = "🎯",
                    targetValue = 15.0,
                    currentValue = minBodyFat,
                    unit = "% BF",
                    isAchieved = minBodyFat <= 15.0,
                    xpReward = 400,
                    tier = "Gold"
                ),
                FitnessMilestone(
                    id = "calorie_burner",
                    title = "1,000 Calorie Burner",
                    description = "Burn more than 1,000 total active calories in training.",
                    category = "Volume",
                    badgeIcon = "🔥",
                    targetValue = 1000.0,
                    currentValue = totalCalories.toDouble(),
                    unit = "kcal",
                    isAchieved = totalCalories >= 1000,
                    xpReward = 250,
                    tier = "Silver"
                )
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        NotificationHelper.createNotificationChannel(application)
    }

    fun selectTab(index: Int) {
        _selectedTab.value = index
    }

    // --- WORKOUT TRACKING ACTIONS ---

    fun startWorkoutFromTemplate(template: WorkoutTemplate) {
        val exercises = template.exerciseNames.split(",").map { name ->
            ActiveExerciseState(
                exerciseName = name.trim(),
                sets = listOf(
                    ActiveSetState(setNumber = 1, weightKg = "60", reps = "10", isCompleted = false),
                    ActiveSetState(setNumber = 2, weightKg = "65", reps = "8", isCompleted = false),
                    ActiveSetState(setNumber = 3, weightKg = "70", reps = "6", isCompleted = false)
                )
            )
        }
        startSession(template.title, exercises)
    }

    fun startCustomWorkout(title: String, exerciseNames: List<String>) {
        val exercises = exerciseNames.map { name ->
            ActiveExerciseState(
                exerciseName = name.trim(),
                sets = listOf(
                    ActiveSetState(setNumber = 1, weightKg = "50", reps = "10", isCompleted = false),
                    ActiveSetState(setNumber = 2, weightKg = "50", reps = "10", isCompleted = false),
                    ActiveSetState(setNumber = 3, weightKg = "50", reps = "10", isCompleted = false)
                )
            )
        }
        startSession(title, exercises)
    }

    private fun startSession(title: String, exercises: List<ActiveExerciseState>) {
        _activeWorkout.value = ActiveWorkoutSession(
            title = title,
            startTime = System.currentTimeMillis(),
            durationSeconds = 0,
            exercises = exercises
        )
        startWorkoutTicker()
    }

    private fun startWorkoutTicker() {
        workoutTimerJob?.cancel()
        workoutTimerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                _activeWorkout.value?.let { current ->
                    _activeWorkout.value = current.copy(durationSeconds = current.durationSeconds + 1)
                }
            }
        }
    }

    fun updateSet(exerciseIndex: Int, setIndex: Int, weight: String, reps: String) {
        val current = _activeWorkout.value ?: return
        val updatedExercises = current.exercises.toMutableList()
        val currentEx = updatedExercises[exerciseIndex]
        val updatedSets = currentEx.sets.toMutableList()
        val targetSet = updatedSets[setIndex]

        updatedSets[setIndex] = targetSet.copy(weightKg = weight, reps = reps)
        updatedExercises[exerciseIndex] = currentEx.copy(sets = updatedSets)
        _activeWorkout.value = current.copy(exercises = updatedExercises)
    }

    fun toggleSetCompleted(exerciseIndex: Int, setIndex: Int) {
        val current = _activeWorkout.value ?: return
        val updatedExercises = current.exercises.toMutableList()
        val currentEx = updatedExercises[exerciseIndex]
        val updatedSets = currentEx.sets.toMutableList()
        val targetSet = updatedSets[setIndex]
        val newCompleted = !targetSet.isCompleted

        updatedSets[setIndex] = targetSet.copy(isCompleted = newCompleted)
        updatedExercises[exerciseIndex] = currentEx.copy(sets = updatedSets)
        _activeWorkout.value = current.copy(exercises = updatedExercises)

        if (newCompleted) {
            // Trigger 90s rest timer
            startRestTimer(90)
        }
    }

    fun addSetToExercise(exerciseIndex: Int) {
        val current = _activeWorkout.value ?: return
        val updatedExercises = current.exercises.toMutableList()
        val currentEx = updatedExercises[exerciseIndex]
        val updatedSets = currentEx.sets.toMutableList()

        val lastSet = updatedSets.lastOrNull()
        val nextSetNum = updatedSets.size + 1
        val defaultWeight = lastSet?.weightKg ?: "50"
        val defaultReps = lastSet?.reps ?: "10"

        updatedSets.add(
            ActiveSetState(
                setNumber = nextSetNum,
                weightKg = defaultWeight,
                reps = defaultReps,
                isCompleted = false
            )
        )
        updatedExercises[exerciseIndex] = currentEx.copy(sets = updatedSets)
        _activeWorkout.value = current.copy(exercises = updatedExercises)
    }

    fun addExerciseToWorkout(exerciseName: String) {
        val current = _activeWorkout.value ?: return
        val updatedExercises = current.exercises.toMutableList()
        updatedExercises.add(
            ActiveExerciseState(
                exerciseName = exerciseName.trim(),
                sets = listOf(
                    ActiveSetState(setNumber = 1, weightKg = "40", reps = "10", isCompleted = false),
                    ActiveSetState(setNumber = 2, weightKg = "40", reps = "10", isCompleted = false),
                    ActiveSetState(setNumber = 3, weightKg = "40", reps = "10", isCompleted = false)
                )
            )
        )
        _activeWorkout.value = current.copy(exercises = updatedExercises)
    }

    fun startRestTimer(seconds: Int) {
        restTimerJob?.cancel()
        _activeWorkout.value?.let { current ->
            _activeWorkout.value = current.copy(
                restTimerRemainingSeconds = seconds,
                isRestTimerActive = true
            )
        }
        restTimerJob = viewModelScope.launch {
            var remaining = seconds
            while (remaining > 0) {
                delay(1000)
                remaining--
                _activeWorkout.value?.let { current ->
                    _activeWorkout.value = current.copy(
                        restTimerRemainingSeconds = remaining,
                        isRestTimerActive = remaining > 0
                    )
                }
            }
        }
    }

    fun cancelRestTimer() {
        restTimerJob?.cancel()
        _activeWorkout.value?.let { current ->
            _activeWorkout.value = current.copy(isRestTimerActive = false, restTimerRemainingSeconds = 0)
        }
    }

    fun finishWorkout(notes: String = "") {
        val current = _activeWorkout.value ?: return
        workoutTimerJob?.cancel()
        restTimerJob?.cancel()

        var totalVolume = 0.0
        var completedSetsCount = 0
        var exercisesCompletedCount = 0

        current.exercises.forEach { ex ->
            var exHadCompleted = false
            ex.sets.forEach { s ->
                if (s.isCompleted) {
                    val w = s.weightKg.toDoubleOrNull() ?: 0.0
                    val r = s.reps.toIntOrNull() ?: 0
                    totalVolume += (w * r)
                    completedSetsCount++
                    exHadCompleted = true

                    // Check if this is a PR
                    viewModelScope.launch {
                        repository.savePersonalRecord(ex.exerciseName, w, r)
                    }
                }
            }
            if (exHadCompleted) exercisesCompletedCount++
        }

        val durationMin = (current.durationSeconds / 60).coerceAtLeast(1)
        val calories = (durationMin * 8.5).toInt()

        val log = WorkoutLog(
            title = current.title,
            dateTimestamp = System.currentTimeMillis(),
            durationMinutes = durationMin,
            totalVolumeKg = Math.round(totalVolume * 10.0) / 10.0,
            exercisesCompleted = exercisesCompletedCount,
            caloriesBurned = calories,
            notes = notes
        )

        viewModelScope.launch {
            repository.logWorkout(log)

            // Update monthly challenges progress if applicable
            challenges.value.forEach { ch ->
                if (ch.isJoined) {
                    val delta = if (ch.targetType.contains("Volume", ignoreCase = true)) {
                        totalVolume
                    } else if (ch.targetType.contains("Workouts", ignoreCase = true)) {
                        1.0
                    } else 0.0

                    if (delta > 0) {
                        val newProgress = ch.currentProgress + delta
                        repository.logChallengeProgress(ch.id, delta)
                        if (ch.currentProgress < ch.targetGoal && newProgress >= ch.targetGoal) {
                            triggerCelebration(
                                CelebrationEvent(
                                    type = CelebrationType.MONTHLY_CHALLENGE,
                                    title = "Challenge Completed! 🏆",
                                    subtitle = ch.title,
                                    badgeIcon = ch.rewardBadge,
                                    statHighlight = "${newProgress.toInt()} / ${ch.targetGoal.toInt()} ${ch.unit} Conquered",
                                    description = "Massive achievement! Your workout completed the ${ch.title} monthly challenge and unlocked the ${ch.rewardBadge} badge!",
                                    xpEarned = 500,
                                    rankOrTier = "Rank #${ch.userRank}"
                                )
                            )
                        }
                    }
                }
            }
        }

        _activeWorkout.value = null
    }

    fun cancelActiveWorkout() {
        workoutTimerJob?.cancel()
        restTimerJob?.cancel()
        _activeWorkout.value = null
    }

    fun deleteWorkoutLog(id: Long) {
        viewModelScope.launch {
            repository.deleteWorkoutLog(id)
        }
    }

    // --- PROGRESS ACTIONS ---

    fun logBodyMetric(weightKg: Double, bodyFat: Double?, chest: Double?, waist: Double?, arms: Double?, notes: String) {
        viewModelScope.launch {
            val metric = BodyMetric(
                timestamp = System.currentTimeMillis(),
                weightKg = weightKg,
                bodyFatPercentage = bodyFat,
                chestCm = chest,
                waistCm = waist,
                armCm = arms,
                notes = notes
            )
            repository.addBodyMetric(metric)
        }
    }

    fun deleteBodyMetric(id: Long) {
        viewModelScope.launch {
            repository.deleteBodyMetric(id)
        }
    }

    fun logPersonalRecord(exerciseName: String, weightKg: Double, reps: Int) {
        viewModelScope.launch {
            val est1Rm = if (reps <= 1) weightKg else weightKg * (1.0 + reps / 30.0)
            val estFormatted = Math.round(est1Rm * 10.0) / 10.0
            repository.savePersonalRecord(exerciseName, weightKg, reps)
            triggerCelebration(
                CelebrationEvent(
                    type = CelebrationType.PERSONAL_RECORD,
                    title = "New Personal Record! 🥇",
                    subtitle = exerciseName,
                    badgeIcon = "🥇",
                    statHighlight = "$weightKg kg × $reps reps (Est. 1RM: $estFormatted kg)",
                    description = "Monster lift! You've set a brand new personal milestone on $exerciseName. Keep crushing your limits!",
                    xpEarned = 350
                )
            )
        }
    }

    // --- NUTRITION ACTIONS ---

    fun saveNutritionProfile(
        goal: String,
        currentWeight: Double,
        height: Double,
        age: Int,
        gender: String,
        activityLevel: String
    ) {
        viewModelScope.launch {
            repository.updateNutritionProfile(
                goal = goal,
                weightKg = currentWeight,
                heightCm = height,
                age = age,
                gender = gender,
                activityLevel = activityLevel
            )
        }
    }

    fun addMeal(
        mealType: String,
        foodName: String,
        calories: Int,
        proteinG: Double,
        carbsG: Double,
        fatsG: Double
    ) {
        viewModelScope.launch {
            repository.addMeal(
                MealLog(
                    mealType = mealType,
                    foodName = foodName,
                    calories = calories,
                    proteinG = proteinG,
                    carbsG = carbsG,
                    fatsG = fatsG,
                    dateTimestamp = System.currentTimeMillis()
                )
            )
        }
    }

    fun deleteMeal(id: Long) {
        viewModelScope.launch {
            repository.deleteMeal(id)
        }
    }

    // --- SOCIAL & COMMUNITY ACTIONS ---

    fun publishSocialPost(content: String, workoutSummary: String?, achievementBadge: String?) {
        viewModelScope.launch {
            repository.createSocialPost(content, workoutSummary, achievementBadge)
        }
    }

    fun togglePostLike(post: SocialPost) {
        viewModelScope.launch {
            repository.togglePostLike(post)
        }
    }

    fun toggleChallengeJoin(challenge: Challenge) {
        viewModelScope.launch {
            repository.toggleChallengeJoin(challenge)
        }
    }

    fun addChallengeProgress(challenge: Challenge, amount: Double) {
        viewModelScope.launch {
            val newProgress = challenge.currentProgress + amount
            repository.logChallengeProgress(challenge.id, amount)
            if (challenge.currentProgress < challenge.targetGoal && newProgress >= challenge.targetGoal) {
                triggerCelebration(
                    CelebrationEvent(
                        type = CelebrationType.MONTHLY_CHALLENGE,
                        title = "Challenge Completed! 🏆",
                        subtitle = challenge.title,
                        badgeIcon = challenge.rewardBadge,
                        statHighlight = "${newProgress.toInt()} / ${challenge.targetGoal.toInt()} ${challenge.unit} Goal Reached",
                        description = "Incredible work! You crushed this monthly challenge with gym friends and claimed the ${challenge.rewardBadge} badge.",
                        xpEarned = 500,
                        rankOrTier = "Rank #${challenge.userRank}"
                    )
                )
            }
        }
    }

    // --- CELEBRATION & MILESTONE ACTIONS ---

    fun triggerCelebration(event: CelebrationEvent) {
        _celebrationEvent.value = event
    }

    fun dismissCelebration() {
        _celebrationEvent.value = null
    }

    fun shareCelebrationToCommunity(event: CelebrationEvent) {
        publishSocialPost(
            content = "Smashed an awesome achievement today! ${event.description}",
            workoutSummary = event.statHighlight,
            achievementBadge = "${event.badgeIcon} ${event.title}"
        )
        dismissCelebration()
    }

    fun celebrateMilestone(milestone: FitnessMilestone) {
        triggerCelebration(
            CelebrationEvent(
                type = CelebrationType.FITNESS_MILESTONE,
                title = "${milestone.title} Unlocked! ${milestone.badgeIcon}",
                subtitle = milestone.description,
                badgeIcon = milestone.badgeIcon,
                statHighlight = "${milestone.currentValue.toInt()} / ${milestone.targetValue.toInt()} ${milestone.unit} Achieved",
                description = "Huge respect for the grind! You have conquered this fitness milestone in your training journey.",
                xpEarned = milestone.xpReward,
                rankOrTier = "${milestone.tier} Tier"
            )
        )
    }

    fun celebrateChallenge(challenge: Challenge) {
        triggerCelebration(
            CelebrationEvent(
                type = CelebrationType.MONTHLY_CHALLENGE,
                title = "Challenge Completed! 🏆",
                subtitle = challenge.title,
                badgeIcon = challenge.rewardBadge,
                statHighlight = "${challenge.currentProgress.toInt()} / ${challenge.targetGoal.toInt()} ${challenge.unit} Smashed",
                description = "Sensational effort! You completed all targets for ${challenge.title} and earned the ${challenge.rewardBadge} badge!",
                xpEarned = 500,
                rankOrTier = "Rank #${challenge.userRank}"
            )
        )
    }

    // --- MOTIVATION & NOTIFICATION ACTIONS ---

    fun updateReminderSettings(settings: ReminderSettings) {
        viewModelScope.launch {
            repository.updateReminderSettings(settings)

            val app = getApplication<Application>()
            if (settings.dailyMotivationEnabled) {
                NotificationHelper.scheduleDailyAlarm(
                    context = app,
                    action = NotificationHelper.ACTION_MOTIVATION,
                    requestCode = 101,
                    hour = settings.motivationHour,
                    minute = settings.motivationMinute
                )
            }
            if (settings.workoutReminderEnabled) {
                NotificationHelper.scheduleDailyAlarm(
                    context = app,
                    action = NotificationHelper.ACTION_WORKOUT,
                    requestCode = 102,
                    hour = settings.workoutReminderHour,
                    minute = settings.workoutReminderMinute
                )
            }
        }
    }

    fun triggerInstantMotivationNotification() {
        val app = getApplication<Application>()
        val quote = GymRepository.MOTIVATION_QUOTES.random()
        NotificationHelper.showNotification(
            context = app,
            notificationId = (System.currentTimeMillis() % 10000).toInt(),
            title = "Daily Gym Motivation ⚡",
            message = quote
        )
    }

    fun triggerInstantWorkoutReminder() {
        val app = getApplication<Application>()
        NotificationHelper.showNotification(
            context = app,
            notificationId = (System.currentTimeMillis() % 10000).toInt() + 1,
            title = "Gym Session Reminder 🏋️",
            message = "Your scheduled workout is waiting. Time to grind and hit new personal records!"
        )
    }
}
