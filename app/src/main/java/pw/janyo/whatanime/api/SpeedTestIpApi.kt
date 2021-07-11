package pw.janyo.whatanime.api

import pw.janyo.whatanime.model.response.GeoIpResponse
import pw.janyo.whatanime.model.response.SpeedTestIpResponse
import retrofit2.http.GET

interface SpeedTestIpApi {
    @GET("/api/location/info")
    suspend fun getSpeedTestIp(): SpeedTestIpResponse
}