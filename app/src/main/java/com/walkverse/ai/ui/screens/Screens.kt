package com.walkverse.ai.ui.screens

import androidx.compose.animation.*
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.TextStyle
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.walkverse.ai.domain.model.*
import com.walkverse.ai.ui.navigation.Screen
import com.walkverse.ai.ui.WalkViewModel
import com.walkverse.ai.ui.components.*
import com.walkverse.ai.ui.theme.LocalShadcnColors
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.format.DateTimeFormatter

// ==========================================
// 1. SPLASH SCREEN
// ==========================================
@Composable
fun SplashScreen(navController: NavController, viewModel: WalkViewModel) {
    val colors = LocalShadcnColors.current
    val onboardingCompleted by viewModel.onboardingCompleted.collectAsState()

    LaunchedEffect(key1 = true) {
        delay(2000)
        if (onboardingCompleted) {
            navController.navigate("home") {
                popUpTo("splash") { inclusive = true }
            }
        } else {
            navController.navigate("onboarding") {
                popUpTo("splash") { inclusive = true }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .border(2.dp, colors.primary, CircleShape)
                    .background(colors.surface, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.DirectionsWalk,
                    contentDescription = "WalkVerse Logo",
                    tint = colors.primary,
                    modifier = Modifier.size(48.dp)
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "WalkVerse AI",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Habit Loop • Pet Companions • Garden Oasis",
                fontSize = 12.sp,
                color = colors.textMuted
            )
        }
    }
}

// ==========================================
// 2. ONBOARDING SCREEN
// ==========================================
@Composable
fun OnboardingScreen(navController: NavController, viewModel: WalkViewModel) {
    val colors = LocalShadcnColors.current
    var name by remember { mutableStateOf("") }
    var stepGoal by remember { mutableStateOf("8000") }
    var selectedPetType by remember { mutableStateOf("CAT") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Welcome to WalkVerse",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Start walking to level up your virtual companion and grow a beautiful garden.",
                fontSize = 14.sp,
                color = colors.textMuted,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(32.dp))

            ShadcnCard(modifier = Modifier.fillMaxWidth()) {
                Text("Enter Your Name", fontWeight = FontWeight.SemiBold, color = colors.textPrimary)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    placeholder = { Text("Walker", color = colors.textMuted) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = colors.textPrimary,
                        unfocusedTextColor = colors.textPrimary,
                        focusedBorderColor = colors.primary,
                        unfocusedBorderColor = colors.border,
                        focusedPlaceholderColor = colors.textMuted,
                        unfocusedPlaceholderColor = colors.textMuted
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text("Daily Step Goal", fontWeight = FontWeight.SemiBold, color = colors.textPrimary)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = stepGoal,
                    onValueChange = { stepGoal = it.filter { char -> char.isDigit() } },
                    placeholder = { Text("8000", color = colors.textMuted) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = colors.textPrimary,
                        unfocusedTextColor = colors.textPrimary,
                        focusedBorderColor = colors.primary,
                        unfocusedBorderColor = colors.border,
                        focusedPlaceholderColor = colors.textMuted,
                        unfocusedPlaceholderColor = colors.textMuted
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text("Choose Your Virtual Pet", fontWeight = FontWeight.SemiBold, color = colors.textPrimary)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val petOptions = listOf("CAT", "DOG", "DRAGON", "ROBOT")
                    petOptions.forEach { type ->
                        val isSelected = selectedPetType == type
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .border(
                                    1.dp,
                                    if (isSelected) colors.primary else colors.border,
                                    RoundedCornerShape(8.dp)
                                )
                                .background(if (isSelected) colors.primary.copy(alpha = 0.1f) else colors.surface)
                                .clickable { selectedPetType = type }
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = when (type) {
                                        "CAT" -> Icons.Default.Pets
                                        "DOG" -> Icons.Default.Pets
                                        "DRAGON" -> Icons.Default.Terrain
                                        else -> Icons.Default.Settings
                                    },
                                    contentDescription = type,
                                    tint = if (isSelected) colors.primary else colors.textMuted,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = type,
                                    fontSize = 10.sp,
                                    color = if (isSelected) colors.textPrimary else colors.textMuted
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            ShadcnButton(
                text = "Get Started",
                onClick = {
                    val finalName = name.ifEmpty { "Walker" }
                    val finalGoal = stepGoal.toIntOrNull() ?: 8000
                    viewModel.completeOnboarding(finalName, finalGoal, selectedPetType)
                    navController.navigate("home") {
                        popUpTo("onboarding") { inclusive = true }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

// ==========================================
// 3. HOME SCREEN
// ==========================================
@Composable
fun HomeScreen(navController: NavController, viewModel: WalkViewModel) {
    val colors = LocalShadcnColors.current
    val name by viewModel.userName.collectAsState()
    val level by viewModel.userLevel.collectAsState()
    val xp by viewModel.userXp.collectAsState()
    val gemsCount by viewModel.gems.collectAsState()
    val streak by viewModel.streakCount.collectAsState()
    val today by viewModel.todayRecord.collectAsState()

    val currentSteps = today?.steps ?: 0
    val goal = today?.goal ?: 8000
    val progress = today?.progress ?: 0f

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Greeting & Profile Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Hello, $name!",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                    Text(
                        text = "Let's make today active.",
                        fontSize = 14.sp,
                        color = colors.textMuted
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ShadcnBadge(
                        text = "Lv. $level",
                        containerColor = colors.primary,
                        textColor = if (colors.primary == Color.White) Color.Black else Color.White
                    )
                    ShadcnBadge(
                        text = "💎 $gemsCount",
                        containerColor = colors.surface,
                        textColor = colors.textPrimary
                    )
                }
            }
        }

        // Circular Step Ring & Live Activity Recognition Status
        item {
            val activeActivity by viewModel.currentActivity.collectAsState()
            val activeConfidence by viewModel.currentConfidence.collectAsState()

            ShadcnCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        ShadcnProgressRing(
                            progress = progress,
                            steps = currentSteps,
                            goal = goal,
                            modifier = Modifier.size(190.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Dynamic Activity Status Badge
                    val activityIcon = when (activeActivity) {
                        "Walking" -> Icons.Default.DirectionsWalk
                        "Running" -> Icons.Default.DirectionsRun
                        "Stationary" -> Icons.Default.Pause
                        "In Vehicle" -> Icons.Default.DirectionsCar
                        "Shaking/Vibrating" -> Icons.Default.Warning
                        else -> Icons.Default.Refresh
                    }
                    
                    val activityColor = when (activeActivity) {
                        "Walking" -> colors.success
                        "Running" -> colors.primary
                        "Stationary" -> colors.textMuted
                        "In Vehicle" -> Color(0xFFF59E0B) // Amber
                        "Shaking/Vibrating" -> Color(0xFFEF4444) // Red
                        else -> colors.textMuted
                    }

                    Row(
                        modifier = Modifier
                            .background(activityColor.copy(alpha = 0.08f), RoundedCornerShape(20.dp))
                            .border(1.dp, activityColor.copy(alpha = 0.2f), RoundedCornerShape(20.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = activityIcon,
                            contentDescription = activeActivity,
                            tint = activityColor,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = when (activeActivity) {
                                "In Vehicle" -> "Vehicle Detected • Paused"
                                "Shaking/Vibrating" -> "Vibration Filtered"
                                else -> activeActivity
                            },
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = colors.textPrimary
                        )
                        Text(
                            text = "${String.format("%.0f%%", activeConfidence * 100f)} conf",
                            fontSize = 11.sp,
                            color = colors.textMuted
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }

        // Stats Cards Grid
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ShadcnStatCard(
                    title = "Distance",
                    value = String.format("%.2f km", today?.distanceKm ?: 0.0),
                    subText = "Total walked today",
                    modifier = Modifier.weight(1f),
                    icon = { Icon(Icons.Default.DirectionsWalk, null, tint = colors.primary) }
                )
                ShadcnStatCard(
                    title = "Calories",
                    value = String.format("%.0f kcal", today?.caloriesKcal ?: 0.0),
                    subText = "Burned actively",
                    modifier = Modifier.weight(1f),
                    icon = { Icon(Icons.Default.LocalFireDepartment, null, tint = Color(0xFFEF4444)) }
                )
            }
        }

        // More stats (active time and streaks)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ShadcnStatCard(
                    title = "Active Time",
                    value = "${today?.durationMinutes ?: 0} mins",
                    subText = "Duration walked",
                    modifier = Modifier.weight(1f),
                    icon = { Icon(Icons.Default.AccessTime, null, tint = colors.textMuted) }
                )
                ShadcnStatCard(
                    title = "Streak",
                    value = "$streak days",
                    subText = "Daily goals met",
                    modifier = Modifier.weight(1f),
                    icon = { Icon(Icons.Default.FlashOn, null, tint = Color(0xFFF59E0B)) }
                )
            }
        }

        // User XP Bar
        item {
            ShadcnCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Level $level XP", fontSize = 12.sp, color = colors.textMuted)
                    Text("$xp / ${level * 1000} XP", fontSize = 12.sp, color = colors.textMuted)
                }
                Spacer(modifier = Modifier.height(8.dp))
                ShadcnProgressBar(progress = xp.toFloat() / (level * 1000).toFloat())
            }
        }

        // Quick AI tips section (Computed locally)
        item {
            ShadcnCard(modifier = Modifier.fillMaxWidth(), gradient = true) {
                Text(
                    text = "AI Walking Insight",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(8.dp))
                val tip = when {
                    progress >= 1.0f -> "Amazing job! You met your goal. Walking an extra 10 minutes now will stimulate post-exercise metabolism."
                    progress >= 0.5f -> "You are halfway there! Try a brisk walk after your meal to reach your daily goal."
                    else -> "Start moving! Every step adds up. Even a 5-minute walk around the room increases heart rate and blood flow."
                }
                Text(
                    text = tip,
                    fontSize = 13.sp,
                    color = Color.White.copy(alpha = 0.85f),
                    lineHeight = 18.sp
                )
            }
        }

        // Developer Tool: Step Simulator
        item {
            ShadcnCard(modifier = Modifier.fillMaxWidth()) {
                Text("Developer Simulation Tools", fontWeight = FontWeight.Bold, color = colors.textPrimary)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Since this is an offline build, you can simulate step count increments to test the game loops, pet, garden, and challenge logic.", fontSize = 12.sp, color = colors.textMuted)
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ShadcnButton(
                        text = "+500 Steps",
                        onClick = { viewModel.simulateSteps(500) },
                        outline = true,
                        modifier = Modifier.weight(1f)
                    )
                    ShadcnButton(
                        text = "+2500 Steps",
                        onClick = { viewModel.simulateSteps(2500) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
        
        item {
            Spacer(modifier = Modifier.height(64.dp)) // Padding for bottom nav
        }
    }
}

// ==========================================
// 4. CHALLENGES SCREEN
// ==========================================
@Composable
fun ChallengesScreen(viewModel: WalkViewModel) {
    val colors = LocalShadcnColors.current
    val list by viewModel.challenges.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Daily & Weekly Challenges",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary
            )
            Text(
                text = "Complete walking tasks to earn gems and experience points.",
                fontSize = 14.sp,
                color = colors.textMuted
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        items(list) { challenge ->
            ShadcnCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = challenge.title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = colors.textPrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            ShadcnBadge(
                                text = challenge.type.name,
                                containerColor = if (challenge.type == ChallengeType.DAILY) colors.primary.copy(alpha = 0.1f) else Color(0xFF3B82F6).copy(alpha = 0.1f),
                                textColor = if (challenge.type == ChallengeType.DAILY) colors.primary else Color(0xFF3B82F6)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = challenge.description,
                            fontSize = 12.sp,
                            color = colors.textMuted
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "+${challenge.rewardXp} XP",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.primary
                        )
                        Text(
                            text = "+${challenge.rewardGems} Gems",
                            fontSize = 11.sp,
                            color = colors.textMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${String.format("%,d", challenge.currentValue)} / ${String.format("%,d", challenge.targetValue)} steps",
                        fontSize = 11.sp,
                        color = colors.textMuted
                    )
                    Text(
                        text = "${(challenge.progress * 100).toInt()}%",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                ShadcnProgressBar(progress = challenge.progress)

                if (challenge.completed) {
                    Spacer(modifier = Modifier.height(12.dp))
                    ShadcnButton(
                        text = if (challenge.claimed) "Claimed" else "Claim Rewards",
                        onClick = { viewModel.claimChallengeRewards(challenge) },
                        enabled = !challenge.claimed,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
        
        item {
            Spacer(modifier = Modifier.height(64.dp))
        }
    }
}

// ==========================================
// 5. GARDEN SCREEN
// ==========================================
@Composable
fun GardenScreen(viewModel: WalkViewModel) {
    val colors = LocalShadcnColors.current
    val plants by viewModel.gardenPlants.collectAsState()
    val gemsCount by viewModel.gems.collectAsState()

    var showPlantSelector by remember { mutableStateOf(false) }
    var selectedRow by remember { mutableStateOf(-1) }
    var selectedCol by remember { mutableStateOf(-1) }

    val rows = 3
    val cols = 3

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Walk Garden",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )
                Text(
                    text = "Seeds cost 15 gems. They grow as you walk!",
                    fontSize = 13.sp,
                    color = colors.textMuted
                )
            }
            ShadcnBadge(
                text = "💎 $gemsCount",
                containerColor = colors.surface,
                textColor = colors.textPrimary
            )
        }

        // Garden Grid
        ShadcnCard(modifier = Modifier.fillMaxWidth()) {
            Text("Garden Oasis (3x3)", fontWeight = FontWeight.Bold, color = colors.textPrimary)
            Spacer(modifier = Modifier.height(16.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                for (r in 0 until rows) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        for (c in 0 until cols) {
                            val plant = plants.firstOrNull { it.row == r && it.col == c }
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .border(1.dp, colors.border, RoundedCornerShape(8.dp))
                                    .background(colors.background)
                                    .clickable {
                                        if (plant == null) {
                                            selectedRow = r
                                            selectedCol = c
                                            showPlantSelector = true
                                        }
                                    }
                                    .padding(4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                if (plant != null) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center,
                                        modifier = Modifier.clickable {
                                            if (plant.growthProgress >= 1.0f) {
                                                viewModel.harvestPlant(plant)
                                            } else {
                                                // Water plant option
                                                viewModel.waterPlant(plant)
                                            }
                                        }
                                    ) {
                                        Icon(
                                            imageVector = when (plant.species) {
                                                "ROSE" -> Icons.Default.LocalFlorist
                                                "SUNFLOWER" -> Icons.Default.LocalFlorist
                                                "TULIP" -> Icons.Default.LocalFlorist
                                                else -> Icons.Default.LocalFlorist
                                            },
                                            contentDescription = plant.name,
                                            tint = when {
                                                plant.growthProgress >= 1.0f -> colors.primary
                                                plant.growthProgress >= 0.6f -> colors.primary.copy(alpha = 0.7f)
                                                else -> Color(0xFF8B5CF6) // violet/bud
                                            },
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = when (plant.stage) {
                                                4 -> "Bloom"
                                                3 -> "Bud"
                                                2 -> "Sprout"
                                                else -> "Seed"
                                            },
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = colors.textPrimary
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        ShadcnProgressBar(progress = plant.growthProgress, height = 4.dp)
                                    }
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Plant seed",
                                        tint = colors.border,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showPlantSelector) {
            ShadcnCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Select Seed to Plant", fontWeight = FontWeight.Bold, color = colors.textPrimary)
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        modifier = Modifier.clickable { showPlantSelector = false }
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))

                val seeds = listOf("ROSE", "SUNFLOWER", "TULIP", "BONSAI")
                seeds.forEach { species ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (gemsCount >= 15) {
                                    viewModel.plantSeed(species, selectedRow, selectedCol)
                                    showPlantSelector = false
                                }
                            }
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocalFlorist, null, tint = colors.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(species, color = colors.textPrimary)
                        }
                        Text("15 Gems", fontSize = 12.sp, color = colors.textMuted)
                    }
                    Divider(color = colors.border)
                }
            }
        }

        // Active growth instruction card
        ShadcnCard(modifier = Modifier.fillMaxWidth()) {
            Text("How it works", fontWeight = FontWeight.Bold, color = colors.textPrimary)
            Spacer(modifier = Modifier.height(8.dp))
            Text("1. Click an empty plot in your garden to buy and plant a seed.", fontSize = 12.sp, color = colors.textMuted)
            Text("2. Walk in real life! For every step you take, your plants grow.", fontSize = 12.sp, color = colors.textMuted)
            Text("3. Spend 2 gems to water your plant to instantly boost its progress.", fontSize = 12.sp, color = colors.textMuted)
            Text("4. Once a plant reaches 100% Bloom, tap it to harvest for double the gems!", fontSize = 12.sp, color = colors.textMuted)
        }
        
        Spacer(modifier = Modifier.height(64.dp))
    }
}

// ==========================================
// 6. PET SCREEN
// ==========================================
@Composable
fun PetScreen(viewModel: WalkViewModel) {
    val colors = LocalShadcnColors.current
    val pet by viewModel.petState.collectAsState()
    val gemsCount by viewModel.gems.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Virtual Pet",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )
                Text(
                    text = "Keep your pet fed and happy using gems.",
                    fontSize = 13.sp,
                    color = colors.textMuted
                )
            }
            ShadcnBadge(
                text = "💎 $gemsCount",
                containerColor = colors.surface,
                textColor = colors.textPrimary
            )
        }

        if (pet != null) {
            ShadcnCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .background(colors.primary.copy(alpha = 0.1f), CircleShape)
                            .border(2.dp, colors.primary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        // Display simple ASCII art style icon for pet
                        Icon(
                            imageVector = when (pet?.type) {
                                "CAT" -> Icons.Default.Pets
                                "DOG" -> Icons.Default.Pets
                                else -> Icons.Default.Pets
                            },
                            contentDescription = pet?.name,
                            tint = colors.primary,
                            modifier = Modifier.size(56.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = pet!!.name,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )

                    Text(
                        text = "Level ${pet!!.level} (${pet!!.type})",
                        fontSize = 14.sp,
                        color = colors.textMuted
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    ShadcnBadge(
                        text = "Mood: ${pet!!.mood}",
                        containerColor = colors.border,
                        textColor = colors.textPrimary
                    )
                }
            }

            // Stat bars for Hunger, Happiness, XP
            ShadcnCard(modifier = Modifier.fillMaxWidth()) {
                Text("Pet Vitals", fontWeight = FontWeight.Bold, color = colors.textPrimary)
                Spacer(modifier = Modifier.height(16.dp))

                // Hunger
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Fullness (Hunger)", fontSize = 12.sp, color = colors.textMuted)
                    Text("${pet!!.hunger}%", fontSize = 12.sp, color = colors.textPrimary, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(4.dp))
                ShadcnProgressBar(progress = pet!!.hunger.toFloat() / 100f)

                Spacer(modifier = Modifier.height(16.dp))

                // Happiness
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Happiness", fontSize = 12.sp, color = colors.textMuted)
                    Text("${pet!!.happiness}%", fontSize = 12.sp, color = colors.textPrimary, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(4.dp))
                ShadcnProgressBar(progress = pet!!.happiness.toFloat() / 100f)

                Spacer(modifier = Modifier.height(16.dp))

                // XP progress
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Pet Level Progress", fontSize = 12.sp, color = colors.textMuted)
                    Text("${pet!!.xp} / ${pet!!.xpNeeded} XP", fontSize = 12.sp, color = colors.textPrimary, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(4.dp))
                ShadcnProgressBar(progress = pet!!.xp.toFloat() / pet!!.xpNeeded.toFloat())
            }

            // Interactive Actions
            ShadcnCard(modifier = Modifier.fillMaxWidth()) {
                Text("Interact with ${pet!!.name}", fontWeight = FontWeight.Bold, color = colors.textPrimary)
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ShadcnButton(
                        text = "Feed (5 Gems)",
                        onClick = { viewModel.feedPet() },
                        enabled = gemsCount >= 5 && pet!!.hunger < 100,
                        modifier = Modifier.weight(1f)
                    )
                    ShadcnButton(
                        text = "Play (3 Gems)",
                        onClick = { viewModel.playWithPet() },
                        enabled = gemsCount >= 3 && pet!!.happiness < 100,
                        outline = true,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        } else {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text("No pet created yet. Please complete onboarding.", color = colors.textMuted)
            }
        }
        
        Spacer(modifier = Modifier.height(64.dp))
    }
}

// ==========================================
// 7. STATISTICS SCREEN
// ==========================================
@Composable
fun StatisticsScreen(viewModel: WalkViewModel) {
    val colors = LocalShadcnColors.current
    val history by viewModel.stepsHistory.collectAsState()

    val displayList = history.take(7).reversed()
    val chartData = displayList.map { Pair(it.date.substringAfter("-").substringAfter("-"), it.steps.toFloat()) }
    val lineData = displayList.map { it.caloriesKcal.toFloat() }
    val lineLabels = displayList.map { it.date.substringAfter("-").substringAfter("-") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Walking Statistics",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary
            )
            Text(
                text = "Detailed review of your walking performance.",
                fontSize = 14.sp,
                color = colors.textMuted
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        item {
            ShadcnCard(modifier = Modifier.fillMaxWidth()) {
                Text("Steps History (Last 7 Days)", fontWeight = FontWeight.Bold, color = colors.textPrimary)
                Spacer(modifier = Modifier.height(16.dp))
                ShadcnBarChart(
                    data = chartData,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                )
            }
        }

        item {
            ShadcnCard(modifier = Modifier.fillMaxWidth()) {
                Text("Active Calories Burned (Kcal)", fontWeight = FontWeight.Bold, color = colors.textPrimary)
                Spacer(modifier = Modifier.height(16.dp))
                ShadcnLineChart(
                    data = lineData,
                    labels = lineLabels,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                )
            }
        }

        item {
            val totalSteps = history.sumOf { it.steps }
            val avgSteps = if (history.isNotEmpty()) history.map { it.steps }.average().toInt() else 0
            val peakSteps = if (history.isNotEmpty()) history.maxOf { it.steps } else 0

            ShadcnCard(modifier = Modifier.fillMaxWidth()) {
                Text("Performance Summary", fontWeight = FontWeight.Bold, color = colors.textPrimary)
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Total Steps Logged:", color = colors.textMuted)
                    Text(String.format("%,d steps", totalSteps), color = colors.textPrimary, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Daily Average:", color = colors.textMuted)
                    Text(String.format("%,d steps", avgSteps), color = colors.textPrimary, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Peak Record:", color = colors.textMuted)
                    Text(String.format("%,d steps", peakSteps), color = colors.textPrimary, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ==========================================
// 8. ACHIEVEMENTS SCREEN
// ==========================================
@Composable
fun AchievementsScreen(viewModel: WalkViewModel) {
    val colors = LocalShadcnColors.current
    val badges by viewModel.achievements.collectAsState()

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(2) }) {
            Column {
                Text(
                    text = "Trophy Room",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )
                Text(
                    text = "Unlock special badges as you walk in WalkVerse AI.",
                    fontSize = 14.sp,
                    color = colors.textMuted
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        items(badges) { badge ->
            val unlockedColor = colors.primary
            val lockedColor = colors.border

            ShadcnCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp),
                gradient = badge.unlocked
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Icon(
                        imageVector = when (badge.iconName) {
                            "DirectionsWalk" -> Icons.Default.DirectionsWalk
                            "Terrain" -> Icons.Default.Terrain
                            "Public" -> Icons.Default.Public
                            "EmojiEvents" -> Icons.Default.EmojiEvents
                            "Pets" -> Icons.Default.Pets
                            else -> Icons.Default.LocalFlorist
                        },
                        contentDescription = badge.title,
                        tint = if (badge.unlocked) Color.White else colors.textMuted,
                        modifier = Modifier.size(36.dp)
                    )

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = badge.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = if (badge.unlocked) Color.White else colors.textPrimary,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = badge.description,
                            fontSize = 10.sp,
                            color = if (badge.unlocked) Color.White.copy(alpha = 0.8f) else colors.textMuted,
                            textAlign = TextAlign.Center,
                            lineHeight = 12.sp
                        )
                    }

                    ShadcnBadge(
                        text = if (badge.unlocked) "Unlocked" else "${badge.currentValue}/${badge.targetValue}",
                        containerColor = if (badge.unlocked) Color.White.copy(alpha = 0.2f) else colors.background,
                        textColor = if (badge.unlocked) Color.White else colors.textMuted
                    )
                }
            }
        }
        
        item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(2) }) {
            Spacer(modifier = Modifier.height(64.dp))
        }
    }
}

// ==========================================
// 9. STORY MODE SCREEN
// ==========================================
@Composable
fun StoryModeScreen(viewModel: WalkViewModel) {
    val colors = LocalShadcnColors.current
    val chapters by viewModel.storyChapters.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Story Adventures",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary
            )
            Text(
                text = "Advance through rich narratives by accumulating walking steps.",
                fontSize = 14.sp,
                color = colors.textMuted
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        items(chapters) { chapter ->
            val alpha = if (chapter.isUnlocked) 1f else 0.5f
            ShadcnCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .alpha(alpha)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = chapter.title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = colors.textPrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            if (chapter.isCompleted) {
                                Icon(Icons.Default.CheckCircle, null, tint = colors.success, modifier = Modifier.size(16.dp))
                            } else if (!chapter.isUnlocked) {
                                Icon(Icons.Default.Lock, null, tint = colors.textMuted, modifier = Modifier.size(14.dp))
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = chapter.description,
                            fontSize = 12.sp,
                            color = colors.textMuted
                        )
                    }
                }

                if (chapter.isUnlocked) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = chapter.narrative,
                        fontSize = 13.sp,
                        color = colors.textPrimary,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${String.format("%,d", chapter.stepsCompleted)} / ${String.format("%,d", chapter.targetSteps)} steps",
                            fontSize = 11.sp,
                            color = colors.textMuted
                        )
                        Text(
                            text = "${(chapter.progress * 100).toInt()}% Completed",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.primary
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    ShadcnProgressBar(progress = chapter.progress)
                }
            }
        }
        
        item {
            Spacer(modifier = Modifier.height(64.dp))
        }
    }
}

