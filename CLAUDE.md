# SumDayIOS — regole di porting

Questo repository contiene le due app di **SumDay** (quadrato magico: completare la griglia in modo
che righe e colonne diano la somma richiesta, con quiz del giorno e classifica):
- **Android**: cartella `app/` (Jetpack Compose + Material3). Copia del progetto
  `AndroidStudioProjects/Sumday` del 20/10/2026. Da questa data e' l'**app Android viva**: il vecchio
  progetto `Sumday` sul PC e' abbandonato e ogni modifica Android si fa qui.
  Si compila da Android Studio (su GitHub Actions e' esclusa con `SOLO_IOS`).
- **iPhone**: Kotlin / Compose Multiplatform (cartella `composeApp/`). Non c'e' un modulo motore
  separato: `MagicSquareEngine.kt` si copia in `composeApp/src/commonMain`.

**L'app Android è il riferimento assoluto.** Stesso metodo usato per Astro Tilt (SkyShapesIOS)
e SkipMaze (SkipMazeIOS).

## Differenza importante rispetto ad Astro Tilt e SkipMaze

SumDay Android e' **gia' scritta in Compose** (non in XML + View). Il porting quindi e' quasi una
copia: stesse `Column`, `Row`, `Text`, `Button`, stessi `dp`/`sp`, stesso Material3
(`compose.material3` di Compose Multiplatform). Si copia il file Android e si sostituiscono solo le
parti legate ad Android (tabella sotto), lasciando invariato tutto il resto.

| Android (`app/`) | iOS (`composeApp/`) |
|---|---|
| `stringResource(R.string.x, ...)` | `stringResource(Res.string.x, ...)` di Compose Multiplatform, con `strings.xml` copiati **alla lettera** in `composeResources/values` e `values-it` |
| `getSharedPreferences("SumdayPrefs")` | `Preferenze("SumdayPrefs")` (expect/actual: NSUserDefaults su iOS, come Astro Tilt) |
| `MediaPlayer` + `res/raw/*.mp3` | motore audio di SkipMaze (AVFAudio), file in `composeResources/files/raw` |
| `Toast` | toast di SkipMaze (`ToastAndroid.kt`) |
| `Intent(ACTION_VIEW)` privacy policy | SFSafariViewController (come SkipMaze) |
| Firestore SDK (`DailyLeaderboard`) | Firebase via REST (`Firebase.kt`, come SkipMaze) |
| `java.time.LocalDate`, `UUID`, `"%02d".format` | `kotlinx-datetime`/`kotlin.time`, `kotlin.uuid.Uuid`, formattatori scritti a mano |
| `BackHandler` | nessun tasto Indietro su iOS: si torna al menu con i bottoni gia' presenti |
| `DeviceFontFamilyName("sans-serif")`, `"sans-serif-black"` | font di sistema iOS (San Francisco) con `FontWeight.Normal` / `FontWeight.Black` — **adattamento dichiarato**: Roboto non e' presente su iPhone |

## Regole (da rispettare sempre)

1. **Non inventare nulla.** Ogni misura, colore, testo, peso del font e comportamento deve venire
   da un file Android preciso. Se su iOS serve un adattamento inevitabile, va dichiarato nel
   commento del codice, nel commit e nel messaggio all'utente.
2. **Si parte sempre dalle fonti Android** (`app/src/main/java/com/giammariagristina/sumday/`),
   mai dagli screenshot (che servono solo a trovare gli errori).
3. **Testi copiati alla lettera** da `res/values/strings.xml` (inglese) e
   `res/values-it/strings.xml` (italiano), maiuscole ed emoji comprese.
4. **Testi alla stessa scala di Android**: le impostazioni di dimensione del testo di iOS vanno
   ignorate (come in Astro Tilt e SkipMaze).
5. **Tocchi:** su iOS `Modifier.clickable` non è affidabile: usare sempre
   `pointerInput { detectTapGestures { ... } }` con `rememberUpdatedState` per l'azione.
   I `Button` Material3 funzionano: verificarlo sulla prima build.
6. **API solo-JVM vietate in `commonMain`** (`Math.*`, `System.*`, `String.format`,
   `"%.2f".format`, `java.*`): usare `kotlin.math`, `kotlin.time` e formattatori scritti a mano.
7. **Non cancellare: commentare.** `app/` si modifica solo su richiesta dell'utente, tenendola
   identica a iOS nelle funzioni comuni e annotando ogni modifica in `MODIFICHE_DA_ANDROID.md`.
8. **Riuso da SkipMaze e Astro Tilt.** Le soluzioni iOS gia' collaudate (salvataggio locale, audio,
   toast, condivisione, Firebase via REST) si riprendono da SkipMazeIOS, confrontandole sempre con
   il codice Android di SumDay: dove SumDay fa diversamente, vince SumDay.

