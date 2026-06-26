package com.audivue.sdk.api

import com.audivue.sdk.core.VideoSdkException
import com.audivue.sdk.models.Comment
import com.audivue.sdk.models.EngagementResult
import com.audivue.sdk.models.PlaybackInfo
import com.audivue.sdk.models.VideoDetails
import com.audivue.sdk.models.VideoFeedPage

class NotConfiguredVideoRemoteDataSource(@Suppress("unused") private val networkModule: NetworkModule) : VideoRemoteDataSource {
    private fun notReady(): Nothing = throw VideoSdkException("AudiVue API endpoints are not configured yet. Implement VideoRemoteDataSource after the API contract is finalized.")
    override suspend fun getVideoList(cursor: String?, limit: Int): VideoFeedPage = notReady()
    override suspend fun getVideoDetails(videoId: String): VideoDetails = notReady()
    override suspend fun getPlaybackInfo(videoId: String): PlaybackInfo = notReady()
    override suspend fun like(videoId: String): EngagementResult = notReady()
    override suspend fun unlike(videoId: String): EngagementResult = notReady()
    override suspend fun addComment(videoId: String, text: String): Comment = notReady()
    override suspend fun share(videoId: String, target: String): EngagementResult = notReady()
    override suspend fun trackProgress(videoId: String, progressSeconds: Long, completed: Boolean) = notReady()
}
