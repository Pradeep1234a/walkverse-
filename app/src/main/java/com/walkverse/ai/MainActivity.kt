package com.walkverse.ai

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import androidx.work.*
import com.walkverse.ai.data.health.HealthConnectManager
import com.walkverse.ai.data.worker.StepTrackerWorker
import com.walkverse.ai.ui.WalkViewModel
import com.walkverse.ai.ui.navigation.Screen
import com.walkverse.ai.ui.navigation.bottomNavItems
import com.walkverse.ai.ui.screens.*
import com.walkverse.ai.ui.theme.LocalShadcnColors
import com.walkverse.ai.ui.theme.WalkVerseTheme
import java.time.LocalDate
import java.time.LocalTime
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {

    private lateinit var viewModel: WalkViewModel
    private lateinit var healthConnectManager: HealthConnectManager

    // Permissions launcher for sensor tracking and notifications
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val activityRecognitionGranted = permissions[android.Manifest.permission.ACTIVITY_RECOGNITION] ?: false
        if (activityRecognitionGranted) {
            startStepCounterService()
        }
    }

    // Permission launcher for Health Connect
    private val requestPermissionActivityContract by lazy {
        androidx.health.connect.client.PermissionController.createRequestPermissionResultContract()
    }
    
    private val requestPermissions = registerForActivityResult(requestPermissionActivityContract) { granted ->
        if (granted.containsAll(healthConnectManager.requiredPermissions)) {
            viewModel.toggleHealthConnect(true)
        } else {
            viewModel.toggleHealthConnect(false)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        viewModel = ViewModelProvider(this)[WalkViewModel::class.java]
        healthConnectManager = HealthConnectManager(this)

        // Schedule periodic background step synchronization using WorkManager (runs every 15 minutes)
        schedulePeriodicStepSync()

        // Request ACTIVITY_RECOGNITION and POST_NOTIFICATIONS permissions on startup
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            requestPermissionLauncher.launch(
                arrayOf(
                    android.Manifest.permission.ACTIVITY_RECOGNITION,
                    android.Manifest.permission.POST_NOTIFICATIONS
                )
            )
        } else {
            startStepCounterService()
        }

        setContent {
            val themeName by viewModel.selectedTheme.collectAsState()
            val darkMode by viewModel.darkModeEnabled.collectAsState()
            val hcSyncEnabled by viewModel.healthConnectSyncEnabled.collectAsState()

            // Trigger permission check if Health Connect sync was toggled in settings
            LaunchedEffect(hcSyncEnabled) {
                if (hcSyncEnabled && !healthConnectManager.hasAllPermissions()) {
                    requestPermissions.launch(healthConnectManager.requiredPermissions)
                }
            }

            WalkVerseTheme(themeName = themeName, darkTheme = darkMode) {
                val colors = LocalShadcnColors.current
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                // Determine whether to display the top bar and bottom nav based on active route
                val showBars = currentRoute != Screen.Splash.route && currentRoute != Screen.Onboarding.route

                Scaffold(
                    topBar = {
                        if (showBars) {
                            WalkVerseTopBar(navController, currentRoute, viewModel)
                        }
                    },
                    bottomBar = {
                        if (showBars) {
                            WalkVerseBottomNavigation(navController, currentRoute)
                        }
                    },
                    containerColor = colors.background,
                    contentColor = colors.textPrimary
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        NavHost(
                            navController = navController,
                            startDestination = Screen.Splash.route
                        ) {
                            composable(Screen.Splash.route) { SplashScreen(navController, viewModel) }
                            composable(Screen.Onboarding.route) { OnboardingScreen(navController, viewModel) }
                            composable(Screen.Home.route) { HomeScreen(navController, viewModel) }
                            composable(Screen.Challenges.route) { ChallengesScreen(viewModel) }
                            composable(Screen.Garden.route) { GardenScreen(viewModel) }
                            composable(Screen.Pet.route) { PetScreen(viewModel) }
                            composable(Screen.Profile.route) { ProfileScreen(navController, viewModel) }
                            
                            // Secondary screens
                            composable(Screen.Statistics.route) { StatisticsScreen(viewModel) }
                            composable(Screen.History.route) { HistoryScreen(viewModel) }
                            composable(Screen.Achievements.route) { AchievementsScreen(viewModel) }
                            composable(Screen.Story.route) { StoryModeScreen(viewModel) }
                            composable(Screen.Settings.route) { SettingsScreen(viewModel) }
                            composable(Screen.About.route) { AboutScreen() }
                            composable(Screen.Help.route) { HelpScreen() }
                        }
                    }
                }
            }
        }
    }

    private fun startStepCounterService() {
        val serviceIntent = Intent(this, com.walkverse.ai.data.sensor.StepDetectorService::class.java)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }
    }

    private fun schedulePeriodicStepSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .build()

        val syncRequest = PeriodicWorkRequestBuilder<StepTrackerWorker>(15, TimeUnit.MINUTES)
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
            "WalkVerseStepSync",
            ExistingPeriodicWorkPolicy.KEEP,
            syncRequest
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalkVerseTopBar(
    navController: NavHostController,
    currentRoute: String?,
    viewModel: WalkViewModel
) {
    val colors = LocalShadcnColors.current
    val isPrimaryScreen = bottomNavItems.any { it.route == currentRoute }

    val titleText = when (currentRoute) {
        Screen.Home.route -> "Home"
        Screen.Challenges.route -> "Challenges"
        Screen.Garden.route -> "Garden"
        Screen.Pet.route -> "Virtual Pet"
        Screen.Profile.route -> "Profile"
        Screen.Statistics.route -> "Statistics"
        Screen.History.route -> "History"
        Screen.Achievements.route -> "Badges"
        Screen.Story.route -> "Story Mode"
        Screen.Settings.route -> "Settings"
        Screen.About.route -> "About"
        Screen.Help.route -> "Help FAQs"
        else -> "WalkVerse"
    }

    TopAppBar(
        title = {
            Text(
                text = titleText,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary
            )
        },
        navigationIcon = {
            if (!isPrimaryScreen) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = colors.textPrimary
                    )
                }
            }
        },
        actions = {
            if (isPrimaryScreen) {
                IconButton(onClick = { navController.navigate(Screen.Settings.route) }) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = colors.textPrimary
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = colors.background
        )
    )
}

@Composable
fun WalkVerseBottomNavigation(
    navController: NavHostController,
    currentRoute: String?
) {
    val colors = LocalShadcnColors.current

    NavigationBar(
        containerColor = colors.surface,
        tonalElevation = 0.dp,
        modifier = Modifier.height(72.dp)
    ) {
        bottomNavItems.forEach { screen ->
            val isSelected = currentRoute == screen.route
            NavigationBarItem(
                selected = isSelected,
                onClick = {
                    if (currentRoute != screen.route) {
                        navController.navigate(screen.route) {
                            popUpTo(Screen.Home.route) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = {
                    Icon(
                        imageVector = screen.icon ?: Icons.Default.Help,
                        contentDescription = screen.title,
                        modifier = Modifier.size(22.dp)
                    )
                },
                label = {
                    Text(
                        text = screen.title,
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = if (colors.primary == Color.White) Color.Black else Color.White,
                    selectedTextColor = colors.primary,
                    indicatorColor = colors.primary,
                    unselectedIconColor = colors.textMuted,
                    unselectedTextColor = colors.textMuted
                )
            )
        }
    }
}
