package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.model.ReminderSettings
import com.example.data.repository.GymRepository
import com.example.ui.GymViewModel
import com.example.ui.components.SectionHeader
import com.example.ui.theme.GymCyanSecondary
import com.example.ui.theme.GymGreenTertiary
import com.example.ui.theme.GymOrangePrimary

@Composable
fun RemindersScreen(
    viewModel: GymViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val settingsState by viewModel.reminderSettings.collectAsState()

    var hasNotificationPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            } else true
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasNotificationPermission = isGranted
        if (isGranted) {
            Toast.makeText(context, "Notification permission granted!", Toast.LENGTH_SHORT).show()
        }
    }

    // Local mutable copy of settings for toggling
    val currentSettings = settingsState ?: ReminderSettings()
    var dailyMotivationEnabled by remember(currentSettings) { mutableStateOf(currentSettings.dailyMotivationEnabled) }
    var workoutReminderEnabled by remember(currentSettings) { mutableStateOf(currentSettings.workoutReminderEnabled) }
    var waterReminderEnabled by remember(currentSettings) { mutableStateOf(currentSettings.waterReminderEnabled) }

    var motivationHour by remember(currentSettings) { mutableIntStateOf(currentSettings.motivationHour) }
    var motivationMinute by remember(currentSettings) { mutableIntStateOf(currentSettings.motivationMinute) }
    var workoutHour by remember(currentSettings) { mutableIntStateOf(currentSettings.workoutReminderHour) }
    var workoutMinute by remember(currentSettings) { mutableIntStateOf(currentSettings.workoutReminderMinute) }

    var quoteIndex by remember { mutableIntStateOf(0) }
    val quotes = GymRepository.MOTIVATION_QUOTES
    val currentQuote = quotes[quoteIndex % quotes.size]

    var showTimeDialogFor by remember { mutableStateOf<String?>(null) } // "motivation" or "workout"

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            SectionHeader(
                title = "Motivation & Push Reminders",
                subtitle = "Daily fitness mindset and scheduled workout alerts"
            )
        }

        // Notification Permission Banner (for Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasNotificationPermission) {
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(GymOrangePrimary.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Notifications, contentDescription = null, tint = GymOrangePrimary)
                            }
                            Column {
                                Text("Enable Push Notifications", fontWeight = FontWeight.Bold)
                                Text("Receive daily motivation & workout alerts", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        Button(
                            onClick = {
                                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = GymOrangePrimary),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Enable")
                        }
                    }
                }
            }
        }

        // Quote of the Day Hero Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("motivation_quote_card")
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.FlashOn, contentDescription = null, tint = GymOrangePrimary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "DAILY GYM MINDSET",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, letterSpacing = 1.2.sp),
                                color = GymOrangePrimary
                            )
                        }

                        IconButton(
                            onClick = { quoteIndex = (quoteIndex + 1) % quotes.size },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Default.Autorenew, contentDescription = "Next Quote", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "“$currentQuote”",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontStyle = FontStyle.Italic,
                            fontSize = 17.sp,
                            lineHeight = 24.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // Instant Notification Push Testers
        item {
            SectionHeader(
                title = "Live Push Notification Test",
                subtitle = "Trigger immediate alerts into your Android status bar"
            )
        }

        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Verify real Android notifications in your notification tray:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasNotificationPermission) {
                                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                } else {
                                    viewModel.triggerInstantMotivationNotification()
                                    Toast.makeText(context, "Motivation notification triggered!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = GymOrangePrimary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("trigger_motivation_button")
                        ) {
                            Icon(Icons.Default.FlashOn, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Send Quote", fontSize = 13.sp)
                        }

                        Button(
                            onClick = {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasNotificationPermission) {
                                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                } else {
                                    viewModel.triggerInstantWorkoutReminder()
                                    Toast.makeText(context, "Workout reminder triggered!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = GymCyanSecondary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("trigger_workout_reminder_button")
                        ) {
                            Icon(Icons.Default.FitnessCenter, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Send Reminder", fontSize = 13.sp)
                        }
                    }
                }
            }
        }

        // Scheduled Reminders Configuration
        item {
            SectionHeader(
                title = "Scheduled Daily Reminders",
                subtitle = "Automated alarms powered by Android AlarmManager"
            )
        }

        // Daily Motivation Reminder Setting
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = GymOrangePrimary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Daily Motivation Alert", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Morning fuel: ${String.format("%02d:%02d", motivationHour, motivationMinute)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedButton(
                            onClick = { showTimeDialogFor = "motivation" },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Text(String.format("%02d:%02d", motivationHour, motivationMinute), fontSize = 12.sp)
                        }

                        Switch(
                            checked = dailyMotivationEnabled,
                            onCheckedChange = {
                                dailyMotivationEnabled = it
                                viewModel.updateReminderSettings(
                                    currentSettings.copy(dailyMotivationEnabled = it)
                                )
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = GymOrangePrimary, checkedTrackColor = GymOrangePrimary.copy(alpha = 0.4f))
                        )
                    }
                }
            }
        }

        // Scheduled Workout Reminder Setting
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Alarm, contentDescription = null, tint = GymCyanSecondary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Workout Session Reminder", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Scheduled time: ${String.format("%02d:%02d", workoutHour, workoutMinute)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedButton(
                            onClick = { showTimeDialogFor = "workout" },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Text(String.format("%02d:%02d", workoutHour, workoutMinute), fontSize = 12.sp)
                        }

                        Switch(
                            checked = workoutReminderEnabled,
                            onCheckedChange = {
                                workoutReminderEnabled = it
                                viewModel.updateReminderSettings(
                                    currentSettings.copy(workoutReminderEnabled = it)
                                )
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = GymCyanSecondary, checkedTrackColor = GymCyanSecondary.copy(alpha = 0.4f))
                        )
                    }
                }
            }
        }

        // Water Reminder Setting
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.WaterDrop, contentDescription = null, tint = GymGreenTertiary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Hydration Reminders", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Regular hydration prompts throughout training days", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    Switch(
                        checked = waterReminderEnabled,
                        onCheckedChange = {
                            waterReminderEnabled = it
                            viewModel.updateReminderSettings(
                                currentSettings.copy(waterReminderEnabled = it)
                            )
                        },
                        colors = SwitchDefaults.colors(checkedThumbColor = GymGreenTertiary, checkedTrackColor = GymGreenTertiary.copy(alpha = 0.4f))
                    )
                }
            }
        }

        item {
            Button(
                onClick = {
                    viewModel.updateReminderSettings(
                        ReminderSettings(
                            dailyMotivationEnabled = dailyMotivationEnabled,
                            motivationHour = motivationHour,
                            motivationMinute = motivationMinute,
                            workoutReminderEnabled = workoutReminderEnabled,
                            workoutReminderHour = workoutHour,
                            workoutReminderMinute = workoutMinute,
                            waterReminderEnabled = waterReminderEnabled
                        )
                    )
                    Toast.makeText(context, "Reminder preferences saved!", Toast.LENGTH_SHORT).show()
                },
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GymOrangePrimary),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Save Schedule Preferences", fontWeight = FontWeight.Bold)
            }
        }

        item { Spacer(modifier = Modifier.height(80.dp)) }
    }

    // Quick Time Picker Dialog
    if (showTimeDialogFor != null) {
        val isMotivation = showTimeDialogFor == "motivation"
        var selectedHour by remember { mutableIntStateOf(if (isMotivation) motivationHour else workoutHour) }
        var selectedMin by remember { mutableIntStateOf(if (isMotivation) motivationMinute else workoutMinute) }

        AlertDialog(
            onDismissRequest = { showTimeDialogFor = null },
            title = { Text(if (isMotivation) "Set Daily Motivation Time" else "Set Workout Reminder Time") },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = String.format("%02d:%02d", selectedHour, selectedMin),
                        style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                        color = GymOrangePrimary
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Hour (0-23)", style = MaterialTheme.typography.labelSmall)
                            Row {
                                TextButton(onClick = { selectedHour = (selectedHour - 1 + 24) % 24 }) { Text("-") }
                                Text("${selectedHour}", modifier = Modifier.padding(top = 10.dp), fontWeight = FontWeight.Bold)
                                TextButton(onClick = { selectedHour = (selectedHour + 1) % 24 }) { Text("+") }
                            }
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Minute", style = MaterialTheme.typography.labelSmall)
                            Row {
                                TextButton(onClick = { selectedMin = (selectedMin - 15 + 60) % 60 }) { Text("-15") }
                                Text("${selectedMin}", modifier = Modifier.padding(top = 10.dp), fontWeight = FontWeight.Bold)
                                TextButton(onClick = { selectedMin = (selectedMin + 15) % 60 }) { Text("+15") }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (isMotivation) {
                            motivationHour = selectedHour
                            motivationMinute = selectedMin
                            viewModel.updateReminderSettings(
                                currentSettings.copy(
                                    motivationHour = selectedHour,
                                    motivationMinute = selectedMin
                                )
                            )
                        } else {
                            workoutHour = selectedHour
                            workoutMinute = selectedMin
                            viewModel.updateReminderSettings(
                                currentSettings.copy(
                                    workoutReminderHour = selectedHour,
                                    workoutReminderMinute = selectedMin
                                )
                            )
                        }
                        showTimeDialogFor = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GymOrangePrimary)
                ) {
                    Text("Done")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimeDialogFor = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}
