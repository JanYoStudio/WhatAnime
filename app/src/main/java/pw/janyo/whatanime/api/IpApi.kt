package pw.janyo.whatanime.api

import pw.janyo.whatanime.model.response.GeoIpResponse
import retrofit2.http.GET

interface IpApi {
    @GET("/cn?json")
    suspend fun getGeoIp(): GeoIpResponse
}