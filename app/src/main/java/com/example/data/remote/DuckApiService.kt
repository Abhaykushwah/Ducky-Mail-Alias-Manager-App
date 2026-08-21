package com.example.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.Response
import retrofit2.http.Header
import retrofit2.http.POST

@JsonClass(generateAdapter = true)
data class DuckAddressResponse(
    @Json(name = "address") val address: String? = null,
    @Json(name = "error") val error: String? = null
)

interface DuckApiService {
    @POST("api/email/addresses")
    suspend fun generateDuckAddress(
        @Header("Authorization") authorization: String,
        @Header("User-Agent") userAgent: String = "DuckDuckGo-Android-EmailAliasManager/1.0"
    ): Response<DuckAddressResponse>
}
