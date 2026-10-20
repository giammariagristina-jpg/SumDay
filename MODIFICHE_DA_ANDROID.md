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
