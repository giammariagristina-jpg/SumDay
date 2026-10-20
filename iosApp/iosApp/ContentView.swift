import SwiftUI
import UIKit
import ComposeApp

/// Ospita l'interfaccia Compose (scritta in Kotlin) dentro l'app iOS.
struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        MainViewControllerKt.MainViewController()
    }
    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

struct ContentView: View {
    var body: some View {
        // MainActivity.onCreate di SumDay: enableEdgeToEdge con barre di sistema VISIBILI e trasparenti
        // (SystemBarStyle.light = icone scure). Diversamente da SkipMaze la barra di stato non si nasconde:
        // Compose disegna a tutto schermo e le schermate rispettano WindowInsets.safeDrawing come su Android.
        ComposeView()
            .ignoresSafeArea(.all)
            .preferredColorScheme(.light)
    }
}
