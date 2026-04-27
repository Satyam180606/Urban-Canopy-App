package com.example.urbancanopy.logic

import com.google.android.gms.maps.model.LatLng

class GameEngine {

    companion object {
        init {
            System.loadLibrary("urbancanopy")
        }
    }

    external fun calculateUserStats(totalPoints: Int): UserStats
    
    external fun isPatchGreen(imageData: ByteArray): Boolean

    external fun clusterMarkers(latitudes: DoubleArray, longitudes: DoubleArray, zoomLevel: Float): IntArray

    external fun calculateRank(userPoints: Int, allPoints: IntArray): Int

    fun getClusteredMarkers(points: List<LatLng>, zoom: Float): List<LatLng> {
        val lats = points.map { it.latitude }.toDoubleArray()
        val lngs = points.map { it.longitude }.toDoubleArray()
        val indices = clusterMarkers(lats, lngs, zoom)
        return indices.map { points[it] }
    }
}
