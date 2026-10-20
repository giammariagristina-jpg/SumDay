package com.giammariagristina.sumday

import android.content.Context
import androidx.core.content.edit
import java.util.UUID

// Same pattern as SkyShapes: SharedPreferences-backed nickname + a locally
// generated player id, no uniqueness check, mandatory on first launch.
object PlayerPrefs {
    private const val PREFS_NAME = "SumdayPrefs"
    private const val KEY_NICKNAME = "nickname"
    private const val KEY_PLAYER_ID = "player_id"
    private const val KEY_SOUND_ENABLED = "sound_enabled"

    fun getNickname(context: Context): String =
        prefs(context).getString(KEY_NICKNAME, null) ?: ""

    fun getPlayerId(context: Context): String =
        prefs(context).getString(KEY_PLAYER_ID, null) ?: ""

    fun saveNickname(context: Context, nickname: String) {
        val playerId = getPlayerId(context).ifBlank { UUID.randomUUID().toString() }
        prefs(context).edit {
            putString(KEY_NICKNAME, nickname)
            putString(KEY_PLAYER_ID, playerId)
        }
    }

    fun isSoundEnabled(context: Context): Boolean =
        prefs(context).getBoolean(KEY_SOUND_ENABLED, true)

    fun setSoundEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit { putBoolean(KEY_SOUND_ENABLED, enabled) }
    }

    // ── Elimina profilo (20/10/2026, strada A) ──
    // I documenti in classifica sono "sumday_daily_<data>_<player_id>": l'id non e' un campo del
    // documento, quindi si tiene qui l'elenco delle date per cui e' stato inviato un risultato.
    private const val KEY_SENT_DATES = "daily_quiz_sent_dates"

    fun addSentDailyDate(context: Context, dateStr: String) {
        val dates = prefs(context).getStringSet(KEY_SENT_DATES, emptySet()).orEmpty() + dateStr
        prefs(context).edit { putStringSet(KEY_SENT_DATES, dates) }
    }

    /** Date inviate, piu' l'ultima data del quiz salvata da DailyQuizPrefs (anche se precedente a questa versione). */
    fun sentDailyDates(context: Context): Set<String> {
        val p = prefs(context)
        val dates = p.getStringSet(KEY_SENT_DATES, emptySet()).orEmpty()
        val lastQuizDate = p.getString("daily_quiz_date", null)   // DailyQuizPrefs.KEY_DATE (stesso file SumdayPrefs)
        return if (lastQuizDate != null) dates + lastQuizDate else dates
    }

    /**
     * Cancella il profilo sul telefono: nickname, id, date inviate, risultato del quiz del giorno e
     * migliori tempi personali (LeaderboardStore, chiavi "best_*"). Restano le impostazioni (suoni),
     * come in SkipMaze (cancellaProfiloLocale).
     */
    fun clearLocalProfile(context: Context) {
        val p = prefs(context)
        p.edit {
            remove(KEY_NICKNAME)
            remove(KEY_PLAYER_ID)
            remove(KEY_SENT_DATES)
            remove("daily_quiz_date")      // DailyQuizPrefs.KEY_DATE
            remove("daily_quiz_seconds")   // DailyQuizPrefs.KEY_SECONDS
            p.all.keys.filter { it.startsWith("best_") }.forEach { remove(it) }   // LeaderboardStore
        }
    }

    private fun prefs(context: Context) = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
}
