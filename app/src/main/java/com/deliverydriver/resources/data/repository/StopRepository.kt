package com.deliverydriver.resources.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.deliverydriver.resources.data.models.AccessCode
import com.deliverydriver.resources.data.models.DeliveryStop
import com.deliverydriver.resources.data.models.StopStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

private val Context.dataStore by preferencesDataStore(name = "route_data")

object StopRepository {
    private lateinit var appContext: Context

    private val STOPS_KEY = stringPreferencesKey("stops")
    private val ACCESS_CODES_KEY = stringPreferencesKey("access_codes")
    private val STOP_ADDRESS_HISTORY_KEY = stringPreferencesKey("stop_address_history")

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    fun loadStopsFlow(): Flow<List<DeliveryStop>> {
        return appContext.dataStore.data.map { prefs ->
            val json = prefs[STOPS_KEY] ?: return@map emptyList()
            parseStopsFromJson(json)
        }
    }

    suspend fun saveStops(stops: List<DeliveryStop>) {
        appContext.dataStore.edit { prefs ->
            prefs[STOPS_KEY] = stopsToJson(stops)
        }
    }

    fun loadAccessCodesFlow(): Flow<List<AccessCode>> {
        return appContext.dataStore.data.map { prefs ->
            val json = prefs[ACCESS_CODES_KEY] ?: return@map emptyList()
            parseAccessCodesFromJson(json)
        }
    }

    suspend fun saveAccessCodes(codes: List<AccessCode>) {
        appContext.dataStore.edit { prefs ->
            prefs[ACCESS_CODES_KEY] = accessCodesToJson(codes)
        }
    }

    fun loadAddressHistoryFlow(): Flow<Map<String, DeliveryStop>> {
        return appContext.dataStore.data.map { prefs ->
            val json = prefs[STOP_ADDRESS_HISTORY_KEY] ?: return@map emptyMap()
            parseAddressHistoryFromJson(json)
        }
    }

    suspend fun saveAddressHistory(history: Map<String, DeliveryStop>) {
        appContext.dataStore.edit { prefs ->
            prefs[STOP_ADDRESS_HISTORY_KEY] = addressHistoryToJson(history)
        }
    }

    fun addressToJson(stop: DeliveryStop): String {
        return JSONObject().apply {
            put("id", stop.id)
            put("address", stop.address)
            put("city", stop.city)
            put("state", stop.state)
            put("zip", stop.zip)
            put("customerName", stop.customerName)
            put("customerPhone", stop.customerPhone)
            put("packageCount", stop.packageCount)
            put("deliveryNotes", stop.deliveryNotes)
            put("accessCode", stop.accessCode)
            put("status", stop.status.name)
            put("stopOrder", stop.stopOrder)
            put("timestamp", stop.timestamp)
        }.toString()
    }

    private fun stopsToJson(stops: List<DeliveryStop>): String {
        return JSONArray(stops.map { stop ->
            JSONObject().apply {
                put("id", stop.id)
                put("address", stop.address)
                put("city", stop.city)
                put("state", stop.state)
                put("zip", stop.zip)
                put("customerName", stop.customerName)
                put("customerPhone", stop.customerPhone)
                put("packageCount", stop.packageCount)
                put("deliveryNotes", stop.deliveryNotes)
                put("accessCode", stop.accessCode)
                put("status", stop.status.name)
                put("stopOrder", stop.stopOrder)
                put("timestamp", stop.timestamp)
            }
        }).toString()
    }

    private fun parseStopsFromJson(json: String): List<DeliveryStop> {
        val array = JSONArray(json)
        return (0 until array.length()).map { i ->
            val obj = array.getJSONObject(i)
            DeliveryStop(
                id = obj.getString("id"),
                address = obj.getString("address"),
                city = obj.optString("city", ""),
                state = obj.optString("state", ""),
                zip = obj.optString("zip", ""),
                customerName = obj.optString("customerName", ""),
                customerPhone = obj.optString("customerPhone", ""),
                packageCount = obj.optInt("packageCount", 1),
                deliveryNotes = obj.optString("deliveryNotes", ""),
                accessCode = obj.optString("accessCode", ""),
                status = try { StopStatus.valueOf(obj.getString("status")) } catch (_: Exception) { StopStatus.PENDING },
                stopOrder = obj.optInt("stopOrder", i),
                timestamp = obj.optLong("timestamp", System.currentTimeMillis())
            )
        }
    }

    private fun accessCodesToJson(codes: List<AccessCode>): String {
        return JSONArray(codes.map { code ->
            JSONObject().apply {
                put("id", code.id)
                put("address", code.address)
                put("code", code.code)
                put("notes", code.notes)
                put("timestamp", code.timestamp)
            }
        }).toString()
    }

    private fun parseAccessCodesFromJson(json: String): List<AccessCode> {
        val array = JSONArray(json)
        return (0 until array.length()).map { i ->
            val obj = array.getJSONObject(i)
            AccessCode(
                id = obj.getInt("id"),
                address = obj.getString("address"),
                code = obj.getString("code"),
                notes = obj.optString("notes", ""),
                timestamp = obj.optLong("timestamp", System.currentTimeMillis())
            )
        }
    }

    private fun addressHistoryToJson(history: Map<String, DeliveryStop>): String {
        return JSONObject(history.mapValues { (_, stop) ->
            JSONObject().apply {
                put("id", stop.id)
                put("address", stop.address)
                put("city", stop.city)
                put("state", stop.state)
                put("zip", stop.zip)
                put("customerName", stop.customerName)
                put("customerPhone", stop.customerPhone)
                put("packageCount", stop.packageCount)
                put("deliveryNotes", stop.deliveryNotes)
                put("accessCode", stop.accessCode)
                put("status", stop.status.name)
                put("stopOrder", stop.stopOrder)
                put("timestamp", stop.timestamp)
            }
        }).toString()
    }

    private fun parseAddressHistoryFromJson(json: String): Map<String, DeliveryStop> {
        val obj = JSONObject(json)
        val map = mutableMapOf<String, DeliveryStop>()
        for (key in obj.keys()) {
            val s = obj.getJSONObject(key)
            map[key] = DeliveryStop(
                id = s.getString("id"),
                address = s.getString("address"),
                city = s.optString("city", ""),
                state = s.optString("state", ""),
                zip = s.optString("zip", ""),
                customerName = s.optString("customerName", ""),
                customerPhone = s.optString("customerPhone", ""),
                packageCount = s.optInt("packageCount", 1),
                deliveryNotes = s.optString("deliveryNotes", ""),
                accessCode = s.optString("accessCode", ""),
                status = try { StopStatus.valueOf(s.getString("status")) } catch (_: Exception) { StopStatus.PENDING },
                stopOrder = s.optInt("stopOrder", 0),
                timestamp = s.optLong("timestamp", System.currentTimeMillis())
            )
        }
        return map
    }
}
