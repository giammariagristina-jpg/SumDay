package com.giammariagristina.sumday

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

// Stable, locale-independent identifier: used for puzzle generation and
// leaderboard storage keys. The displayed name/descriptor are localized
// separately via string resources, so translating them can never break logic.
enum class DifficultyId { MATH_SOS, HOMEWORK_HERO, IVE_GOT_THIS, GURU }

data class DifficultyLevel(val id: DifficultyId, val nameRes: Int, val descriptorRes: Int)

val difficultyLevels = listOf(
    DifficultyLevel(DifficultyId.MATH_SOS, R.string.level_math_sos_name, R.string.level_math_sos_descriptor),
    DifficultyLevel(DifficultyId.HOMEWORK_HERO, R.string.level_homework_hero_name, R.string.level_homework_hero_descriptor),
    DifficultyLevel(DifficultyId.IVE_GOT_THIS, R.string.level_ive_got_this_name, R.string.level_ive_got_this_descriptor),
    DifficultyLevel(DifficultyId.GURU, R.string.level_guru_name, R.string.level_guru_descriptor)
)

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    selectedLevel: DifficultyLevel,
    onLevelSelected: (DifficultyLevel) -> Unit
) {
    val context = LocalContext.current
    var soundsOn by remember { mutableStateOf(PlayerPrefs.isSoundEnabled(context)) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        SimpleTopBar(title = stringResource(R.string.settings_title), onBack = onBack)

        Column(modifier = Modifier.padding(horizontal = 24.dp)) {
            SettingsToggleRow(
                label = stringResource(R.string.settings_sounds),
                checked = soundsOn,
                onCheckedChange = {
                    soundsOn = it
                    PlayerPrefs.setSoundEnabled(context, it)
                }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 20.dp), color = MaterialTheme.colorScheme.outline)

            Text(
                text = stringResource(R.string.settings_difficulty),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Column(modifier = Modifier.selectableGroup()) {
                difficultyLevels.forEach { level ->
                    DifficultyRow(
                        level = level,
                        selected = level == selectedLevel,
                        onSelect = { onLevelSelected(level) }
                    )
                }
            }
        }
    }
}

@Composable
fun SimpleTopBar(title: String, onBack: () -> Unit, trailing: @Composable (() -> Unit)? = null) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Text("<", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onBackground)
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.weight(1f).padding(start = 4.dp)
        )
        if (trailing != null) trailing()
    }
}

@Composable
private fun SettingsToggleRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onBackground)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun DifficultyRow(level: DifficultyLevel, selected: Boolean, onSelect: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(selected = selected, onClick = onSelect)
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = onSelect)
        Text(
            text = stringResource(level.nameRes),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}
