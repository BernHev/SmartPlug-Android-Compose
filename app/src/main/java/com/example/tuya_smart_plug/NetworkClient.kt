package com.example.tuya_smart_plug

import com.google.gson.annotations.SerializedName
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import java.util.concurrent.TimeUnit

data class StatusResponse(
    @SerializedName("status")
    val status: String
)

interface TuyaApiService {
    /**
     * Turns on the smart plug: GET /on
     */
    @GET("/on")
    suspend fun turnOn(): ResponseBody

    /**
     * Turns off the smart plug: GET /off
     */
    @GET("/off")
    suspend fun turnOff(): ResponseBody

    /**
     * Returns smart plug status: GET /status -> {"status": "on"} or {"status": "off"}
     */
    @GET("/status")
    suspend fun getStatus(): StatusResponse
}

object NetworkClient {
    // Base URL specified in requirements: http://192.168.66.6:5000
    private const val BASE_URL = "http://192.168.66.6:5000"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(5, TimeUnit.SECONDS)
        .writeTimeout(5, TimeUnit.SECONDS)
        .addInterceptor(loggingInterceptor)
        .retryOnConnectionFailure(true)
        .build()

    val apiService: TuyaApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(TuyaApiService::class.java)
    }
}