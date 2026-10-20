package com.giammariagristina.sumday

import android.content.Context
import android.media.MediaPlayer

object SoundPlayer {
    // Rotation-ready: add up to 4 intro sounds here (drop the files in res/raw and
    // list them below) and a random one plays each time a new puzzle appears.
    private val NEW_PUZZLE_SOUNDS = listOf(R.raw.trumpet, R.raw.intro1, R.raw.intro2, R.raw.intro3)

    fun playNewPuzzle(context: Context) = play(context, NEW_PUZZLE_SOUNDS.random())
    fun playSolved(context: Context) = play(context, R.raw.solved)

    private fun play(context: Context, resId: Int) {
        if (!PlayerPrefs.isSoundEnabled(context)) return
        val player = MediaPlayer.create(context, resId) ?: return
        player.setOnCompletionListener { it.release() }
        player.start()
    }
}
