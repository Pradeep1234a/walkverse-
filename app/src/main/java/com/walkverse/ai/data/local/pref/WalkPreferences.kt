package com.walkverse.ai.data.local.pref

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "walkverse_settings")

class WalkPreferences(private val context: Context) {

    companion object {
        val KEY_USER_NAME = stringPreferencesKey("user_name")
        val KEY_DAILY_GOAL = intPreferencesKey("daily_goal")
        val KEY_THEME = stringPreferencesKey("selected_theme")
        val KEY_DARK_MODE = booleanPreferencesKey("dark_mode_enabled")
        val KEY_ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val KEY_HEALTH_CONNECT_ENABLED = booleanPreferencesKey("health_connect_enabled")
        val KEY_STREAK_COUNT = intPreferencesKey("streak_count")
        val KEY_LAST_ACTIVE_DATE = stringPreferencesKey("last_active_date")
        val KEY_USER_XP = intPreferencesKey("user_xp")
        val KEY_USER_LEVEL = intPreferencesKey("user_level")
        val KEY_GEMS = intPreferencesKey("user_gems")
    }

    val userNameFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[KEY_USER_NAME] ?: "Walker"
    }

    val dailyGoalFlow: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[KEY_DAILY_GOAL] ?: 8000
    }

    val themeFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[KEY_THEME] ?: "ZINC"
    }

    val darkModeFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_DARK_MODE] ?: true // default to dark mode for rich aesthetics
    }

    val onboardingCompletedFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_ONBOARDING_COMPLETED] ?: false
    }

    val healthConnectEnabledFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_HEALTH_CONNECT_ENABLED] ?: false
    }

    val streakCountFlow: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[KEY_STREAK_COUNT] ?: 0
    }

    val lastActiveDateFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[KEY_LAST_ACTIVE_DATE] ?: ""
    }

    val userXpFlow: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[KEY_USER_XP] ?: 0
    }

    val userLevelFlow: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[KEY_USER_LEVEL] ?: 1
    }

    val gemsFlow: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[KEY_GEMS] ?: 20 // start with 20 gems
    }

    suspend fun saveUserName(name: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_USER_NAME] = name
        }
    }

    suspend fun saveDailyGoal(goal: Int) {
        context.dataStore.edit { preferences ->
            preferences[KEY_DAILY_GOAL] = goal
        }
    }

    suspend fun saveTheme(theme: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_THEME] = theme
        }
    }

    suspend fun saveDarkMode(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_DARK_MODE] = enabled
        }
    }

    suspend fun saveOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_ONBOARDING_COMPLETED] = completed
        }
    }

    suspend fun saveHealthConnectEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_HEALTH_CONNECT_ENABLED] = enabled
        }
    }

    suspend fun saveStreakCount(streak: Int) {
        context.dataStore.edit { preferences ->
            preferences[KEY_STREAK_COUNT] = streak
        }
    }

    suspend fun saveLastActiveDate(date: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_LAST_ACTIVE_DATE] = date
        }
    }

    suspend fun saveUserXp(xp: Int) {
        context.dataStore.edit { preferences ->
            preferences[KEY_USER_XP] = xp
        }
    }

    suspend fun saveUserLevel(level: Int) {
        context.dataStore.edit { preferences ->
            preferences[KEY_USER_LEVEL] = level
        }
    }

    suspend fun saveGems(gems: Int) {
        context.dataStore.edit { preferences ->
            preferences[KEY_GEMS] = gems
        }
    }
}
