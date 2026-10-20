package com.giammariagristina.sumday

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

/**
 * Punto di ingresso comune (iOS e prova desktop).
 *
 * PROVVISORIO (20/10/2026): solo l'impalcatura del progetto. Le schermate si portano una alla volta
 * da app/src/main/java/com/giammariagristina/sumday/ (MainActivity.SumdayApp, MenuScreen, ...),
 * seguendo le regole di CLAUDE.md.
 */
@Composable
fun App() {
    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("SumDay")   // res/values/strings.xml: app_name
            }
        }
    }
}
