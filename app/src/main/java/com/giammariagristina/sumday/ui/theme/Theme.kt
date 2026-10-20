package com.giammariagristina.sumday.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val SumdayColorScheme = lightColorScheme(
    primary = SumdayPrimary,
    onPrimary = SumdayOnPrimary,
    background = SumdayBackground,
    surface = SumdaySurface,
    onBackground = SumdayTextPrimary,
    onSurface = SumdayTextPrimary,
    outline = SumdayBorder
)

@Composable
fun SumdayTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = SumdayColorScheme,
        typography = SumdayTypography,
        content = content
    )
}
