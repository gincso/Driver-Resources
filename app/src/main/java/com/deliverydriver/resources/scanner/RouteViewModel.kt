package com.deliverydriver.resources.scanner

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.deliverydriver.resources.data.models.DeliveryStop
import com.deliverydriver.resources.data.models.StopStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object RouteViewModel {
    private val _stops = MutableStateFlow<List<DeliveryStop>>(emptyList())
    val stops: StateFlow<List<DeliveryStop>> = _stops.asStateFlow()

    var sortByAddress by mutableStateOf(true)
        private set

    fun addStop(stop: DeliveryStop) {
        _stops.value = _stops.value + stop.copy(stopOrder = _stops.value.size)
    }

    fun updateStop(updated: DeliveryStop) {
        _stops.value = _stops.value.map { if (it.id == updated.id) updated else it }
    }

    fun removeStop(id: String) {
        _stops.value = _stops.value.filter { it.id != id }
    }

    fun clearStops() {
        _stops.value = emptyList()
    }

    fun updateStatus(id: String, status: StopStatus) {
        _stops.value = _stops.value.map {
            if (it.id == id) it.copy(status = status) else it
        }
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
}
