package com.abutorab.teacher.hub.util

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ThemePreference private constructor(context: Context) {
    private val prefs: SharedPreferences = context.applicationContext.getSharedPreferences("theme_prefs", Context.MODE_PRIVATE)

    private val _themeFlow = MutableStateFlow(getTheme())
    val themeFlow: StateFlow<String> = _themeFlow.asStateFlow()

    fun getTheme(): String {
        return prefs.getString("dark_mode_override", "system") ?: "system"
    }

    fun setTheme(theme: String) {
        prefs.edit().putString("dark_mode_override", theme).apply()
        _themeFlow.value = theme
    }

    companion object {
        @Volatile
        private var INSTANCE: ThemePreference? = null

        fun getInstance(context: Context): ThemePreference {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ThemePreference(context).also { INSTANCE = it }
            }
        }
    }
}