// ==========================================
// 10. HISTORY SCREEN
// ==========================================
@Composable
fun HistoryScreen(viewModel: WalkViewModel) {
    val colors = LocalShadcnColors.current
    val history by viewModel.stepsHistory.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Walking Log",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary
            )
            Text(
                text = "Review your step records and metrics for previous days.",
                fontSize = 14.sp,
                color = colors.textMuted
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (history.isEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    Text("No records found", color = colors.textMuted)
                }
            }
        } else {
            items(history) { record ->
                val goalMet = record.steps >= record.goal
                ShadcnCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            val parsedDate = LocalDate.parse(record.date)
                            val formattedDate = parsedDate.format(DateTimeFormatter.ofPattern("EEEE, MMMM dd"))
                            Text(
                                text = formattedDate,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = colors.textPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Goal: ${record.goal} steps",
                                fontSize = 11.sp,
                                color = colors.textMuted
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = String.format("%,d steps", record.steps),
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 16.sp,
                                color = if (goalMet) colors.success else colors.textPrimary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = String.format("%.2f km  •  %.0f kcal", record.distanceKm, record.caloriesKcal),
                                fontSize = 11.sp,
                                color = colors.textMuted
                            )
                        }
                    }
                }
            }
        }
        
        item {
            Spacer(modifier = Modifier.height(64.dp))
        }
    }
}

