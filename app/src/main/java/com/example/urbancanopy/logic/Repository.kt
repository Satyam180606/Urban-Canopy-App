package com.example.urbancanopy.logic

import com.example.urbancanopy.model.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.toObject
import com.google.firebase.firestore.toObjects
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.example.urbancanopy.model.Report
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class Repository {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val database = FirebaseDatabase.getInstance("https://urban-canopy-solution-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("reports")
    private val gameEngine = GameEngine()

    fun getCurrentUser() = auth.currentUser

    fun getUserStats(points: Int): UserStats {
        return gameEngine.calculateUserStats(points)
    }

    suspend fun getUserProfile(uid: String): User? {
        return try {
            firestore.collection("users").document(uid).get().await().toObject<User>()
        } catch (e: Exception) {
            null
        }
    }

    fun getMissions(): Flow<List<Mission>> = callbackFlow {
        val subscription = firestore.collection("missions")
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    trySend(snapshot.toObjects<Mission>())
                }
            }
        awaitClose { subscription.remove() }
    }

    fun getLeaderboard(): Flow<List<LeaderboardEntry>> = callbackFlow {
        val subscription = firestore.collection("users")
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
        val subscription = firestore.collection("patches")
            .whereEqualTo("userId", uid)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    trySend(snapshot.toObjects<Patch>())
                }
            }
        awaitClose { subscription.remove() }
    }
    
    fun getReports(): Flow<List<Report>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val reports = mutableListOf<Report>()
                for (child in snapshot.children) {
                    child.getValue(Report::class.java)?.let { reports.add(it) }
                }
                trySend(reports)
            }
            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        database.addValueEventListener(listener)
        awaitClose { database.removeEventListener(listener) }
    }
}
