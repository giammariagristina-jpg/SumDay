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

    private fun prefs(context: Context) = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
}
