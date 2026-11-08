package biz.tapos.utovar.api

import com.russhwolf.settings.Settings
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

object ApiClient {
    private const val BASE_URL = "https://utovar.tapos.biz/api"
    private val settings = Settings()

    val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
                coerceInputValues = true
            })
        }
    }

    var token: String?
        get() = settings.getStringOrNull("auth_token")
        set(value) {
            if (value != null) settings.putString("auth_token", value)
            else settings.remove("auth_token")
        }

    var userName: String?
        get() = settings.getStringOrNull("user_name")
        set(value) {
            if (value != null) settings.putString("user_name", value)
            else settings.remove("user_name")
        }

    suspend fun login(req: LoginRequest): LoginResponse {
        return client.post("$BASE_URL/login.php") {
            contentType(ContentType.Application.Json)
            setBody(req)
        }.body()
    }

    suspend fun getTrucks(): TruckListResponse {
        return client.get("$BASE_URL/trucks.php") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }.body()
    }

    suspend fun getTruckStatus(truckId: Int): TruckStatusResponse {
        return client.get("$BASE_URL/truck_status.php?truck_id=$truckId") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }.body()
    }

    suspend fun postScan(req: ScanRequest): ScanResponse {
        return client.post("$BASE_URL/scan.php") {
            contentType(ContentType.Application.Json)
            header(HttpHeaders.Authorization, "Bearer $token")
            setBody(req)
        }.body()
    }

    fun logout() {
        token = null
        userName = null
    }
}