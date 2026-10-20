package com.giammariagristina.sumday

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

@Composable
fun LeaderboardScreen(onBack: () -> Unit) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        SimpleTopBar(title = stringResource(R.string.leaderboard_title), onBack = onBack)

        Text(
            text = stringResource(R.string.leaderboard_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
        )

        Column(modifier = Modifier.padding(horizontal = 24.dp)) {
            difficultyLevels.forEach { level ->
                val times = LeaderboardStore.bestTimes(context, level.id)
                Text(
                    text = stringResource(level.nameRes),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(top = 20.dp, bottom = 4.dp)
                )
                if (times.isEmpty()) {
                    Text(
                        text = stringResource(R.string.leaderboard_no_times),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    times.forEachIndexed { index, seconds ->
                        Text(
                            text = stringResource(R.string.leaderboard_time_row, index + 1, formatTime(seconds)),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            }
        }
    }
}
