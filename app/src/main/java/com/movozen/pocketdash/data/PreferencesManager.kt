package com.movozen.pocketdash.data

import android.content.Context
import android.content.SharedPreferences

class PreferencesManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var rollNumber: String
        get() = prefs.getString(KEY_ROLL_NUMBER, "") ?: ""
        set(value) {
            val sanitized = sanitizeRollNumber(value)
            prefs.edit().putString(KEY_ROLL_NUMBER, sanitized).apply()
        }

    fun sanitizeRollNumber(input: String): String {
        return input.uppercase().trim().replace(" ", "")
    }

    fun isValidRollNumber(input: String): Boolean {
        val sanitized = sanitizeRollNumber(input)
        return sanitized.isNotEmpty() && !sanitized.contains(" ")
    }

    companion object {
        private const val PREFS_NAME = "pocketdash_prefs"
        private const val KEY_ROLL_NUMBER = "roll_number"
    }
}
