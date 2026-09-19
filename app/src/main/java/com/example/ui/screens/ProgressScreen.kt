package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BodyMetric
import com.example.data.model.CelebrationEvent
import com.example.data.model.CelebrationType
import com.example.data.model.FitnessMilestone
import com.example.ui.GymViewModel
import com.example.ui.components.MetricStatCard
import com.example.ui.components.SectionHeader
import com.example.ui.theme.GymCyanSecondary
import com.example.ui.theme.GymGreenTertiary
import com.example.ui.theme.GymOrangePrimary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ProgressScreen(
    viewModel: GymViewModel,
    modifier: Modifier = Modifier
) {
    val metrics by viewModel.bodyMetrics.collectAsState()
    val prs by viewModel.personalRecords.collectAsState()
    val milestones by viewModel.fitnessMilestones.collectAsState()

    var showLogMetricDialog by remember { mutableStateOf(false) }
    var showPrDialog by remember { mutableStateOf(false) }

    // Weigh in state
    var inputWeight by remember { mutableStateOf("") }
    var inputBodyFat by remember { mutableStateOf("") }
    var inputWaist by remember { mutableStateOf("") }
    var inputChest by remember { mutableStateOf("") }
    var inputArms by remember { mutableStateOf("") }
    var inputNotes by remember { mutableStateOf("") }

    // PR state
    var prExerciseName by remember { mutableStateOf("") }
    var prWeight by remember { mutableStateOf("") }
    var prReps by remember { mutableStateOf("") }

    val latestMetric = metrics.firstOrNull()
    val oldestMetric = metrics.lastOrNull()
    val weightDelta = if (latestMetric != null && oldestMetric != null && metrics.size > 1) {
        latestMetric.weightKg - oldestMetric.weightKg
    } else 0.0

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
                title = "Progress Monitoring",
                subtitle = "Weight trends, 1RM strength & body measurements",
                actionText = "+ Log Weight",
                onActionClick = { showLogMetricDialog = true }
            )
        }

        // Summary Metric Cards
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricStatCard(
                    title = "Current Weight",
                    value = latestMetric?.let { "${it.weightKg} kg" } ?: "--",
                    subtitle = if (weightDelta != 0.0) String.format("%+.1f kg overall", weightDelta) else "Baseline",
                    icon = Icons.Default.MonitorWeight,
                    iconTint = GymOrangePrimary,
                    modifier = Modifier.weight(1f)
                )
                MetricStatCard(
                    title = "Body Fat",
                    value = latestMetric?.bodyFatPercentage?.let { "$it %" } ?: "--",
                    subtitle = "Measured estimate",
                    icon = Icons.Default.ShowChart,
                    iconTint = GymCyanSecondary,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Body Weight Trend Chart Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("weight_progress_chart_card")
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Weight Trend (Recent)",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Chronological body weight tracking",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(GymOrangePrimary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.ShowChart, contentDescription = null, tint = GymOrangePrimary, modifier = Modifier.size(18.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (metrics.size >= 2) {
                        WeightChartCanvas(
                            metrics = metrics.reversed(), // chronological order
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Log at least 2 weigh-ins to generate chart",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Personal Records (1RM) Section
        item {
            SectionHeader(
                title = "Personal Records & 1RM",
                subtitle = "Epley calculated One-Rep Max",
                actionText = "+ Log PR",
                onActionClick = { showPrDialog = true }
            )
        }

        items(prs) { pr ->
            Card(
                shape = RoundedCornerShape(16.dp),
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
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(GymOrangePrimary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.EmojiEvents,
                                contentDescription = null,
                                tint = GymOrangePrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Column {
                            Text(
                                text = pr.exerciseName,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Best: ${pr.maxWeightKg} kg × ${pr.maxReps} reps",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "${pr.estimated1RmKg} kg",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Black,
                                    fontSize = 20.sp
                                ),
                                color = GymOrangePrimary
                            )
                            Text(
                                text = "Est. 1RM",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        IconButton(
                            onClick = {
                                viewModel.triggerCelebration(
                                    CelebrationEvent(
                                        type = CelebrationType.PERSONAL_RECORD,
                                        title = "Personal Record! 🥇",
                                        subtitle = pr.exerciseName,
                                        badgeIcon = "🥇",
                                        statHighlight = "${pr.maxWeightKg} kg × ${pr.maxReps} reps (Est. 1RM: ${pr.estimated1RmKg} kg)",
                                        description = "Outstanding strength milestone on ${pr.exerciseName}! Your consistency and dedication are showing results.",
                                        xpEarned = 350
                                    )
                                )
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                Icons.Default.AutoAwesome,
                                contentDescription = "Celebrate PR",
                                tint = GymOrangePrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }

        // Fitness Milestones & Achievements Showcase
        item {
            Spacer(modifier = Modifier.height(8.dp))
            SectionHeader(
                title = "Fitness Milestones & Trophies",
                subtitle = "Tap unlocked milestones to replay celebrations"
            )
        }

        items(milestones) { milestone ->
            val progressRatio = (milestone.currentValue / milestone.targetValue).toFloat().coerceIn(0f, 1f)
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (milestone.isAchieved) {
                        MaterialTheme.colorScheme.surfaceVariant
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    }
                ),
                border = if (milestone.isAchieved) {
                    BorderStroke(1.dp, GymCyanSecondary.copy(alpha = 0.5f))
                } else null,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("milestone_${milestone.id}")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (milestone.isAchieved) GymCyanSecondary.copy(alpha = 0.2f)
                                        else MaterialTheme.colorScheme.surface
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = milestone.badgeIcon, fontSize = 22.sp)
                            }

                            Column {
                                Text(
                                    text = milestone.title,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = milestone.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        if (milestone.isAchieved) {
                            Button(
                                onClick = { viewModel.celebrateMilestone(milestone) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = GymCyanSecondary,
                                    contentColor = Color.Black
                                ),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text("Celebrate 🎉", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${milestone.currentValue.toInt()} / ${milestone.targetValue.toInt()} ${milestone.unit}",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (milestone.isAchieved) GymCyanSecondary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = if (milestone.isAchieved) "UNLOCKED 🏆" else "${(progressRatio * 100).toInt()}%",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (milestone.isAchieved) GymGreenTertiary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { progressRatio },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = if (milestone.isAchieved) GymGreenTertiary else GymCyanSecondary,
                        trackColor = MaterialTheme.colorScheme.surface
                    )
                }
            }
        }

        // Measurement History
        item {
            SectionHeader(
                title = "Weigh-in & Measurement Log",
                subtitle = "Historical body metric entries"
            )
        }

        items(metrics) { item ->
            val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
            val dateStr = dateFormat.format(Date(item.timestamp))

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${item.weightKg} kg",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (item.bodyFatPercentage != null) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = GymCyanSecondary.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = "${item.bodyFatPercentage}% BF",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = GymCyanSecondary,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = dateStr,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            IconButton(
                                onClick = { viewModel.deleteBodyMetric(item.id) },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    if (item.waistCm != null || item.chestCm != null || item.armCm != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            item.chestCm?.let {
                                Text("Chest: ${it}cm", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            item.waistCm?.let {
                                Text("Waist: ${it}cm", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            item.armCm?.let {
                                Text("Arms: ${it}cm", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }

                    if (item.notes.isNotBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = item.notes,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(80.dp)) }
    }

    // Log Weigh-in Dialog
    if (showLogMetricDialog) {
        AlertDialog(
            onDismissRequest = { showLogMetricDialog = false },
            title = { Text("Log Body Weigh-in") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = inputWeight,
                        onValueChange = { inputWeight = it },
                        label = { Text("Weight (kg) *") },
                        placeholder = { Text("e.g. 75.5") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = inputBodyFat,
                        onValueChange = { inputBodyFat = it },
                        label = { Text("Body Fat % (optional)") },
                        placeholder = { Text("e.g. 15.0") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = inputWaist,
                            onValueChange = { inputWaist = it },
                            label = { Text("Waist (cm)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = inputChest,
                            onValueChange = { inputChest = it },
                            label = { Text("Chest (cm)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    OutlinedTextField(
                        value = inputArms,
                        onValueChange = { inputArms = it },
                        label = { Text("Arms (cm)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = inputNotes,
                        onValueChange = { inputNotes = it },
                        label = { Text("Notes") },
                        placeholder = { Text("e.g. Morning fasting weight") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val w = inputWeight.toDoubleOrNull()
                        if (w != null && w > 0) {
                            viewModel.logBodyMetric(
                                weightKg = w,
                                bodyFat = inputBodyFat.toDoubleOrNull(),
                                chest = inputChest.toDoubleOrNull(),
                                waist = inputWaist.toDoubleOrNull(),
                                arms = inputArms.toDoubleOrNull(),
                                notes = inputNotes
                            )
                            inputWeight = ""
                            inputBodyFat = ""
                            inputWaist = ""
                            inputChest = ""
                            inputArms = ""
                            inputNotes = ""
                            showLogMetricDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GymOrangePrimary)
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogMetricDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Log Personal Record Dialog
    if (showPrDialog) {
        AlertDialog(
            onDismissRequest = { showPrDialog = false },
            title = { Text("Log Personal Record (PR)") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = prExerciseName,
                        onValueChange = { prExerciseName = it },
                        label = { Text("Exercise Name *") },
                        placeholder = { Text("e.g. Barbell Bench Press") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = prWeight,
                        onValueChange = { prWeight = it },
                        label = { Text("Weight (kg) *") },
                        placeholder = { Text("e.g. 100") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = prReps,
                        onValueChange = { prReps = it },
                        label = { Text("Reps Performed *") },
                        placeholder = { Text("e.g. 3") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val w = prWeight.toDoubleOrNull()
                        val r = prReps.toIntOrNull()
                        if (prExerciseName.isNotBlank() && w != null && r != null) {
                            viewModel.logPersonalRecord(prExerciseName, w, r)
                            prExerciseName = ""
                            prWeight = ""
                            prReps = ""
                            showPrDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GymOrangePrimary)
                ) {
                    Text("Save PR")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPrDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun WeightChartCanvas(
    metrics: List<BodyMetric>,
    modifier: Modifier = Modifier
) {
    val weights = metrics.map { it.weightKg }
    val minWeight = (weights.minOrNull() ?: 60.0) - 1.0
    val maxWeight = (weights.maxOrNull() ?: 90.0) + 1.0
    val range = (maxWeight - minWeight).coerceAtLeast(1.0)

    val lineColor = GymOrangePrimary
    val gradientColor = GymOrangePrimary.copy(alpha = 0.2f)

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val pointsCount = weights.size
        val stepX = width / (pointsCount - 1).coerceAtLeast(1)

        val path = Path()
        val fillPath = Path()

        val points = weights.mapIndexed { index, weight ->
            val x = index * stepX
            val normalizedY = ((weight - minWeight) / range).toFloat()
            val y = height - (normalizedY * (height - 30.dp.toPx())) - 15.dp.toPx()
            Offset(x, y)
        }

        if (points.isNotEmpty()) {
            path.moveTo(points.first().x, points.first().y)
            fillPath.moveTo(points.first().x, height)
            fillPath.lineTo(points.first().x, points.first().y)

            for (i in 1 until points.size) {
                val prev = points[i - 1]
                val curr = points[i]
                val controlX1 = prev.x + (curr.x - prev.x) / 2
                val controlY1 = prev.y
                val controlX2 = prev.x + (curr.x - prev.x) / 2
                val controlY2 = curr.y

                path.cubicTo(controlX1, controlY1, controlX2, controlY2, curr.x, curr.y)
                fillPath.cubicTo(controlX1, controlY1, controlX2, controlY2, curr.x, curr.y)
            }

            fillPath.lineTo(points.last().x, height)
            fillPath.close()

            // Draw Area Gradient
            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(gradientColor, Color.Transparent),
                    startY = 0f,
                    endY = height
                )
            )

            // Draw Line
            drawPath(
                path = path,
                color = lineColor,
                style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round)
            )

            // Draw Point Circles
            points.forEach { point ->
                drawCircle(
                    color = Color.White,
                    radius = 5.dp.toPx(),
                    center = point
                )
                drawCircle(
                    color = lineColor,
                    radius = 3.dp.toPx(),
                    center = point
                )
            }
        }
    }
}
