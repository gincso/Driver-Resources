package com.deliverydriver.resources.scanner

import android.content.Context
import android.location.Address
import android.location.Geocoder
import com.deliverydriver.resources.data.models.DeliveryStop
import java.util.Locale
import kotlin.math.*

data class GeoPoint(val latitude: Double, val longitude: Double)

object RouteOptimizer {

    suspend fun optimizeRoute(
        stops: List<DeliveryStop>,
        context: Context
    ): List<DeliveryStop> {
        if (stops.size <= 2) return stops

        val geocoder = Geocoder(context, Locale.getDefault())
        val stopsWithCoords = mutableListOf<Pair<DeliveryStop, GeoPoint?>>()

        for (stop in stops) {
            val fullAddress = buildFullAddress(stop)
            val point = try {
                geocodeAddress(geocoder, fullAddress)
            } catch (_: Exception) {
                null
            }
            stopsWithCoords.add(stop to point)
        }

        val geocoded = stopsWithCoords.filter { it.second != null }
            .map { it.first to it.second!! }

        if (geocoded.size <= 1) return stops

        return nearestNeighborSort(geocoded)
    }

    fun buildFullAddress(stop: DeliveryStop): String {
        return buildString {
            append(stop.address)
            if (stop.city.isNotBlank()) append(", ${stop.city}")
            if (stop.state.isNotBlank()) append(", ${stop.state}")
            if (stop.zip.isNotBlank()) append(" ${stop.zip}")
        }
    }

    private fun geocodeAddress(geocoder: Geocoder, address: String): GeoPoint? {
        val results: List<Address> = geocoder.getFromLocationName(address, 1) ?: return null
        if (results.isEmpty()) return null
        return GeoPoint(results[0].latitude, results[0].longitude)
    }

    private fun nearestNeighborSort(
        stopsWithCoords: List<Pair<DeliveryStop, GeoPoint>>
    ): List<DeliveryStop> {
        val remaining = stopsWithCoords.toMutableList()
        val result = mutableListOf<DeliveryStop>()

        var current = remaining.removeFirst()
        result.add(current.first.copy(stopOrder = result.size))

        while (remaining.isNotEmpty()) {
            val (closestIdx, _) = remaining.mapIndexed { idx, pair ->
                idx to haversine(
                    current.second.latitude, current.second.longitude,
                    pair.second.latitude, pair.second.longitude
                )
            }.minBy { it.second }

            current = remaining.removeAt(closestIdx)
            result.add(current.first.copy(stopOrder = result.size))
        }

        return result
    }

    private fun haversine(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val R = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val sinDLat = sin(dLat / 2)
        val sinDLon = sin(dLon / 2)
        val a = sinDLat * sinDLat +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sinDLon * sinDLon
        return R * 2 * atan2(sqrt(a), sqrt(1 - a))
    }
}
