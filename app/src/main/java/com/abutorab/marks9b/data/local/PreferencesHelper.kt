package com.abutorab.marks9b.data.local

import android.content.Context
import android.content.SharedPreferences

object PreferencesHelper {
    private const val PREFS_NAME = "marks9b_prefs"
    private const val KEY_LAST_OPENED_TERM = "last_opened_term"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun saveLastOpenedTerm(context: Context, termId: Int) {
        getPrefs(context).edit().putInt(KEY_LAST_OPENED_TERM, termId).apply()
    }

    fun getLastOpenedTerm(context: Context): Int? {
        val prefs = getPrefs(context)
        return if (prefs.contains(KEY_LAST_OPENED_TERM)) {
            prefs.getInt(KEY_LAST_OPENED_TERM, -1)
        } else {
            null
        }
    }
}
