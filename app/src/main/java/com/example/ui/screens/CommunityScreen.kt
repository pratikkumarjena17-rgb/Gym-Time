package com.example.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Challenge
import com.example.data.model.SocialPost
import com.example.ui.GymViewModel
import com.example.ui.components.SectionHeader
import com.example.ui.theme.GymCyanSecondary
import com.example.ui.theme.GymGreenTertiary
import com.example.ui.theme.GymOrangePrimary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CommunityScreen(
    viewModel: GymViewModel,
    modifier: Modifier = Modifier
) {
    val posts by viewModel.socialPosts.collectAsState()
    val challenges by viewModel.challenges.collectAsState()

    var communityTab by remember { mutableStateOf(0) } // 0: Feed, 1: Challenges

    var showCreatePostDialog by remember { mutableStateOf(false) }
    var postContent by remember { mutableStateOf("") }
    var postWorkoutSummary by remember { mutableStateOf("") }
    var selectedBadge by remember { mutableStateOf("New PR 🥇") }

    var selectedChallengeForProgress by remember { mutableStateOf<Challenge?>(null) }
    var progressToAddInput by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Community Sub-tabs
        TabRow(
            selectedTabIndex = communityTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = GymOrangePrimary,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[communityTab]),
                    color = GymOrangePrimary
                )
            }
        ) {
            Tab(
                selected = communityTab == 0,
                onClick = { communityTab = 0 },
                text = {
                    Text(
                        "Feed & Achievements",
                        fontWeight = if (communityTab == 0) FontWeight.Bold else FontWeight.Normal
                    )
                },
                modifier = Modifier.testTag("tab_community_feed")
            )
            Tab(
                selected = communityTab == 1,
                onClick = { communityTab = 1 },
                text = {
                    Text(
                        "Monthly Challenges",
                        fontWeight = if (communityTab == 1) FontWeight.Bold else FontWeight.Normal
                    )
                },
                modifier = Modifier.testTag("tab_monthly_challenges")
            )
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            if (communityTab == 0) {
                // Share achievement prompt
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showCreatePostDialog = true }
                            .testTag("share_achievement_card")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(GymOrangePrimary),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("YOU", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 11.sp)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Share your PR, streak, or workout with the gym squad...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(
                                Icons.Default.Share,
                                contentDescription = null,
                                tint = GymOrangePrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                item {
                    SectionHeader(
                        title = "Gym Squad Feed",
                        subtitle = "Friend achievements and workout milestones"
                    )
                }

                items(posts) { post ->
                    val avatarColor = Color(post.authorAvatarHex)
                    val likeColor by animateColorAsState(
                        targetValue = if (post.isLikedByMe) Color(0xFFEF4444) else MaterialTheme.colorScheme.onSurfaceVariant,
                        label = "likeColor"
                    )

                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            // Author Header
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(avatarColor),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = post.authorName.take(1).uppercase(),
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            fontSize = 16.sp
                                        )
                                    }

                                    Column {
                                        Text(
                                            text = post.authorName,
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = post.authorHandle,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                if (post.achievementBadge != null) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = GymOrangePrimary.copy(alpha = 0.15f)
                                    ) {
                                        Text(
                                            text = post.achievementBadge,
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = GymOrangePrimary,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Post content
                            Text(
                                text = post.content,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                lineHeight = 20.sp
                            )

                            // Workout summary embed if present
                            if (post.workoutSummary != null) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.surface,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = GymCyanSecondary, modifier = Modifier.size(20.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = post.workoutSummary,
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Action buttons (Like & Comment)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { viewModel.togglePostLike(post) }
                                        .padding(horizontal = 8.dp, vertical = 6.dp)
                                ) {
                                    Icon(
                                        imageVector = if (post.isLikedByMe) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                        contentDescription = "Like",
                                        tint = likeColor,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "${post.likesCount} Cheers",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                        color = likeColor
                                    )
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                                ) {
                                    Icon(
                                        Icons.Default.ChatBubbleOutline,
                                        contentDescription = "Comments",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "${post.commentsCount} comments",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                // Monthly Challenges Tab
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    SectionHeader(
                        title = "Compete in Monthly Challenges",
                        subtitle = "Climb the leaderboard with gym friends"
                    )
                }

                items(challenges) { challenge ->
                    val progressRatio = (challenge.currentProgress / challenge.targetGoal).toFloat().coerceIn(0f, 1f)

                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = challenge.rewardBadge,
                                            fontSize = 18.sp
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = challenge.title,
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = challenge.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Progress Bar
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "${challenge.currentProgress.toInt()} / ${challenge.targetGoal.toInt()} ${challenge.unit}",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = GymOrangePrimary
                                )
                                Text(
                                    text = "${(progressRatio * 100).toInt()}%",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            LinearProgressIndicator(
                                progress = { progressRatio },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(10.dp)
                                    .clip(RoundedCornerShape(5.dp)),
                                color = GymOrangePrimary,
                                trackColor = GymOrangePrimary.copy(alpha = 0.2f),
                                strokeCap = StrokeCap.Round
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Badges & Rank
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = MaterialTheme.colorScheme.surface
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Default.Group, contentDescription = null, modifier = Modifier.size(14.dp), tint = GymCyanSecondary)
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("${challenge.participantsCount} friends", style = MaterialTheme.typography.labelSmall)
                                        }
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = MaterialTheme.colorScheme.surface
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(14.dp), tint = GymGreenTertiary)
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("${challenge.daysLeft}d left", style = MaterialTheme.typography.labelSmall)
                                        }
                                    }

                                    if (challenge.isJoined && challenge.userRank > 0) {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = GymOrangePrimary.copy(alpha = 0.15f)
                                        ) {
                                            Text(
                                                text = "Rank #${challenge.userRank}",
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                color = GymOrangePrimary,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    if (challenge.isJoined) {
                                        OutlinedButton(
                                            onClick = {
                                                selectedChallengeForProgress = challenge
                                                progressToAddInput = ""
                                            },
                                            shape = RoundedCornerShape(10.dp)
                                        ) {
                                            Text("+ Log")
                                        }
                                    }

                                    Button(
                                        onClick = { viewModel.toggleChallengeJoin(challenge) },
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (challenge.isJoined) GymGreenTertiary else GymOrangePrimary
                                        )
                                    ) {
                                        if (challenge.isJoined) {
                                            Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Joined")
                                        } else {
                                            Text("Join")
                                        }
                                    }
                                }
                            }

                            // Challenge Completed Banner & Celebration Action
                            if (challenge.isCompleted) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0xFFFFD700).copy(alpha = 0.15f),
                                    border = BorderStroke(1.dp, Color(0xFFFFD700).copy(alpha = 0.5f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            Text(text = challenge.rewardBadge, fontSize = 24.sp)
                                            Column {
                                                Text(
                                                    text = "CHALLENGE COMPLETED! 🏆",
                                                    style = MaterialTheme.typography.labelMedium.copy(
                                                        fontWeight = FontWeight.Black,
                                                        letterSpacing = 0.5.sp
                                                    ),
                                                    color = Color(0xFFFFD700)
                                                )
                                                Text(
                                                    text = "+500 XP Earned • Badge Claimed",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }

                                        Button(
                                            onClick = { viewModel.celebrateChallenge(challenge) },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = Color(0xFFFFD700),
                                                contentColor = Color.Black
                                            ),
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                        ) {
                                            Text("Celebrate 🎉", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }

    // Create Social Post Dialog
    if (showCreatePostDialog) {
        AlertDialog(
            onDismissRequest = { showCreatePostDialog = false },
            title = { Text("Share Gym Achievement 🏆") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Choose achievement badge:", style = MaterialTheme.typography.labelMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("New PR 🥇", "Volume Titan ⚡", "Streak Master 🔥", "Goal Smashed 🎯").forEach { b ->
                            FilterChip(
                                selected = selectedBadge == b,
                                onClick = { selectedBadge = b },
                                label = { Text(b, fontSize = 11.sp) }
                            )
                        }
                    }

                    OutlinedTextField(
                        value = postContent,
                        onValueChange = { postContent = it },
                        label = { Text("What did you achieve today? *") },
                        placeholder = { Text("e.g. Finally hit a 140kg squat for reps!") },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = postWorkoutSummary,
                        onValueChange = { postWorkoutSummary = it },
                        label = { Text("Workout Highlight (optional)") },
                        placeholder = { Text("e.g. Back Squat: 140kg x 4 reps") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (postContent.isNotBlank()) {
                            viewModel.publishSocialPost(
                                content = postContent,
                                workoutSummary = if (postWorkoutSummary.isNotBlank()) postWorkoutSummary else null,
                                achievementBadge = selectedBadge
                            )
                            postContent = ""
                            postWorkoutSummary = ""
                            showCreatePostDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GymOrangePrimary)
                ) {
                    Text("Post to Community")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreatePostDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Log Progress Towards Challenge Dialog
    if (selectedChallengeForProgress != null) {
        val ch = selectedChallengeForProgress!!
        AlertDialog(
            onDismissRequest = { selectedChallengeForProgress = null },
            title = { Text("Log Progress: ${ch.title}") },
            text = {
                Column {
                    Text("Enter additional ${ch.unit} to add towards your monthly challenge goal:")
                    Spacer(modifier = Modifier.height(10.dp))

                    val remaining = (ch.targetGoal - ch.currentProgress).coerceAtLeast(0.0)
                    if (remaining > 0) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = GymOrangePrimary.copy(alpha = 0.12f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    progressToAddInput = if (remaining % 1.0 == 0.0) remaining.toInt().toString() else remaining.toString()
                                }
                        ) {
                            Text(
                                text = "💡 Quick Tap: Fill remaining ${remaining.toInt()} ${ch.unit} to complete!",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = GymOrangePrimary,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    OutlinedTextField(
                        value = progressToAddInput,
                        onValueChange = { progressToAddInput = it },
                        label = { Text("Add ${ch.unit}") },
                        placeholder = { Text("e.g. 1") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amt = progressToAddInput.toDoubleOrNull()
                        if (amt != null && amt > 0) {
                            viewModel.addChallengeProgress(ch, amt)
                            selectedChallengeForProgress = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GymOrangePrimary)
                ) {
                    Text("Add Progress")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedChallengeForProgress = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}
