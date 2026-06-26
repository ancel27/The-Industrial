package com.audivue.sdk.api

import com.audivue.sdk.core.SdkConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

class NetworkModule(private val config: SdkConfig) {
    val okHttpClient: OkHttpClient by lazy {
        val logging = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC }
        OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .header("Accept", "application/json")
                    .header("X-AudiVue-Platform", "android")
                    .header("X-AudiVue-SDK-Version", config.sdkVersion)
                    .apply { config.sdkKey?.let { header("X-AudiVue-SDK-Key", it) } }
                    .build()
                chain.proceed(request)
            }
            .addInterceptor(logging)
            .build()
    }

    val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(if (config.baseUrl.endsWith('/')) config.baseUrl else "${config.baseUrl}/")
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
    }
}
