package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.ShowChart
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.GymViewModel
import com.example.ui.components.CelebrationOverlay
import com.example.ui.screens.CommunityScreen
import com.example.ui.screens.NutritionScreen
import com.example.ui.screens.ProgressScreen
import com.example.ui.screens.RemindersScreen
import com.example.ui.screens.WorkoutsScreen
import com.example.ui.theme.GymOrangePrimary
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                GymAppRoot()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GymAppRoot(viewModel: GymViewModel = viewModel()) {
    val selectedTab by viewModel.selectedTab.collectAsState()
    val activeSession by viewModel.activeWorkout.collectAsState()
    val celebrationEvent by viewModel.celebrationEvent.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            topBar = {
            if (activeSession == null) {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 2.dp,
                    modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(GymOrangePrimary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.FlashOn,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Text(
                                    text = "GYM TRACKER",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 1.sp
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = GymOrangePrimary.copy(alpha = 0.12f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.FitnessCenter,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp),
                                        tint = GymOrangePrimary
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "COMMUNITY ACTIVE",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = GymOrangePrimary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        bottomBar = {
            // Only show bottom navigation when there's no full-screen active workout
            if (activeSession == null) {
                NavigationBar(
                    modifier = Modifier
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .testTag("bottom_navigation_bar"),
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp
                ) {
                    NavigationBarItem(
                        selected = selectedTab == 0,
                        onClick = { viewModel.selectTab(0) },
                        icon = {
                            Icon(
                                if (selectedTab == 0) Icons.Filled.FitnessCenter else Icons.Outlined.FitnessCenter,
                                contentDescription = "Workouts"
                            )
                        },
                        label = { Text("Workouts", fontSize = 11.sp, fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = GymOrangePrimary,
                            selectedTextColor = GymOrangePrimary,
                            indicatorColor = GymOrangePrimary.copy(alpha = 0.15f)
                        ),
                        modifier = Modifier.testTag("nav_workouts")
                    )

                    NavigationBarItem(
                        selected = selectedTab == 1,
                        onClick = { viewModel.selectTab(1) },
                        icon = {
                            Icon(
                                if (selectedTab == 1) Icons.Filled.ShowChart else Icons.Outlined.ShowChart,
                                contentDescription = "Progress"
                            )
                        },
                        label = { Text("Progress", fontSize = 11.sp, fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = GymOrangePrimary,
                            selectedTextColor = GymOrangePrimary,
                            indicatorColor = GymOrangePrimary.copy(alpha = 0.15f)
                        ),
                        modifier = Modifier.testTag("nav_progress")
                    )

                    NavigationBarItem(
                        selected = selectedTab == 2,
                        onClick = { viewModel.selectTab(2) },
                        icon = {
                            Icon(
                                if (selectedTab == 2) Icons.Filled.Restaurant else Icons.Outlined.Restaurant,
                                contentDescription = "Nutrition"
                            )
                        },
                        label = { Text("Nutrition", fontSize = 11.sp, fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = GymOrangePrimary,
                            selectedTextColor = GymOrangePrimary,
                            indicatorColor = GymOrangePrimary.copy(alpha = 0.15f)
                        ),
                        modifier = Modifier.testTag("nav_nutrition")
                    )

                    NavigationBarItem(
                        selected = selectedTab == 3,
                        onClick = { viewModel.selectTab(3) },
                        icon = {
                            Icon(
                                if (selectedTab == 3) Icons.Filled.Group else Icons.Outlined.Group,
                                contentDescription = "Community"
                            )
                        },
                        label = { Text("Community", fontSize = 11.sp, fontWeight = if (selectedTab == 3) FontWeight.Bold else FontWeight.Normal) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = GymOrangePrimary,
                            selectedTextColor = GymOrangePrimary,
                            indicatorColor = GymOrangePrimary.copy(alpha = 0.15f)
                        ),
                        modifier = Modifier.testTag("nav_community")
                    )

                    NavigationBarItem(
                        selected = selectedTab == 4,
                        onClick = { viewModel.selectTab(4) },
                        icon = {
                            Icon(
                                if (selectedTab == 4) Icons.Filled.Notifications else Icons.Outlined.Notifications,
                                contentDescription = "Reminders"
                            )
                        },
                        label = { Text("Alerts", fontSize = 11.sp, fontWeight = if (selectedTab == 4) FontWeight.Bold else FontWeight.Normal) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = GymOrangePrimary,
                            selectedTextColor = GymOrangePrimary,
                            indicatorColor = GymOrangePrimary.copy(alpha = 0.15f)
                        ),
                        modifier = Modifier.testTag("nav_reminders")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                0 -> WorkoutsScreen(viewModel = viewModel)
                1 -> ProgressScreen(viewModel = viewModel)
                2 -> NutritionScreen(viewModel = viewModel)
                3 -> CommunityScreen(viewModel = viewModel)
                4 -> RemindersScreen(viewModel = viewModel)
            }
        }
    }

    if (celebrationEvent != null) {
        CelebrationOverlay(
            event = celebrationEvent!!,
            onDismiss = { viewModel.dismissCelebration() },
            onShareToFeed = { viewModel.shareCelebrationToCommunity(it) }
        )
    }
}
}

// Kept for screenshot test backward compatibility
@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(text = "Hello $name!", modifier = modifier)
}
