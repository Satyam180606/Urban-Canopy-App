package com.example.urbancanopy.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

data class ReportRequest(
    val lat: Double,
    val lng: Double,
    val description: String,
    val imageUrl: String?
)

data class ReportResponse(
    val success: Boolean,
    val data: ReportData?
)

data class ReportData(
    val id: String,
    val status: String
)

interface ApiService {
    @POST("api/reports")
    suspend fun createReport(
        @Header("Authorization") token: String,
        @Body request: ReportRequest
    ): Response<ReportResponse>
}
