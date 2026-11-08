package biz.tapos.utovar.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(val username: String, val password: String)

@Serializable
data class LoginResponse(
    val success: Boolean,
    val message: String? = null,
    val user: String? = null,
    val token: String? = null
)

@Serializable
data class Truck(
    val id: Int,
    @SerialName("truck_name") val truckName: String
)

@Serializable
data class TruckListResponse(
    val success: Boolean,
    val trucks: List<Truck> = emptyList(),
    val message: String? = null
)

@Serializable
data class ItemQty(
    val naziv: String,
    val kol: Int
)

@Serializable
data class CityDetail(
    val exp: Int = 0,
    val scan: Int = 0,
    val preostalo: List<ItemQty> = emptyList(),
    val utovareno: List<ItemQty> = emptyList()
)

@Serializable
data class TruckStatusResponse(
    val success: Boolean,
    @SerialName("truck_id") val truckId: Int = 0,
    @SerialName("truck_name") val truckName: String = "",
    @SerialName("route_is_set") val routeIsSet: Boolean = false,
    @SerialName("active_city") val activeCity: String? = null,
    @SerialName("is_complete") val isComplete: Boolean = false,
    @SerialName("total_expected") val totalExpected: Int = 0,
    @SerialName("total_scanned") val totalScanned: Int = 0,
    @SerialName("progress_percent") val progressPercent: Double = 0.0,
    @SerialName("route_cities") val routeCities: List<String> = emptyList(),
    @SerialName("cities_data") val citiesData: Map<String, CityDetail> = emptyMap(),
    val message: String? = null
)

@Serializable
data class ScanRequest(
    @SerialName("truck_id") val truckId: Int,
    val code: String,
    @SerialName("scan_mode") val scanMode: String = "load"
)

@Serializable
data class ScanResponse(
    val success: Boolean,
    val message: String = "",
    @SerialName("active_city") val activeCity: String? = null,
    val mode: String? = null
)