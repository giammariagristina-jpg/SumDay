package com.giammariagristina.sumday

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState

/** Versione DESKTOP, solo per provare l'interfaccia sul PC senza iPhone (finestra grande come un iPhone). */
fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Sumday - prova desktop",
        state = rememberWindowState(width = 390.dp, height = 844.dp)
    ) {
        App()
    }
}
