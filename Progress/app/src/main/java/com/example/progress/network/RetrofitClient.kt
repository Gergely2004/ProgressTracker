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
import java.lang.reflect.Type

object RetrofitClient {
    private const val BASE_URL = "http://10.0.2.2:8080"
    fun getInstance(context: Context): ApiService {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(context.applicationContext))
            .addInterceptor(logging)
            .build()
        // Configure Gson to handle java.time.LocalDateTime <-> ISO strings
        val formatter = DateTimeFormatter.ISO_DATE_TIME

        val localDateTimeDeserializer = JsonDeserializer { json, typeOfT, contextGson ->
            try {
                if (json == null || json.asString == null) return@JsonDeserializer null
                LocalDateTime.parse(json.asString, formatter)
            } catch (e: Exception) {
                // Fallback: try to parse as plain date/time without offset
                try {
                    LocalDateTime.parse(json.asString)
                } catch (ex: Exception) {
                    null
                }
            }
        }

        val localDateTimeSerializer = JsonSerializer<LocalDateTime> { src, typeOfSrc, contextGson ->
            try {
                JsonPrimitive(src.format(formatter))
            } catch (e: Exception) {
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
