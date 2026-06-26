package com.audivue.sdk.models

data class VideoDetails(
    val id: String,
    val title: String,
    val description: String? = null,
    val thumbnailUrl: String? = null,
    val durationSeconds: Long? = null,
    val type: VideoType = VideoType.Video,
    val channel: ChannelInfo? = null,
    val stats: VideoStats = VideoStats(),
    val userState: VideoUserState = VideoUserState()
)

data class ChannelInfo(
    val id: String? = null,
    val name: String,
    val slug: String? = null,
    val logoUrl: String? = null,
    val verified: Boolean = false,
    val subscribers: Long = 0
)

data class VideoStats(val viewsCount: Long = 0, val likesCount: Long = 0, val commentsCount: Long = 0)
data class VideoUserState(val liked: Boolean = false, val saved: Boolean = false, val subscribed: Boolean = false, val progressSeconds: Long = 0)

data class PlaybackInfo(
    val videoId: String,
    val primaryUrl: String,
    val format: PlaybackFormat = PlaybackFormat.HLS,
    val mimeType: String = "application/vnd.apple.mpegurl",
    val sources: List<PlaybackSource> = emptyList(),
    val captions: List<CaptionTrack> = emptyList(),
    val drm: DrmInfo = DrmInfo()
)

data class PlaybackSource(
    val url: String,
    val format: PlaybackFormat,
    val mimeType: String,
    val qualityLabel: String? = null,
    val width: Int? = null,
    val height: Int? = null,
    val bitrateKbps: Int? = null
)

data class CaptionTrack(val language: String, val label: String, val url: String, val mimeType: String = "text/vtt")
data class DrmInfo(val enabled: Boolean = false, val provider: String? = null, val scheme: String? = null, val licenseUrl: String? = null, val headers: Map<String, String> = emptyMap())

enum class PlaybackFormat { HLS, DASH, MP4 }
enum class VideoType { Video, Audio, Live }

data class VideoFeedPage(val items: List<VideoDetails>, val nextCursor: String? = null, val hasMore: Boolean = false)
data class EngagementResult(val ok: Boolean, val liked: Boolean? = null, val likesCount: Long? = null, val sharesCount: Long? = null)
data class Comment(val id: String, val userName: String, val text: String, val likesCount: Long = 0, val createdAt: String? = null)
