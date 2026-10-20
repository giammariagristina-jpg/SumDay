# Modifiche fatte su `app/` (Android) dopo la copia del 20/10/2026

Ogni modifica all'app Android si annota qui (CLAUDE.md, regola 7). Il codice sostituito resta
commentato nel file.

## 20/10/2026 — Copia iniziale

- `app/` copiata senza modifiche da `AndroidStudioProjects/Sumday` (versione 4.0.0, versionCode 5).
- Unico file cambiato fuori da `app/`: `gradle.properties`, riga `org.gradle.java.home` commentata
  (percorso Windows che bloccherebbe la build su GitHub Actions).

## 20/10/2026 — Nome "SumDay" con la D maiuscola

- File: `app/src/main/res/values/strings.xml` e `values-it/strings.xml` (vecchio testo in commento).
- Stringhe: `app_name`, `privacy_title`, `nickname_welcome` ("Sumday" → "SumDay").
- Non cambiati i nomi interni: `SumdayPrefs` (chiave dei dati salvati), `SumdayTheme`, `SumdayApp`,
  `Theme.Sumday`.
- iOS: `CFBundleDisplayName` in `iosApp/project.yml`.

## 20/10/2026 — Classifica di test o di produzione: regola unica come SkipMaze e Astro Tilt

- File: `app/build.gradle.kts` → `buildConfigField("int", "INTERNAL_PROD_CODE_VERSION", "0")`,
  `buildConfig = true` e la procedura operativa per il rilascio copiata da SkipMaze.
- File: `DailyLeaderboard.kt` → `COLLECTION` = `leaderboard_prod` se
  `BuildConfig.INTERNAL_PROD_CODE_VERSION != 0`, altrimenti `leaderboard` (vecchia costante commentata).
- Regole Firestore: `'SumDay'` aggiunto alle app ammesse in `leaderboard_prod` (vedi
  `firebase/firestore.rules`). Senza questa modifica in console le build di produzione non
  potrebbero scrivere.
- Su iOS la stessa regola andra' in `Firebase.kt` quando si porta Firebase.

## 20/10/2026 — Elimina profilo (strada A)

- `res/values*/strings.xml`: `elimina_profilo`, `elimina_profilo_conferma`, `profilo_eliminato`,
  `elimina_profilo_errore`, copiati alla lettera da SkipMaze.
- `PlayerPrefs.kt`: `addSentDailyDate` (elenco `daily_quiz_sent_dates` in `SumdayPrefs`),
  `sentDailyDates` (aggiunge anche l'ultima `daily_quiz_date`, cosi' copre il risultato inviato
  prima di questa versione), `clearLocalProfile` (nickname, id, date inviate, quiz del giorno,
  migliori tempi `best_*`; restano i suoni, come SkipMaze).
- `DailyQuizScreen.kt`: dopo `submitResult` riuscito salva la data.
- `DailyLeaderboard.kt`: `deleteProfile` cancella `sumday_daily_<data>_<id>` per ogni data, da
  `leaderboard` e `leaderboard_prod`.
- `SettingsScreen.kt`: in fondo, separatore e bottone "Elimina profilo" (#D32F2F, testo bianco,
  come `btnEliminaProfilo` di SkipMaze); conferma con OK / Annulla; poi toast "Profilo eliminato" e
  ritorno alla schermata del nickname (`MainActivity`: `onProfileDeleted`). In caso di errore toast
  `elimina_profilo_errore` e il profilo resta.
- Limite: risultati di giorni precedenti all'ultimo, inviati prima di questa versione, non sono
  noti all'app e restano sul server.

## 20/10/2026 — Tolta l'arancia dalla schermata principale

- `MainActivity.kt`, `BottomBar`: il `Text(orange_emoji)` al centro e' commentato. Tornera' come
  logo quando ci sara' un'azienda. La stringa `orange_emoji` resta in `strings.xml`.
