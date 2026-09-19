package com.example.ui.screens

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MealLog
import com.example.ui.GymViewModel
import com.example.ui.components.CalorieCircularProgress
import com.example.ui.components.MacroProgressBar
import com.example.ui.components.SectionHeader
import com.example.ui.theme.GymCyanSecondary
import com.example.ui.theme.GymGreenTertiary
import com.example.ui.theme.GymOrangePrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NutritionScreen(
    viewModel: GymViewModel,
    modifier: Modifier = Modifier
) {
    val profileState by viewModel.nutritionProfile.collectAsState()
    val meals by viewModel.allMeals.collectAsState()

    val profile = profileState

    var showProfileDialog by remember { mutableStateOf(false) }
    var showAddMealDialog by remember { mutableStateOf(false) }

    // Meal input state
    var selectedMealType by remember { mutableStateOf("Lunch") }
    var foodNameInput by remember { mutableStateOf("") }
    var caloriesInput by remember { mutableStateOf("") }
    var proteinInput by remember { mutableStateOf("") }
    var carbsInput by remember { mutableStateOf("") }
    var fatsInput by remember { mutableStateOf("") }

    // Profile input state
    var editGoal by remember { mutableStateOf(profile?.goal ?: "Muscle Gain") }
    var editWeight by remember { mutableStateOf(profile?.currentWeightKg?.toString() ?: "75.0") }
    var editHeight by remember { mutableStateOf(profile?.heightCm?.toString() ?: "178.0") }
    var editAge by remember { mutableStateOf(profile?.age?.toString() ?: "26") }
    var editGender by remember { mutableStateOf(profile?.gender ?: "Male") }
    var editActivity by remember { mutableStateOf(profile?.activityLevel ?: "High") }

    val totalCaloriesConsumed = meals.sumOf { it.calories }
    val totalProteinConsumed = meals.sumOf { it.proteinG }
    val totalCarbsConsumed = meals.sumOf { it.carbsG }
    val totalFatsConsumed = meals.sumOf { it.fatsG }

    val calorieTarget = profile?.dailyCalorieTarget ?: 2650
    val proteinTarget = profile?.proteinGramsTarget?.toDouble() ?: 165.0
    val carbsTarget = profile?.carbsGramsTarget?.toDouble() ?: 300.0
    val fatsTarget = profile?.fatsGramsTarget?.toDouble() ?: 70.0

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
                title = "Personalized Nutrition Planner",
                subtitle = "Tailored macronutrient targets and daily meal log",
                actionText = "+ Log Meal",
                onActionClick = { showAddMealDialog = true }
            )
        }

        // Daily Calorie & Macro Target Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("nutrition_summary_card")
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Today's Energy Balance",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Goal: ${profile?.goal ?: "Muscle Gain"}",
                                style = MaterialTheme.typography.bodySmall,
                                color = GymOrangePrimary
                            )
                        }

                        Surface(
                            onClick = {
                                profile?.let {
                                    editGoal = it.goal
                                    editWeight = it.currentWeightKg.toString()
                                    editHeight = it.heightCm.toString()
                                    editAge = it.age.toString()
                                    editGender = it.gender
                                    editActivity = it.activityLevel
                                }
                                showProfileDialog = true
                            },
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surface
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(16.dp), tint = GymOrangePrimary)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Adjust Plan", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        CalorieCircularProgress(
                            caloriesConsumed = totalCaloriesConsumed,
                            calorieTarget = calorieTarget
                        )

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            MacroProgressBar(
                                label = "Protein",
                                current = totalProteinConsumed,
                                target = proteinTarget,
                                color = GymOrangePrimary
                            )
                            MacroProgressBar(
                                label = "Carbs",
                                current = totalCarbsConsumed,
                                target = carbsTarget,
                                color = GymCyanSecondary
                            )
                            MacroProgressBar(
                                label = "Fats",
                                current = totalFatsConsumed,
                                target = fatsTarget,
                                color = GymGreenTertiary
                            )
                        }
                    }
                }
            }
        }

        // Daily Meals Log
        item {
            SectionHeader(
                title = "Logged Meals",
                subtitle = "Today's nutrition entries"
            )
        }

        if (meals.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.Restaurant, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(36.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No meals logged yet", style = MaterialTheme.typography.titleMedium)
                        Text("Tap '+ Log Meal' to track your protein and calories", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        } else {
            items(meals) { meal ->
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
                        Column(modifier = Modifier.weight(1f)) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.surface
                            ) {
                                Text(
                                    text = meal.mealType,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = GymOrangePrimary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = meal.foodName,
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text(
                                    text = "P: ${meal.proteinG.toInt()}g",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                    color = GymOrangePrimary
                                )
                                Text(
                                    text = "C: ${meal.carbsG.toInt()}g",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                    color = GymCyanSecondary
                                )
                                Text(
                                    text = "F: ${meal.fatsG.toInt()}g",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                    color = GymGreenTertiary
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${meal.calories} kcal",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            IconButton(
                                onClick = { viewModel.deleteMeal(meal.id) },
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
                }
            }
        }

        item { Spacer(modifier = Modifier.height(80.dp)) }
    }

    // Add Meal Dialog
    if (showAddMealDialog) {
        AlertDialog(
            onDismissRequest = { showAddMealDialog = false },
            title = { Text("Log Food / Meal") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("Breakfast", "Lunch", "Dinner", "Snack").forEach { type ->
                            FilterChip(
                                selected = selectedMealType == type,
                                onClick = { selectedMealType = type },
                                label = { Text(type, fontSize = 12.sp) }
                            )
                        }
                    }

                    OutlinedTextField(
                        value = foodNameInput,
                        onValueChange = { foodNameInput = it },
                        label = { Text("Food Description *") },
                        placeholder = { Text("e.g. Chicken breast with sweet potato") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = caloriesInput,
                        onValueChange = { caloriesInput = it },
                        label = { Text("Calories (kcal) *") },
                        placeholder = { Text("e.g. 520") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = proteinInput,
                            onValueChange = { proteinInput = it },
                            label = { Text("Protein (g)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = carbsInput,
                            onValueChange = { carbsInput = it },
                            label = { Text("Carbs (g)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = fatsInput,
                            onValueChange = { fatsInput = it },
                            label = { Text("Fats (g)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val cal = caloriesInput.toIntOrNull()
                        if (foodNameInput.isNotBlank() && cal != null) {
                            viewModel.addMeal(
                                mealType = selectedMealType,
                                foodName = foodNameInput,
                                calories = cal,
                                proteinG = proteinInput.toDoubleOrNull() ?: 0.0,
                                carbsG = carbsInput.toDoubleOrNull() ?: 0.0,
                                fatsG = fatsInput.toDoubleOrNull() ?: 0.0
                            )
                            foodNameInput = ""
                            caloriesInput = ""
                            proteinInput = ""
                            carbsInput = ""
                            fatsInput = ""
                            showAddMealDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GymOrangePrimary)
                ) {
                    Text("Add Meal")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddMealDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Adjust Nutrition Plan Profile Dialog
    if (showProfileDialog) {
        AlertDialog(
            onDismissRequest = { showProfileDialog = false },
            title = { Text("Personalize Nutrition Plan") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Select your primary objective:", style = MaterialTheme.typography.labelMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("Muscle Gain", "Fat Loss", "Maintenance").forEach { g ->
                            FilterChip(
                                selected = editGoal == g,
                                onClick = { editGoal = g },
                                label = { Text(g, fontSize = 12.sp) }
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = editWeight,
                            onValueChange = { editWeight = it },
                            label = { Text("Weight (kg)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = editHeight,
                            onValueChange = { editHeight = it },
                            label = { Text("Height (cm)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = editAge,
                            onValueChange = { editAge = it },
                            label = { Text("Age") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = editGender,
                            onValueChange = { editGender = it },
                            label = { Text("Gender") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Text("Activity Level:", style = MaterialTheme.typography.labelMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("Moderate", "High", "Athlete").forEach { act ->
                            FilterChip(
                                selected = editActivity == act,
                                onClick = { editActivity = act },
                                label = { Text(act, fontSize = 12.sp) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val w = editWeight.toDoubleOrNull() ?: 75.0
                        val h = editHeight.toDoubleOrNull() ?: 178.0
                        val a = editAge.toIntOrNull() ?: 26
                        viewModel.saveNutritionProfile(
                            goal = editGoal,
                            currentWeight = w,
                            height = h,
                            age = a,
                            gender = editGender,
                            activityLevel = editActivity
                        )
                        showProfileDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GymOrangePrimary)
                ) {
                    Text("Recalculate Macros")
                }
            },
            dismissButton = {
                TextButton(onClick = { showProfileDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
