package com.giammariagristina.sumday

import android.content.Context
import androidx.core.content.edit

// Personal-only leaderboard: keeps the 2 fastest times per difficulty level,
// stored locally. No cross-player comparison, so puzzle randomness never
// makes it unfair. Keyed on the stable DifficultyId (not the display name),
// so results stay valid regardless of the UI language.
object LeaderboardStore {
    private const val PREFS_NAME = "SumdayPrefs"
    private const val MAX_PLAUSIBLE_SECONDS = 24 * 60 * 60 // defensive filter against corrupted/placeholder data

    fun recordResult(context: Context, id: DifficultyId, seconds: Int) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val top2 = (bestTimes(context, id) + seconds).sorted().take(2)
        prefs.edit {
            putInt(key(id, 1), top2[0])
            if (top2.size > 1) putInt(key(id, 2), top2[1]) else remove(key(id, 2))
        }
    }

    fun bestTimes(context: Context, id: DifficultyId): List<Int> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return listOf(1, 2).mapNotNull { rank ->
            val recordKey = key(id, rank)
            if (!prefs.contains(recordKey)) return@mapNotNull null
            val value = prefs.getInt(recordKey, -1)
            value.takeIf { it in 0..MAX_PLAUSIBLE_SECONDS }
        }
    }

    private fun key(id: DifficultyId, rank: Int) = "best_${id.name}_$rank"
}
