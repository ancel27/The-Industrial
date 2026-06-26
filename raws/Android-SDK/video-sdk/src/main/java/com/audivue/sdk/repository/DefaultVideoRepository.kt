package com.audivue.sdk.repository

import com.audivue.sdk.api.VideoRemoteDataSource

internal class DefaultVideoRepository(private val remoteDataSource: VideoRemoteDataSource) : VideoRepository {
    override suspend fun getVideoList(cursor: String?, limit: Int) = remoteDataSource.getVideoList(cursor, limit)
    override suspend fun getVideoDetails(videoId: String) = remoteDataSource.getVideoDetails(videoId)
    override suspend fun getPlaybackInfo(videoId: String) = remoteDataSource.getPlaybackInfo(videoId)
    override suspend fun like(videoId: String) = remoteDataSource.like(videoId)
    override suspend fun unlike(videoId: String) = remoteDataSource.unlike(videoId)
    override suspend fun addComment(videoId: String, text: String) = remoteDataSource.addComment(videoId, text)
    override suspend fun share(videoId: String, target: String) = remoteDataSource.share(videoId, target)
    override suspend fun trackProgress(videoId: String, progressSeconds: Long, completed: Boolean) = remoteDataSource.trackProgress(videoId, progressSeconds, completed)
}
