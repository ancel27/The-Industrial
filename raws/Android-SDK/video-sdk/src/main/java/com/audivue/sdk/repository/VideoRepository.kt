package com.audivue.sdk.repository

import com.audivue.sdk.models.Comment
import com.audivue.sdk.models.EngagementResult
import com.audivue.sdk.models.PlaybackInfo
import com.audivue.sdk.models.VideoDetails
import com.audivue.sdk.models.VideoFeedPage

interface VideoRepository {
    suspend fun getVideoList(cursor: String? = null, limit: Int = 20): VideoFeedPage
    suspend fun getVideoDetails(videoId: String): VideoDetails
    suspend fun getPlaybackInfo(videoId: String): PlaybackInfo
    suspend fun like(videoId: String): EngagementResult
    suspend fun unlike(videoId: String): EngagementResult
    suspend fun addComment(videoId: String, text: String): Comment
    suspend fun share(videoId: String, target: String): EngagementResult
    suspend fun trackProgress(videoId: String, progressSeconds: Long, completed: Boolean)
}
