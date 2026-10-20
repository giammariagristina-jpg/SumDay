package com.giammariagristina.sumday

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
fun DailyQuizScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val dateStr = remember { todayDateString() }
    var gameState by remember { mutableStateOf(newGameState(generateDailyQuizPuzzle(dateStr))) }
    var elapsedSeconds by remember { mutableStateOf(0) }
    var alreadyPlayedSeconds by remember { mutableStateOf(DailyQuizPrefs.getTodayResult(context)) }
    var percentFaster by remember { mutableStateOf<Int?>(null) }
    var totalPlayers by remember { mutableStateOf<Int?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        if (alreadyPlayedSeconds == null) SoundPlayer.playNewPuzzle(context)
        while (!gameState.isSolved) {
            delay(1000)
            if (alreadyPlayedSeconds == null) elapsedSeconds++
        }
    }

    LaunchedEffect(gameState.isSolved) {
        if (gameState.isSolved && alreadyPlayedSeconds == null) {
            SoundPlayer.playSolved(context)
            DailyQuizPrefs.saveTodayResult(context, elapsedSeconds)
            alreadyPlayedSeconds = elapsedSeconds
        }
        val playedSeconds = alreadyPlayedSeconds ?: return@LaunchedEffect
        try {
            val nickname = PlayerPrefs.getNickname(context)
            val playerId = PlayerPrefs.getPlayerId(context)
            DailyLeaderboard.submitResult(nickname, playerId, dateStr, playedSeconds)
            val times = DailyLeaderboard.fetchTodayTimes(dateStr)
            totalPlayers = times.size
            percentFaster = if (times.size <= 1) 100 else (times.count { it > playedSeconds } * 100 / times.size)
        } catch (e: Exception) {
            errorMessage = context.getString(R.string.daily_quiz_error)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        SimpleTopBar(
            title = stringResource(R.string.daily_quiz_title),
            onBack = onBack,
            trailing = {
                if (alreadyPlayedSeconds == null) {
                    Text(text = formatTime(elapsedSeconds), style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onBackground)
                }
            }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            val playedSeconds = alreadyPlayedSeconds
            if (playedSeconds != null) {
                DailyQuizSummary(
                    seconds = playedSeconds,
                    percentFaster = percentFaster,
                    totalPlayers = totalPlayers,
                    errorMessage = errorMessage
                )
            } else {
                DailyQuizPromptLine(gameState = gameState)

                Box(modifier = Modifier.padding(vertical = 20.dp)) {
                    MagicSquareGrid(
                        gameState = gameState,
                        onCellClick = { r, c ->
                            gameState = if (gameState.placed[r][c] != null) {
                                gameState.clearCell(r, c)
                            } else {
                                gameState.placeSelectedAt(r, c)
                            }
                        }
                    )
                }

                Text(
                    text = stringResource(R.string.numbers_left_to_place),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                NumberBank(
                    bank = gameState.bank,
                    selectedIndex = gameState.selectedBankIndex,
                    onSelect = { index -> gameState = gameState.selectBankIndex(index) }
                )
            }
        }
    }
}

@Composable
private fun DailyQuizPromptLine(gameState: MagicSquareGameState) {
    val text: String
    val color: Color
    if (gameState.wrongCells().isNotEmpty()) {
        text = stringResource(R.string.swap_hint)
        color = MaterialTheme.colorScheme.primary
    } else {
        text = stringResource(R.string.target_sum, gameState.puzzle.targetSum)
        color = MaterialTheme.colorScheme.onBackground
    }
    Text(text = text, style = MaterialTheme.typography.bodyLarge, color = color)
}

@Composable
private fun DailyQuizSummary(seconds: Int, percentFaster: Int?, totalPlayers: Int?, errorMessage: String?) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = stringResource(R.string.solved), style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.colorScheme.primary)
        Text(
            text = formatTime(seconds),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
        )
        val rankText = when {
            errorMessage != null -> errorMessage
            percentFaster == null -> stringResource(R.string.daily_quiz_checking)
            (totalPlayers ?: 0) <= 1 -> stringResource(R.string.daily_quiz_first)
            else -> stringResource(R.string.daily_quiz_percent_faster, percentFaster)
        }
        Text(
            text = rankText,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = stringResource(R.string.daily_quiz_come_back),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 24.dp)
        )
    }
}
