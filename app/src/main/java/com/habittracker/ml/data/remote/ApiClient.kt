package com.habittracker.ml.data.remote

import android.content.Context
import com.habittracker.ml.data.local.preferences.AuthPreferences
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {

    // Change this to your server URL
    // For emulator use 10.0.2.2, for real device use your PC's local IP
    private const val BASE_URL = "http://10.0.2.2:8080/"

    @Volatile
    private var apiService: ApiService? = null

    @Volatile
    private var authPreferences: AuthPreferences? = null

    fun init(context: Context) {
        authPreferences = AuthPreferences(context)
    }

    fun getService(context: Context): ApiService {
        return apiService ?: synchronized(this) {
            apiService ?: buildService(context).also { apiService = it }
        }
    }

    private fun buildService(context: Context): ApiService {
        if (authPreferences == null) {
            authPreferences = AuthPreferences(context)
        }

        val authInterceptor = Interceptor { chain ->
            val token = authPreferences?.getToken()
            val request = if (!token.isNullOrEmpty()) {
                chain.request().newBuilder()
                    .addHeader("Authorization", "Bearer $token")
                    .build()
            } else {
                chain.request()
            }
            chain.proceed(request)
        }

        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        return retrofit.create(ApiService::class.java)
    }

    fun resetClient() {
        apiService = null
    }
}
