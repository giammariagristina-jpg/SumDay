package com.giammariagristina.sumday

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import android.widget.Toast
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.TextButton
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

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
    onLevelSelected: (DifficultyLevel) -> Unit,
    onProfileDeleted: () -> Unit   // [20/10/2026] Elimina profilo
) {
    val context = LocalContext.current
    var soundsOn by remember { mutableStateOf(PlayerPrefs.isSoundEnabled(context)) }
    var soundVolume by remember { mutableStateOf(PlayerPrefs.getSoundVolume(context)) }   // [21/10/2026]
    var showDeleteDialog by remember { mutableStateOf(false) }
    var deleting by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val deletedMessage = stringResource(R.string.profilo_eliminato)
    val deleteErrorMessage = stringResource(R.string.elimina_profilo_errore)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        SimpleTopBar(title = stringResource(R.string.settings_title), onBack = onBack)

        // [21/10/2026] verticalScroll: con volume ed Elimina profilo la schermata puo' superare l'altezza
        Column(modifier = Modifier.verticalScroll(rememberScrollState()).padding(horizontal = 24.dp)) {
            SettingsToggleRow(
                label = stringResource(R.string.settings_sounds),
                checked = soundsOn,
                onCheckedChange = {
                    soundsOn = it
                    PlayerPrefs.setSoundEnabled(context, it)
                }
            )

            // [21/10/2026] Volume degli effetti sonori, sotto l'interruttore Suoni.
            // Slider Material3: stesso colore dell'interruttore (colorScheme.primary).
            // Disattivato (grigio) quando i suoni sono spenti; al rilascio fa sentire il volume scelto.
            Text(
                text = stringResource(R.string.sfx_volume),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(top = 4.dp)
            )
            Slider(
                value = soundVolume,
                onValueChange = {
                    soundVolume = it
                    PlayerPrefs.setSoundVolume(context, it)
                },
                onValueChangeFinished = { SoundPlayer.playVolumePreview(context) },
                enabled = soundsOn,
                modifier = Modifier.fillMaxWidth()
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

            // ── [20/10/2026] Elimina profilo (come SkipMaze: layout_opzioni.xml btnEliminaProfilo,
            //    sfondo #D32F2F e testo bianco; conferma con OK / Annulla) ──
            HorizontalDivider(modifier = Modifier.padding(vertical = 20.dp), color = MaterialTheme.colorScheme.outline)

            Button(
                onClick = { showDeleteDialog = true },
                enabled = !deleting,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F), contentColor = Color.White),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) { Text(stringResource(R.string.elimina_profilo)) }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            text = { Text(stringResource(R.string.elimina_profilo_conferma)) },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    deleting = true
                    scope.launch {
                        try {
                            DailyLeaderboard.deleteProfile(
                                PlayerPrefs.getPlayerId(context),
                                PlayerPrefs.sentDailyDates(context)
                            )
                            PlayerPrefs.clearLocalProfile(context)
                            Toast.makeText(context, deletedMessage, Toast.LENGTH_SHORT).show()
                            onProfileDeleted()
                        } catch (e: Exception) {
                            Toast.makeText(context, deleteErrorMessage, Toast.LENGTH_SHORT).show()
                        } finally {
                            deleting = false
                        }
                    }
                }) { Text(stringResource(android.R.string.ok)) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text(stringResource(android.R.string.cancel)) }
            }
        )
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
