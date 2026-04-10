package com.example.urbancanopy.logic

import com.example.urbancanopy.model.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.toObject
import com.google.firebase.firestore.toObjects
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class Repository {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val gameEngine = GameEngine()

    fun getCurrentUser() = auth.currentUser

    fun getUserStats(points: Int): UserStats {
        return gameEngine.calculateUserStats(points)
    }

    suspend fun getUserProfile(uid: String): User? {
        return try {
            db.collection("users").document(uid).get().await().toObject<User>()
        } catch (e: Exception) {
            null
        }
    }

    fun getMissions(): Flow<List<Mission>> = callbackFlow {
        val subscription = db.collection("missions")
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    trySend(snapshot.toObjects<Mission>())
                }
            }
        awaitClose { subscription.remove() }
    }

    fun getLeaderboard(): Flow<List<LeaderboardEntry>> = callbackFlow {
        val subscription = db.collection("users")
            .orderBy("totalPoints", Query.Direction.DESCENDING)
            .limit(50)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    val users = snapshot.toObjects<User>()
                    val entries = users.mapIndexed { index, user ->
                        LeaderboardEntry(
                            userId = user.uid,
                            username = user.username,
                            points = user.totalPoints,
                            rank = index + 1
                        )
                    }
                    trySend(entries)
                }
            }
        awaitClose { subscription.remove() }
    }

    fun getUserPatches(uid: String): Flow<List<Patch>> = callbackFlow {
        val subscription = db.collection("patches")
            .whereEqualTo("userId", uid)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    trySend(snapshot.toObjects<Patch>())
                }
            }
        awaitClose { subscription.remove() }
    }
    
    fun getOpenMissions(): Flow<List<Patch>> = callbackFlow {
        val subscription = db.collection("patches")
            .whereEqualTo("status", "pending")
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    trySend(snapshot.toObjects<Patch>())
                }
            }
        awaitClose { subscription.remove() }
    }
}
