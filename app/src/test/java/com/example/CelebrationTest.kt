package com.example

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.CelebrationEvent
import com.example.data.model.CelebrationType
import com.example.data.model.Challenge
import com.example.data.model.FitnessMilestone
import com.example.ui.GymViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class CelebrationTest {

    @Test
    fun `challenge completion status calculated correctly`() {
        val incompleteChallenge = Challenge(
            id = 1,
            title = "100k Volume Club",
            description = "Lift 100,000kg in a month",
            monthYear = "October 2026",
            targetType = "Volume (kg)",
            currentProgress = 45000.0,
            targetGoal = 100000.0,
            unit = "kg",
            daysLeft = 14,
            participantsCount = 18,
            rewardBadge = "🏆"
        )
        assertFalse(incompleteChallenge.isCompleted)

        val completedChallenge = incompleteChallenge.copy(currentProgress = 100000.0)
        assertTrue(completedChallenge.isCompleted)

        val exceededChallenge = incompleteChallenge.copy(currentProgress = 120000.0)
        assertTrue(exceededChallenge.isCompleted)
    }

    @Test
    fun `celebration event can be triggered and dismissed in GymViewModel`() {
        val context = ApplicationProvider.getApplicationContext<Application>()
        val viewModel = GymViewModel(context)

        // Initially no celebration active
        assertNull(viewModel.celebrationEvent.value)

        val event = CelebrationEvent(
            type = CelebrationType.MONTHLY_CHALLENGE,
            title = "Challenge Completed! 🏆",
            subtitle = "100k Volume Club",
            badgeIcon = "🏆",
            statHighlight = "100,000 / 100,000 kg",
            description = "You've crushed this month's challenge!",
            xpEarned = 500
        )

        viewModel.triggerCelebration(event)
        assertEquals(event, viewModel.celebrationEvent.value)

        viewModel.dismissCelebration()
        assertNull(viewModel.celebrationEvent.value)
    }

    @Test
    fun `milestone celebration triggers expected event`() {
        val context = ApplicationProvider.getApplicationContext<Application>()
        val viewModel = GymViewModel(context)

        val milestone = FitnessMilestone(
            id = "century_club",
            title = "Century Club (100kg+ Lift)",
            description = "Lift 100kg or more on any compound barbell exercise.",
            category = "Strength",
            badgeIcon = "🏆",
            targetValue = 100.0,
            currentValue = 140.0,
            unit = "kg",
            isAchieved = true,
            xpReward = 500,
            tier = "Diamond"
        )

        viewModel.celebrateMilestone(milestone)
        val active = viewModel.celebrationEvent.value
        assertNotNull(active)
        assertEquals(CelebrationType.FITNESS_MILESTONE, active?.type)
        assertEquals("🏆", active?.badgeIcon)
        assertEquals(500, active?.xpEarned)
    }
}
