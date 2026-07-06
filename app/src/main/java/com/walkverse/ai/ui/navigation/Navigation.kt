package com.walkverse.ai.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector? = null) {
    object Splash : Screen("splash", "Splash")
    object Onboarding : Screen("onboarding", "Onboarding")
    
    // Bottom Nav Items
    object Home : Screen("home", "Home", Icons.Default.Home)
    object Challenges : Screen("challenges", "Challenges", Icons.Default.EmojiEvents)
    object Garden : Screen("garden", "Garden", Icons.Default.LocalFlorist)
    object Pet : Screen("pet", "Virtual Pet", Icons.Default.Pets)
    
    // Side/Secondary screens
    object Profile : Screen("profile", "Profile", Icons.Default.Person)
    object Statistics : Screen("statistics", "Statistics", Icons.Default.BarChart)
    object History : Screen("history", "History", Icons.Default.History)
    object Achievements : Screen("achievements", "Badges", Icons.Default.MilitaryTech)
    object Story : Screen("story", "Story Mode", Icons.Default.AutoStories)
    object Settings : Screen("settings", "Settings", Icons.Default.Settings)
    object About : Screen("about", "About", Icons.Default.Info)
    object Help : Screen("help", "Help", Icons.Default.Help)
}

val bottomNavItems = listOf(
    Screen.Home,
    Screen.Challenges,
    Screen.Garden,
    Screen.Pet,
    Screen.Profile
)