// ==========================================
// 11. PROFILE SCREEN
// ==========================================
@Composable
fun ProfileScreen(navController: NavController, viewModel: WalkViewModel) {
    val colors = LocalShadcnColors.current
    val name by viewModel.userName.collectAsState()
    val level by viewModel.userLevel.collectAsState()
    val xp by viewModel.userXp.collectAsState()
    val gemsCount by viewModel.gems.collectAsState()
    val streak by viewModel.streakCount.collectAsState()
    val history by viewModel.stepsHistory.collectAsState()

    var editingName by remember { mutableStateOf(false) }
    var nameInput by remember { mutableStateOf("") }

    var editingGoal by remember { mutableStateOf(false) }
    var goalInput by remember { mutableStateOf("") }

    val totalSteps = history.sumOf { it.steps }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "User Profile",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = colors.textPrimary
        )

        // Profile Card
        ShadcnCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(colors.primary.copy(alpha = 0.1f), CircleShape)
                        .border(1.dp, colors.primary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Person, null, tint = colors.primary, modifier = Modifier.size(32.dp))
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    if (editingName) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = nameInput,
                                onValueChange = { nameInput = it },
                                modifier = Modifier.width(120.dp),
                                textStyle = TextStyle(fontSize = 14.sp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Save Name",
                                tint = colors.success,
                                modifier = Modifier.clickable {
                                    if (nameInput.isNotEmpty()) {
                                        viewModel.updateUserName(nameInput)
                                    }
                                    editingName = false
                                }
                            )
                        }
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = name,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Name",
                                tint = colors.textMuted,
                                modifier = Modifier
                                    .size(16.dp)
                                    .clickable {
                                        nameInput = name
                                        editingName = true
                                    }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "Rank: WalkVerse Explorer", fontSize = 12.sp, color = colors.textMuted)
                }
            }
        }

        // Stats summary
        ShadcnCard(modifier = Modifier.fillMaxWidth()) {
            Text("Activity Overview", fontWeight = FontWeight.Bold, color = colors.textPrimary)
            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Total Steps", fontSize = 11.sp, color = colors.textMuted)
                    Text(String.format("%,d", totalSteps), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("Gems Balance", fontSize = 11.sp, color = colors.textMuted)
                    Text("💎 $gemsCount", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Level", fontSize = 11.sp, color = colors.textMuted)
                    Text("$level", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("Active Streak", fontSize = 11.sp, color = colors.textMuted)
                    Text("$streak days", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                }
            }
        }

        // Options List (Settings, Statistics, Achievements, Story, History)
        ShadcnCard(modifier = Modifier.fillMaxWidth()) {
            Text("Quick Menu", fontWeight = FontWeight.Bold, color = colors.textPrimary)
            Spacer(modifier = Modifier.height(8.dp))

            val menuItems = listOf(
                Pair("Statistics & Charts", Screen.Statistics.route),
                Pair("Achievements & Badges", Screen.Achievements.route),
                Pair("Walking History Log", Screen.History.route),
                Pair("Story Adventures", Screen.Story.route),
                Pair("Settings & Theme", Screen.Settings.route),
                Pair("Help FAQs", Screen.Help.route),
                Pair("About WalkVerse AI", Screen.About.route)
            )

            menuItems.forEach { (title, route) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { navController.navigate(route) }
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(title, color = colors.textPrimary, fontSize = 14.sp)
                    Icon(Icons.Default.ChevronRight, null, tint = colors.textMuted)
                }
                Divider(color = colors.border)
            }
        }
        
        Spacer(modifier = Modifier.height(64.dp))
    }
}

