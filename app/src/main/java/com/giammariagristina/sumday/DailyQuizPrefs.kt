package com.giammariagristina.sumday

import android.content.Context
import androidx.core.content.edit
import java.time.LocalDate

fun todayDateString(): String = LocalDate.now().toString() // ISO yyyy-MM-dd, stable across the whole day

object DailyQuizPrefs {
    private const val PREFS_NAME = "SumdayPrefs"
    private const val KEY_DATE = "daily_quiz_date"
    private const val KEY_SECONDS = "daily_quiz_seconds"

    // Non-null only if the stored result is for today; a previous day's result
    // is treated as stale so a fresh quiz is offered.
    fun getTodayResult(context: Context): Int? {
        val prefs = prefs(context)
        val savedDate = prefs.getString(KEY_DATE, null)
        if (savedDate != todayDateString()) return null
        return prefs.getInt(KEY_SECONDS, -1).takeIf { it >= 0 }
    }

    fun saveTodayResult(context: Context, seconds: Int) {
        prefs(context).edit {
            putString(KEY_DATE, todayDateString())
            putInt(KEY_SECONDS, seconds)
        }
    }

    private fun prefs(context: Context) = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
}
