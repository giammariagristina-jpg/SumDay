package com.giammariagristina.sumday

import android.content.Intent
import android.graphics.Color as AndroidColor
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import android.widget.Toast
import com.giammariagristina.sumday.ui.theme.SumdayTheme

enum class Screen {
    Menu, DailyQuiz, Play, HowToPlay, Leaderboard, Settings
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(AndroidColor.TRANSPARENT, AndroidColor.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(AndroidColor.TRANSPARENT, AndroidColor.TRANSPARENT)
        )
        setContent {
            SumdayTheme {
                SumdayApp()
            }
        }
    }
}

@Composable
fun SumdayApp() {
    val context = LocalContext.current
    var nickname by remember { mutableStateOf(PlayerPrefs.getNickname(context)) }
    var screen by remember { mutableStateOf(Screen.Menu) }
    var selectedLevel by remember { mutableStateOf(difficultyLevels[0]) }
    val welcomeBackMessage = stringResource(R.string.welcome_back_toast, nickname)

    LaunchedEffect(Unit) {
        if (nickname.isNotBlank()) {
            Toast.makeText(context, welcomeBackMessage, Toast.LENGTH_SHORT).show()
        }
    }

    BackHandler(enabled = screen != Screen.Menu) { screen = Screen.Menu }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        if (nickname.isBlank()) {
            NicknameScreen(onSubmit = { name ->
                PlayerPrefs.saveNickname(context, name)
                nickname = name
            })
            return@Surface
        }
        when (screen) {
            Screen.Menu -> MenuScreen(onNavigate = { screen = it }, selectedLevel = selectedLevel)
            Screen.DailyQuiz -> DailyQuizScreen(onBack = { screen = Screen.Menu })
            Screen.Play -> PlayScreen(onBack = { screen = Screen.Menu }, level = selectedLevel)
            Screen.HowToPlay -> HowToPlayScreen(onBack = { screen = Screen.Menu })
            Screen.Leaderboard -> LeaderboardScreen(onBack = { screen = Screen.Menu })
            Screen.Settings -> SettingsScreen(
                onBack = { screen = Screen.Menu },
                selectedLevel = selectedLevel,
                onLevelSelected = { selectedLevel = it }
            )
        }
    }
}

@Composable
fun MenuScreen(onNavigate: (Screen) -> Unit, selectedLevel: DifficultyLevel) {
    Scaffold(
        bottomBar = { BottomBar(onSettings = { onNavigate(Screen.Settings) }) }
    ) { padding ->
        MenuContent(padding = padding, onNavigate = onNavigate, selectedLevel = selectedLevel)
    }
}

@Composable
private fun MenuContent(padding: PaddingValues, onNavigate: (Screen) -> Unit, selectedLevel: DifficultyLevel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = stringResource(R.string.menu_tagline),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp, bottom = 40.dp)
        )

        Button(
            onClick = { onNavigate(Screen.Play) },
            modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)
        ) { Text(stringResource(R.string.menu_play)) }

        OutlinedButton(
            onClick = { onNavigate(Screen.DailyQuiz) },
            modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)
        ) { Text(stringResource(R.string.menu_daily_quiz)) }

        OutlinedButton(
            onClick = { onNavigate(Screen.HowToPlay) },
            modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)
        ) { Text(stringResource(R.string.menu_how_to_play)) }

        OutlinedButton(
            onClick = { onNavigate(Screen.Leaderboard) },
            modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)
        ) { Text(stringResource(R.string.menu_leaderboard)) }

        Text(
            text = stringResource(R.string.menu_difficulty_label, stringResource(selectedLevel.nameRes)),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(top = 32.dp)
        )
        Text(
            text = stringResource(R.string.menu_change_in_settings),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun BottomBar(onSettings: () -> Unit) {
    var showPrivacyDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars.only(WindowInsetsSides.Bottom))
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        TextButton(
            onClick = { showPrivacyDialog = true },
            modifier = Modifier.align(Alignment.CenterStart)
        ) { Text(stringResource(R.string.bottom_privacy), color = MaterialTheme.colorScheme.onSurfaceVariant) }

        Text(
            text = stringResource(R.string.orange_emoji),
            fontSize = 25.sp,
            modifier = Modifier.align(Alignment.Center)
        )

        TextButton(
            onClick = onSettings,
            modifier = Modifier.align(Alignment.CenterEnd)
        ) { Text(stringResource(R.string.bottom_settings), color = MaterialTheme.colorScheme.onSurfaceVariant) }
    }

    if (showPrivacyDialog) {
        PrivacyDialog(onDismiss = { showPrivacyDialog = false })
    }
}

@Composable
private fun PrivacyDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.privacy_title)) },
        text = { Text(stringResource(R.string.privacy_message)) },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.privacy_close)) }
        },
        dismissButton = {
            TextButton(onClick = {
                val intent = Intent(Intent.ACTION_VIEW, "https://sites.google.com/view/privacy-policy-for-skat-in".toUri())
                context.startActivity(intent)
            }) { Text(stringResource(R.string.privacy_policy)) }
        }
    )
}
