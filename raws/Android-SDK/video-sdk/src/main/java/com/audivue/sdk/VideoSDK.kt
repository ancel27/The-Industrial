package com.audivue.sdk

import android.content.Context
import com.audivue.sdk.api.NetworkModule
import com.audivue.sdk.api.NotConfiguredVideoRemoteDataSource
import com.audivue.sdk.api.VideoRemoteDataSource
import com.audivue.sdk.core.SdkConfig
import com.audivue.sdk.repository.DefaultVideoRepository
import com.audivue.sdk.repository.VideoRepository

object VideoSDK {
    private var initialized = false
    private lateinit var repository: VideoRepository

    fun initialize(
        context: Context,
        baseUrl: String,
        sdkKey: String? = null,
        remoteDataSource: VideoRemoteDataSource? = null
    ) {
        val config = SdkConfig(context.applicationContext, baseUrl.trimEnd('/'), sdkKey)
        val networkModule = NetworkModule(config)
        repository = DefaultVideoRepository(remoteDataSource ?: NotConfiguredVideoRemoteDataSource(networkModule))
        initialized = true
    }

    fun repository(): VideoRepository {
        check(initialized) { "VideoSDK.initialize(...) must be called before using AudiVue SDK." }
        return repository
    }

    fun isInitialized(): Boolean = initialized
}