// ==========================================
// 12. SETTINGS SCREEN
// ==========================================
@Composable
fun SettingsScreen(viewModel: WalkViewModel) {
    val colors = LocalShadcnColors.current
    val currentTheme by viewModel.selectedTheme.collectAsState()
    val isDark by viewModel.darkModeEnabled.collectAsState()
    val hcEnabled by viewModel.healthConnectSyncEnabled.collectAsState()
    val currentGoal by viewModel.dailyGoal.collectAsState()

    var goalText by remember { mutableStateOf(currentGoal.toString()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "App Settings",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = colors.textPrimary
        )

        // Step Goal Setting
        ShadcnCard(modifier = Modifier.fillMaxWidth()) {
            Text("Goal Configuration", fontWeight = FontWeight.Bold, color = colors.textPrimary)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Adjust your target steps per day to customize challenge calculations.", fontSize = 12.sp, color = colors.textMuted)
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = goalText,
                    onValueChange = { goalText = it.filter { char -> char.isDigit() } },
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = colors.textPrimary,
                        unfocusedTextColor = colors.textPrimary,
                        focusedBorderColor = colors.primary,
                        unfocusedBorderColor = colors.border,
                        focusedPlaceholderColor = colors.textMuted,
                        unfocusedPlaceholderColor = colors.textMuted
                    )
                )
                Spacer(modifier = Modifier.width(12.dp))
                ShadcnButton(
                    text = "Save Goal",
                    onClick = {
                        val intVal = goalText.toIntOrNull()
                        if (intVal != null && intVal > 0) {
                            viewModel.updateDailyGoal(intVal)
                        }
                    }
                )
            }
        }

        // Themes Selector Setting
        ShadcnCard(modifier = Modifier.fillMaxWidth()) {
            Text("App Theme Theme", fontWeight = FontWeight.Bold, color = colors.textPrimary)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Select color schemes for premium custom styling.", fontSize = 12.sp, color = colors.textMuted)
            Spacer(modifier = Modifier.height(16.dp))

            val themes = listOf("ZINC", "ROSE", "EMERALD", "ORANGE")
            themes.forEach { themeName ->
                val isSelected = currentTheme == themeName
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.updateTheme(themeName) }
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = themeName,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = colors.textPrimary
                    )
                    if (isSelected) {
                        Icon(Icons.Default.Check, null, tint = colors.primary)
                    }
                }
                Divider(color = colors.border)
            }
        }

        // Toggles Setting
        ShadcnCard(modifier = Modifier.fillMaxWidth()) {
            Text("Preferences", fontWeight = FontWeight.Bold, color = colors.textPrimary)
            Spacer(modifier = Modifier.height(12.dp))

            // Dark Mode Switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Dark Mode", color = colors.textPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    Text("Use clean high contrast black tones", color = colors.textMuted, fontSize = 11.sp)
                }
                Switch(
                    checked = isDark,
                    onCheckedChange = { viewModel.toggleDarkMode(it) }
                )
            }

            Divider(color = colors.border, modifier = Modifier.padding(vertical = 12.dp))

            // Health Connect Sync
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Health Connect Sync", color = colors.textPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    Text("Synchronize background step counts automatically.", color = colors.textMuted, fontSize = 11.sp)
                }
                Switch(
                    checked = hcEnabled,
                    onCheckedChange = { viewModel.toggleHealthConnect(it) }
                )
            }
        }
    }
}

