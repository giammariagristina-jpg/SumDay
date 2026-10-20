package com.giammariagristina.sumday

import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine

// Same shared "leaderboard" Firestore collection SkyShapes/Cookit/SkipMaze write
// to, filtered by an "app" field per game. Security rules restrict the document
// shape to exactly: nome_giocatore (string), punteggio (number), livello (number),
// app (one of a fixed allow-list, must be "SumDay" for us), plus optional
// livelli_completati/timestamp — no room for a "date" field. The day is encoded
// in the document id instead, so "today's times" is a document-id prefix query.
object DailyLeaderboard {
    // [20/10/2026] Sostituita dalla regola unica Android/iOS (come SkipMaze e Astro Tilt):
    // private const val COLLECTION = "leaderboard"
    // INTERNAL_PROD_CODE_VERSION != 0 → produzione, 0 → test. Nessuna lettura da Firebase.
    private val COLLECTION: String
        get() = if (BuildConfig.INTERNAL_PROD_CODE_VERSION != 0) "leaderboard_prod" else "leaderboard"
    private const val APP_NAME = "SumDay"
    private const val DAILY_QUIZ_LEVEL = 3 // position of "I've Got This" among the 4 difficulty levels

    private fun docIdPrefix(dateStr: String) = "sumday_daily_${dateStr}_"

    suspend fun submitResult(nickname: String, playerId: String, dateStr: String, seconds: Int) {
        val docId = docIdPrefix(dateStr) + playerId
        val data = hashMapOf(
            "nome_giocatore" to nickname,
            "punteggio" to seconds,
            "livello" to DAILY_QUIZ_LEVEL,
            "app" to APP_NAME
        )
        FirebaseFirestore.getInstance().collection(COLLECTION).document(docId).set(data).await()
    }

    suspend fun fetchTodayTimes(dateStr: String): List<Int> {
        val prefix = docIdPrefix(dateStr)
        val snapshot = FirebaseFirestore.getInstance().collection(COLLECTION)
            .orderBy(FieldPath.documentId())
            .startAt(prefix)
            .endAt(prefix + "")
            .get()
            .await()
        return snapshot.documents.mapNotNull { it.getLong("punteggio")?.toInt() }
    }

    // ── Elimina profilo (20/10/2026, come SkipMaze) ──
    // Cancella sumday_daily_<data>_<id> per ogni data inviata, da entrambe le classifiche.
    // Su Firestore cancellare un documento che non esiste non e' un errore.
    suspend fun deleteProfile(playerId: String, dates: Set<String>) {
        if (playerId.isBlank()) return
        val db = FirebaseFirestore.getInstance()
        for (dateStr in dates) {
            val docId = docIdPrefix(dateStr) + playerId
            for (collection in listOf("leaderboard", "leaderboard_prod")) {
                db.collection(collection).document(docId).delete().await()
            }
        }
    }
}

private suspend fun <T> Task<T>.await(): T = suspendCancellableCoroutine { cont ->
    addOnCompleteListener { task ->
        if (task.isSuccessful) {
            cont.resume(task.result)
        } else {
            cont.resumeWithException(task.exception ?: Exception("Unknown Firestore error"))
        }
    }
}
