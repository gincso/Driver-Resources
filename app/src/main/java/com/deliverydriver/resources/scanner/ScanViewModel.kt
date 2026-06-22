package com.deliverydriver.resources.scanner

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ScannedPackage(
    val driverAidNumber: Int,
    val firstSeenTime: Long = System.currentTimeMillis(),
    val toteLabel: String = "",
    var count: Int = 1
)

data class ScanState(
    val scannedPackages: List<ScannedPackage> = emptyList(),
    val lastDetectedNumber: Int? = null,
    val isScanning: Boolean = false,
    val flashOn: Boolean = false,
    val error: String? = null
)

class ScanViewModel(application: Application) : AndroidViewModel(application) {

    private val _state = MutableStateFlow(ScanState())
    val state: StateFlow<ScanState> = _state.asStateFlow()

    // Debounce map to avoid duplicates
    private val recentDetections = mutableMapOf<Int, Long>()
    private val DEBOUNCE_MS = 3000L

    fun onNumberDetected(number: Int) {
        val now = System.currentTimeMillis()
        val lastTime = recentDetections[number]

        if (lastTime != null && (now - lastTime) < DEBOUNCE_MS) {
            // Check if this number is still pending addition
            val existing = _state.value.scannedPackages.find { it.driverAidNumber == number }
            if (existing != null) {
                // Already added, just update last seen
                recentDetections[number] = now
            }
            _state.value = _state.value.copy(lastDetectedNumber = number)
            return
        }

        recentDetections[number] = now
        _state.value = _state.value.copy(lastDetectedNumber = number)

        // Add package with a small delay so user can see detection
        viewModelScope.launch {
            delay(200)
            addPackage(number)
        }
    }

    private fun addPackage(number: Int) {
        val current = _state.value.scannedPackages
        val existing = current.find { it.driverAidNumber == number }

        val updated = if (existing != null) {
            current.map {
                if (it.driverAidNumber == number) it.copy(count = it.count + 1)
                else it
            }
        } else {
            current + ScannedPackage(driverAidNumber = number)
        }

        _state.value = _state.value.copy(
            scannedPackages = updated.sortedBy { it.driverAidNumber }
        )
    }

    fun removePackage(number: Int) {
        val updated = _state.value.scannedPackages.filter { it.driverAidNumber != number }
        _state.value = _state.value.copy(scannedPackages = updated)
        recentDetections.remove(number)
    }

    fun clearAll() {
        _state.value = ScanState(flashOn = _state.value.flashOn)
        recentDetections.clear()
    }

    fun toggleScanning() {
        _state.value = _state.value.copy(isScanning = !_state.value.isScanning)
    }

    fun toggleFlash() {
        _state.value = _state.value.copy(flashOn = !_state.value.flashOn)
    }

    fun setError(msg: String?) {
        _state.value = _state.value.copy(error = msg)
    }

    // Organization logic
    fun getOrganizationGroups(): Map<String, List<ScannedPackage>> {
        val packages = _state.value.scannedPackages
        if (packages.isEmpty()) return emptyMap()

        val sorted = packages.sortedBy { it.driverAidNumber }
        val groups = mutableMapOf<String, MutableList<ScannedPackage>>()

        // Determine group size (rough quarters)
        val size = sorted.size
        val quarterSize = maxOf(1, size / 4)

        var currentGroup = 1
        var counter = 0

        for (pkg in sorted) {
            val groupName = when {
                currentGroup == 1 -> "🚐 Back of Van (Load First)"
                currentGroup == 2 -> "📦 Middle-Back"
                currentGroup == 3 -> "📦 Middle-Front"
                else -> "🚪 Front / Passenger Seat (Load Last)"
            }

            groups.getOrPut(groupName) { mutableListOf() }.add(pkg)
            counter++
            if (counter >= quarterSize && currentGroup < 4) {
                currentGroup++
                counter = 0
            }
        }

        return groups
    }

    fun getOrganizationSummary(): String {
        val packages = _state.value.scannedPackages
        if (packages.isEmpty()) return "No packages scanned yet."

        val sorted = packages.sortedBy { it.driverAidNumber }
        val min = sorted.first().driverAidNumber
        val max = sorted.last().driverAidNumber

        return buildString {
            appendLine("📊 ${packages.size} packages scanned")
            appendLine("📈 Range: $min → $max")
            appendLine()
            appendLine("Load order (back → front):")
            getOrganizationGroups().forEach { (group, pkgs) ->
                val nums = pkgs.map { it.driverAidNumber }
                appendLine("  $group")
                appendLine("    ${nums.joinToString(", ")}")
            }
            appendLine()
            append("💡 Load high numbers first (back of van), low numbers last (near cab)")
        }
    }
}