// ==========================================
// 13. ABOUT SCREEN
// ==========================================
@Composable
fun AboutScreen() {
    val colors = LocalShadcnColors.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "About WalkVerse AI",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = colors.textPrimary
        )

        ShadcnCard(modifier = Modifier.fillMaxWidth()) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(colors.primary.copy(alpha = 0.1f), CircleShape)
                        .border(1.dp, colors.primary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.DirectionsWalk, null, tint = colors.primary, modifier = Modifier.size(40.dp))
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text("WalkVerse AI", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                Text("Version 1.0.0", fontSize = 12.sp, color = colors.textMuted)
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text("WalkVerse AI is a next-generation offline step tracker. It transforms daily physical movements into an engaging game. Walk more in real life to grow seeds in your virtual garden, level up your cute pet buddy, progress through adventure story chapters, and unlock trophies.", fontSize = 13.sp, color = colors.textPrimary, lineHeight = 18.sp)
        }

        ShadcnCard(modifier = Modifier.fillMaxWidth()) {
            Text("Architecture & Stack", fontWeight = FontWeight.Bold, color = colors.textPrimary)
            Spacer(modifier = Modifier.height(8.dp))
            Text("• UI: Jetpack Compose with Material 3 & custom shadcn-inspired components.", fontSize = 12.sp, color = colors.textMuted)
            Text("• DB: Room database for robust offline storage.", fontSize = 12.sp, color = colors.textMuted)
            Text("• Settings: Preferences DataStore API.", fontSize = 12.sp, color = colors.textMuted)
            Text("• Architecture: MVVM with Clean Architecture pattern.", fontSize = 12.sp, color = colors.textMuted)
            Text("• Background: Periodic WorkManager triggers.", fontSize = 12.sp, color = colors.textMuted)
            Text("• Health Sync: Google Health Connect APIs.", fontSize = 12.sp, color = colors.textMuted)
        }

        ShadcnCard(modifier = Modifier.fillMaxWidth()) {
            Text("Privacy Policy", fontWeight = FontWeight.Bold, color = colors.textPrimary)
            Spacer(modifier = Modifier.height(8.dp))
            Text("This app has no remote connections. All your steps data, pet configurations, and profile records are stored completely locally on your device's filesystem. There is no telemetry, tracking, or online sync.", fontSize = 12.sp, color = colors.textMuted, lineHeight = 16.sp)
        }
    }
}

