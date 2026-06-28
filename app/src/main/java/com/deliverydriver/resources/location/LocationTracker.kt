package com.deliverydriver.resources.location

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.*
import kotlin.math.roundToLong

data class TrackedPoint(
    val latitude: Double,
    val longitude: Double,
    val timestamp: Long
)

data class TrackedStop(
    val latitude: Double,
    val longitude: Double,
    val arrivalTime: Long,
    val departureTime: Long = arrivalTime
)

data class LocationTrackingState(
    val isTracking: Boolean = false,
    val route: List<TrackedPoint> = emptyList(),
    val stops: List<TrackedStop> = emptyList(),
    val totalDistanceMeters: Double = 0.0
)

object LocationTracker {
    private val _state = MutableStateFlow(LocationTrackingState())
    val state: StateFlow<LocationTrackingState> = _state.asStateFlow()

    private var fusedLocationClient: FusedLocationProviderClient? = null
    private var locationCallback: LocationCallback? = null

    private var dwellStartTime: Long? = null
    private var dwellStartLat: Double = 0.0
    private var dwellStartLng: Double = 0.0
    private var dwellStopLogged: Boolean = false
    private var lastPoint: TrackedPoint? = null
    private val loggedAreaKeys = mutableSetOf<String>()

    private val STILL_RADIUS_METERS = 50.0
    private val DWELL_MS = 90_000L
    private val MOVE_THRESHOLD_METERS = 100.0

    fun startTracking(context: Context) {
        if (_state.value.isTracking) return

        _state.value = LocationTrackingState(isTracking = true)
        dwellStartTime = null
        dwellStopLogged = false
        loggedAreaKeys.clear()
        lastPoint = null

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 30_000L)
            .setMinUpdateIntervalMillis(15_000L)
            .build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { loc ->
                    onLocationUpdate(loc.latitude, loc.longitude)
                }
            }
        }

        try {
            fusedLocationClient?.requestLocationUpdates(
                locationRequest,
                locationCallback!!,
                context.mainLooper
            )
        } catch (_: SecurityException) {
            _state.value = LocationTrackingState()
        }
    }

    fun stopTracking() {
        locationCallback?.let { fusedLocationClient?.removeLocationUpdates(it) }
        locationCallback = null
        fusedLocationClient = null
    }

    fun reset() {
        stopTracking()
        _state.value = LocationTrackingState()
        dwellStartTime = null
        dwellStopLogged = false
        loggedAreaKeys.clear()
        lastPoint = null
    }

    fun getOpenMapsIntent(): Intent {
        val state = _state.value
        val allPoints = mutableListOf<TrackedPoint>()

        for (stop in state.stops) {
            allPoints.add(TrackedPoint(stop.latitude, stop.longitude, stop.arrivalTime))
        }

        if (allPoints.isEmpty()) {
            allPoints.addAll(state.route)
        }

        if (allPoints.isEmpty()) {
            return Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps"))
        }

        val dirUrl = buildString {
            append("https://www.google.com/maps/dir/")
            append("${allPoints.first().latitude},${allPoints.first().longitude}")
            for (i in 1 until allPoints.size) {
                append("/${allPoints[i].latitude},${allPoints[i].longitude}")
            }
        }

        return Intent(Intent.ACTION_VIEW, Uri.parse(dirUrl))
    }

    fun getDistanceKm(): String {
        val meters = _state.value.totalDistanceMeters
        return if (meters > 0) "%.1f".format(meters / 1000.0) else "—"
    }

    private fun onLocationUpdate(lat: Double, lng: Double) {
        val now = System.currentTimeMillis()
        val point = TrackedPoint(lat, lng, now)
        val current = _state.value
        val route = current.route + point
        var distance = current.totalDistanceMeters

        if (lastPoint != null) {
            distance += haversineMeters(
                lastPoint!!.latitude, lastPoint!!.longitude,
                lat, lng
            )
        }
        lastPoint = point

        if (dwellStartTime == null) {
            dwellStartTime = now
            dwellStartLat = lat
            dwellStartLng = lng
            dwellStopLogged = false
        } else {
            val distFromDwell = haversineMeters(dwellStartLat, dwellStartLng, lat, lng)

            if (distFromDwell < STILL_RADIUS_METERS) {
                val elapsed = now - dwellStartTime!!
                if (elapsed >= DWELL_MS && !dwellStopLogged) {
                    val areaKey = "${(dwellStartLat * 1000).roundToLong()},${(dwellStartLng * 1000).roundToLong()}"
                    if (areaKey !in loggedAreaKeys) {
                        loggedAreaKeys.add(areaKey)
                        val stop = TrackedStop(
                            latitude = dwellStartLat,
                            longitude = dwellStartLng,
                            arrivalTime = dwellStartTime!!,
                            departureTime = now
                        )
                        _state.value = current.copy(
                            route = route,
                            stops = current.stops + stop,
                            totalDistanceMeters = distance
                        )
                        dwellStopLogged = true
                    } else {
                        _state.value = current.copy(route = route, totalDistanceMeters = distance)
                    }
                } else {
                    _state.value = current.copy(route = route, totalDistanceMeters = distance)
                }
            } else if (distFromDwell > MOVE_THRESHOLD_METERS) {
                dwellStartTime = now
                dwellStartLat = lat
                dwellStartLng = lng
                dwellStopLogged = false
                _state.value = current.copy(route = route, totalDistanceMeters = distance)
            } else {
                _state.value = current.copy(route = route, totalDistanceMeters = distance)
            }
        }
    }

    private fun haversineMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val R = 6371000.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val sinDLat = sin(dLat / 2)
        val sinDLon = sin(dLon / 2)
        val a = sinDLat * sinDLat +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sinDLon * sinDLon
        return R * 2 * atan2(sqrt(a), sqrt(1 - a))
    }
}
