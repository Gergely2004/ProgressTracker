package com.example.progress.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import android.content.Context
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializer
import com.google.gson.JsonSerializer
import com.google.gson.JsonPrimitive
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.Instant
import java.time.ZoneOffset

object RetrofitClient {
    private const val BASE_URL = "http://localhost:8080/"

    fun getInstance(context: Context): ApiService {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(context.applicationContext))
            .addInterceptor(logging)
            .build()
        val formatter = DateTimeFormatter.ISO_DATE_TIME

        val localDateTimeDeserializer = JsonDeserializer { json, _, _ ->
            try {

                val str = json?.asString
                if (str.isNullOrBlank()) return@JsonDeserializer null
                return@JsonDeserializer LocalDateTime.parse(str, formatter)
            } catch (_: Exception) {
                try {
                    val str = json?.asString
                    if (str.isNullOrBlank()) return@JsonDeserializer null
                    val instant = try { Instant.parse(str) } catch (_: Exception) {
                        java.time.OffsetDateTime.parse(str, DateTimeFormatter.ISO_OFFSET_DATE_TIME).toInstant()
                    }
                    LocalDateTime.ofInstant(instant, ZoneOffset.UTC)
                } catch (_: Exception) {
                    null
                }
            }
        }

        val localDateTimeSerializer = JsonSerializer<LocalDateTime> { src, _, _ ->
            try {
                JsonPrimitive(src.format(formatter))
            } catch (_: Exception) {
                JsonPrimitive(src.toString())
            }
        }

        val gson = GsonBuilder()
            .registerTypeAdapter(LocalDateTime::class.java, localDateTimeDeserializer as JsonDeserializer<*>)
            .registerTypeAdapter(LocalDateTime::class.java, localDateTimeSerializer as JsonSerializer<*>)
            .create()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(ApiService::class.java)
    }
}