// ==========================================
// 14. HELP SCREEN
// ==========================================
@Composable
fun HelpScreen() {
    val colors = LocalShadcnColors.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Help & FAQs",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = colors.textPrimary
        )

        val faqs = listOf(
            Pair(
                "How do I track steps?",
                "By default, the app automatically tracks steps in the background. If Health Connect sync is enabled, it pulls official records. You can also simulate walks using the simulation tools on the Home screen."
            ),
            Pair(
                "What are gems used for?",
                "Gems are the in-game currency. Spend gems to buy seeds (15 gems each) or water your plants (2 gems). You can also spend gems to feed your pet (5 gems) or play with it (3 gems)."
            ),
            Pair(
                "How do I level up my pet?",
                "Your pet levels up by receiving experience points (XP). You earn pet XP by walking in real life (1 XP for every 5 steps) and by feeding or playing with your pet."
            ),
            Pair(
                "How does the garden grow?",
                "After planting a seed, it grows progressively as you walk (fully grown in 10,000 steps). When it is at 100% Bloom, tap it to harvest and claim massive gem rewards!"
            ),
            Pair(
                "Is there any online account sync?",
                "No. WalkVerse AI is designed to be 100% private and offline. We do not use any servers or cloud databases. Backups should be managed locally using Android's native backup utilities."
            )
        )

        faqs.forEach { (question, answer) ->
            ShadcnCard(modifier = Modifier.fillMaxWidth()) {
                Text(text = question, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = colors.textPrimary)
                Spacer(modifier = Modifier.height(6.dp))
                Text(text = answer, fontSize = 12.sp, color = colors.textMuted, lineHeight = 16.sp)
            }
        }
    }
}
