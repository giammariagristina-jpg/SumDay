package com.giammariagristina.sumday

import androidx.compose.ui.window.ComposeUIViewController
import platform.UIKit.UIViewController

/** Punto di ingresso iOS: chiamato da Swift (ContentView.swift) come MainViewControllerKt.MainViewController(). */
fun MainViewController(): UIViewController = ComposeUIViewController {
    App()
}
