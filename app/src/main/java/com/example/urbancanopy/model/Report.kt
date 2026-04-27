package com.example.urbancanopy.model

import com.google.android.gms.maps.model.LatLng

data class Report(
    val id: String = "",
    val userId: String = "",
    val imageUrl: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val address: String = "",
    val severity: String = "",
    val isAccessible: Boolean = false,
    val violationType: String = "",
    val description: String = "",
    val timestamp: Long = 0,
    val status: String = "pending"
)
