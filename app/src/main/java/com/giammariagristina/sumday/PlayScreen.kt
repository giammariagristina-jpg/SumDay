package com.giammariagristina.sumday

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
fun PlayScreen(onBack: () -> Unit, level: DifficultyLevel) {
    val context = LocalContext.current
    var gameState by remember(level) { mutableStateOf(newGameState(generatePuzzleFor(level.id))) }
    var elapsedSeconds by remember { mutableStateOf(0) }
    val parSeconds = remember(level) { parSecondsFor(level.id) }

    LaunchedEffect(gameState.puzzle) {
        SoundPlayer.playNewPuzzle(context)
        elapsedSeconds = 0
        while (!gameState.isSolved) {
            delay(1000)
            elapsedSeconds++
        }
    }

    LaunchedEffect(gameState.isSolved) {
        if (gameState.isSolved) {
            SoundPlayer.playSolved(context)
            LeaderboardStore.recordResult(context, level.id, elapsedSeconds)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        SimpleTopBar(
            title = stringResource(level.nameRes),
            onBack = onBack,
            trailing = { Text(text = formatTime(elapsedSeconds), style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onBackground) }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            StatusLine(gameState = gameState, elapsedSeconds = elapsedSeconds, parSeconds = parSeconds)

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

            if (!gameState.isSolved) {
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
            } else {
                TextButton(onClick = { gameState = newGameState(generatePuzzleFor(level.id)) }) {
                    Text(stringResource(R.string.new_puzzle))
                }
            }
        }
    }
}

@Composable
private fun StatusLine(gameState: MagicSquareGameState, elapsedSeconds: Int, parSeconds: Int) {
    if (gameState.isSolved) {
        val diff = elapsedSeconds - parSeconds
        val detail = if (diff <= 0) stringResource(R.string.solved_under_par, formatTime(elapsedSeconds)) else formatTime(elapsedSeconds)
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = stringResource(R.string.solved), style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.colorScheme.primary)
            Text(
                text = detail,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
        return
    }

    val text: String
    val color: androidx.compose.ui.graphics.Color
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
fun MagicSquareGrid(gameState: MagicSquareGameState, onCellClick: (Int, Int) -> Unit) {
    val wrong = gameState.wrongCells()
    val n = gameState.puzzle.size
    val boxSize = when {
        n <= 3 -> 240.dp
        n == 4 -> 280.dp
        else -> 320.dp
    }
    val cellTextStyle = if (n >= 5) MaterialTheme.typography.bodyLarge else MaterialTheme.typography.titleLarge
    Column(
        modifier = Modifier
            .size(boxSize)
            .aspectRatio(1f),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        for (r in 0 until n) {
            Row(
                modifier = Modifier.fillMaxWidth().weight(1f),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                for (c in 0 until n) {
                    MagicSquareCell(
                        value = gameState.placed[r][c],
                        isGiven = gameState.puzzle.given[r][c],
                        isWrong = (r to c) in wrong,
                        textStyle = cellTextStyle,
                        modifier = Modifier.weight(1f).fillMaxSize(),
                        onClick = { onCellClick(r, c) }
                    )
                }
            }
        }
    }
}

@Composable
fun MagicSquareCell(
    value: Int?,
    isGiven: Boolean,
    isWrong: Boolean,
    textStyle: androidx.compose.ui.text.TextStyle,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val background = when {
        isGiven -> MaterialTheme.colorScheme.surfaceVariant
        value != null -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.surface
    }
    val textColor = when {
        isGiven -> MaterialTheme.colorScheme.onSurfaceVariant
        value != null -> MaterialTheme.colorScheme.onPrimary
        else -> MaterialTheme.colorScheme.primary
    }
    val borderColor = when {
        isWrong -> MaterialTheme.colorScheme.tertiary
        value == null && !isGiven -> MaterialTheme.colorScheme.outline
        else -> androidx.compose.ui.graphics.Color.Transparent
    }
    Box(
        modifier = modifier
            .clip(MaterialTheme.shapes.medium)
            .background(background)
            .border(width = 1.5.dp, color = borderColor, shape = MaterialTheme.shapes.medium)
            .clickable(enabled = !isGiven, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(text = value?.toString().orEmpty(), style = textStyle, color = textColor)
    }
}

@Composable
fun NumberBank(bank: List<Int>, selectedIndex: Int?, onSelect: (Int) -> Unit) {
    val hasBigNumbers = bank.any { it >= 100 }
    val chipSize = if (hasBigNumbers) 56.dp else 48.dp
    val chipTextStyle = if (hasBigNumbers) MaterialTheme.typography.bodyLarge else MaterialTheme.typography.titleLarge
    val rows = bank.withIndex().chunked(5)
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        rows.forEach { rowItems ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                rowItems.forEach { (index, number) ->
                    val selected = index == selectedIndex
                    Box(
                        modifier = Modifier
                            .size(chipSize)
                            .clip(MaterialTheme.shapes.medium)
                            .background(if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                            .border(
                                width = if (selected) 2.dp else 0.dp,
                                color = MaterialTheme.colorScheme.primary,
                                shape = MaterialTheme.shapes.medium
                            )
                            .clickable { onSelect(index) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = number.toString(),
                            style = chipTextStyle,
                            color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
