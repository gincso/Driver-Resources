package com.deliverydriver.resources.scanner

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.deliverydriver.resources.data.models.DeliveryStop
import com.deliverydriver.resources.data.models.StopStatus
import com.deliverydriver.resources.data.repository.StopRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

object RouteViewModel {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val _stops = MutableStateFlow<List<DeliveryStop>>(emptyList())
    val stops: StateFlow<List<DeliveryStop>> = _stops.asStateFlow()

    var sortByAddress by mutableStateOf(true)
        private set

    private var _addressHistory by mutableStateOf<Map<String, DeliveryStop>>(emptyMap())
    val addressHistory: Map<String, DeliveryStop> get() = _addressHistory

    private var initialized = false

    fun init(context: Context) {
        if (initialized) return
        initialized = true
        StopRepository.init(context)

        scope.launch {
            StopRepository.loadStopsFlow().collect { loaded ->
                _stops.value = loaded
            }
        }
        scope.launch {
            StopRepository.loadAddressHistoryFlow().collect { history ->
                _addressHistory = history
            }
        }
    }

    fun addStop(stop: DeliveryStop) {
        val newStop = stop.copy(stopOrder = _stops.value.size)
        _stops.value = _stops.value + newStop
        scope.launch {
            StopRepository.saveStops(_stops.value)
            updateAddressHistory(newStop)
        }
    }

    fun updateStop(updated: DeliveryStop) {
        _stops.value = _stops.value.map { if (it.id == updated.id) updated else it }
        scope.launch {
            StopRepository.saveStops(_stops.value)
            updateAddressHistory(updated)
        }
    }

    fun removeStop(id: String) {
        _stops.value = _stops.value.filter { it.id != id }
        scope.launch { StopRepository.saveStops(_stops.value) }
    }

    fun clearStops() {
        _stops.value = emptyList()
        scope.launch { StopRepository.saveStops(_stops.value) }
    }

    fun updateStatus(id: String, status: StopStatus) {
        _stops.value = _stops.value.map {
            if (it.id == id) it.copy(status = status) else it
        }
        scope.launch { StopRepository.saveStops(_stops.value) }
    }

    fun toggleSort() {
        sortByAddress = !sortByAddress
    }

    fun getSortedStops(): List<DeliveryStop> {
        return if (sortByAddress) {
            _stops.value.sortedBy { it.address }
        } else {
            _stops.value.sortedBy { it.stopOrder }
        }
    }

    fun getStats(): Triple<Int, Int, Int> {
        val all = _stops.value
        val total = all.size
        val delivered = all.count { it.status == StopStatus.DELIVERED }
        val totalPackages = all.sumOf { it.packageCount }
        return Triple(total, delivered, totalPackages)
    }

    fun optimizeRoute(context: Context) {
        scope.launch {
            val current = _stops.value
            if (current.size <= 2) return@launch
            val optimized = RouteOptimizer.optimizeRoute(current, context)
            val updated = if (optimized.size != current.size) {
                val optimizedIds = optimized.map { it.id }.toSet()
                val reordered = optimized.toMutableList()
                current.filter { it.id !in optimizedIds }.forEach { stop ->
                    reordered.add(stop.copy(stopOrder = reordered.size))
                }
                reordered
            } else {
                optimized
            }
            _stops.value = updated
            StopRepository.saveStops(updated)
        }
    }

    private suspend fun updateAddressHistory(stop: DeliveryStop) {
        val key = stop.address.trim().lowercase()
        if (key.isNotBlank()) {
            val updated = _addressHistory.toMutableMap()
            updated[key] = stop
            _addressHistory = updated
            StopRepository.saveAddressHistory(updated)
        }
    }
}
