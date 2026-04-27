package com.example.urbancanopy.model

import com.google.firebase.Timestamp

data class User(
    val uid: String = "",
    val username: String = "",
    val totalPoints: Int = 0,
    val avatarUrl: String = ""
)

data class Patch(
    val id: String = "",
    val userId: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val timestamp: Timestamp? = null,
    val status: String = "pending", // "pending", "verified", "planted", "awarded"
    val description: String = "",
    val imageUrl: String = ""
)

data class Mission(
    val id: String = "",
    val title: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val rewardPoints: Int = 100
)

data class LeaderboardEntry(
    val userId: String = "",
    val username: String = "",
    val points: Int = 0,
    val rank: Int = 0
)