## Repository pubblico

Il repository e' pubblico (build macOS gratuite su GitHub Actions). Quindi:
- **mai nel repository**: `google-services.json`, `GoogleService-Info.plist`, keystore, file `.p8`,
  password, chiavi. I segreti per la firma iOS stanno nei *Secrets* di GitHub
  (`ASC_KEY_ID`, `ASC_ISSUER_ID`, `ASC_KEY_P8`, `APPLE_TEAM_ID`): sono cifrati e invisibili.
- Firebase via REST (progetto `skatin-9b73f`, come Astro Tilt e SkipMaze): niente SDK ne'
  `GoogleService-Info.plist`. Classifica di test o di produzione con la regola unica di SkipMaze e
  Astro Tilt: `INTERNAL_PROD_CODE_VERSION` (in `app/build.gradle.kts` per Android, in `Firebase.kt`
  per iOS) = 0 → `leaderboard`, diverso da 0 → `leaderboard_prod`. Si imposta solo nella build da
  pubblicare e si rimette a 0 subito dopo.
- Per compilare l'app Android in Android Studio copiare a mano `app/google-services.json` dal
  vecchio progetto `Sumday` (e' ignorato da git).
- `firebase/firestore.rules` e' una **copia** delle regole della console Firebase: si aggiorna a
  mano dopo ogni modifica in console.

## Flusso di lavoro

- Si lavora sul ramo **`porting`** (i push su questo ramo non avviano build).
- Si unisce a `main` **solo quando lo chiede l'utente**: il push su `main` avvia
  GitHub Actions → TestFlight.
- Prima di chiedere una build si verifica in locale almeno la compilazione desktop
  (`SOLO_IOS=1 ./gradlew :composeApp:compileKotlinDesktop`) e l'app Android in Android Studio.
- Ogni commit elenca le fonti Android portate e gli eventuali adattamenti dichiarati.

## Elimina profilo (nuova funzione, Android + iOS)

Deciso il 20/10/2026 (strada A). SumDay scrive in `leaderboard` o `leaderboard_prod` un documento per ogni giorno giocato
al quiz del giorno: `sumday_daily_<aaaa-mm-gg>_<player_id>`. L'id del giocatore non e' un campo del
documento (le regole non lo permettono), quindi:
- l'app salva sul telefono (`SumdayPrefs`) l'elenco delle date per cui ha inviato un risultato;
- "Elimina profilo" cancella quei documenti da entrambe le classifiche (come SkipMaze), poi azzera
  nickname, id e dati locali;
- regole Firestore: `allow delete` in `leaderboard` e `leaderboard_prod` estesa a `sumday_.+`
  (vedi `firebase/firestore.rules`).
Limite noto: i risultati inviati **prima** di questa versione non sono nell'elenco locale.
Android fatto il 20/10/2026 (vedi `MODIFICHE_DA_ANDROID.md`): bottone in fondo a Impostazioni.
Su iOS va portato identico.

## Ordine dei lavori

1. Testi (`strings.xml` in `composeResources`), tema (`ui/theme`), `Preferenze`.
2. Schermate: nickname, menu (con barra in basso e dialogo privacy), come si gioca, impostazioni,
   gioco (`PlayScreen` + `MagicSquareEngine`), quiz del giorno, classifica personale.
3. Suoni (`SoundPlayer`: suono casuale a ogni nuovo quadrato, suono di soluzione).
4. Firebase (`DailyLeaderboard`: invio del tempo e tempi di oggi) ed elimina profilo.

## Arancia

L'emoji 🍊 in basso al centro del menu e' tolta (commentata) dal 20/10/2026: tornera' come logo
quando ci sara' un'azienda. Su iOS non va messa.

## Nome

Il nome dell'app e' **SumDay** (D maiuscola) ovunque sia visibile: `app_name`, testi, nome sotto
l'icona iOS. Restano invariati solo nomi interni come `SumdayPrefs` (chiave dei dati salvati:
cambiarla farebbe perdere i dati) e le classi `SumdayTheme`, `SumdayApp`.

## Da fare prima della pubblicazione

- Icona App Store: `iosApp/iosApp/Assets.xcassets/AppIcon.appiconset/icon-1024.png` = originale
  `Desktop/SumDay/ICONA/SumDay Icon.png` (1024×1024, RGB senza trasparenza, angoli non arrotondati:
  iOS li arrotonda da solo). Inserita il 20/10/2026.
- App registrata su App Store Connect con identificativo `com.giammariagristina.sumday`
  (uguale ad Android).
- iOS non ha tasti "Esci" (linee guida Apple), come in Astro Tilt e SkipMaze.
