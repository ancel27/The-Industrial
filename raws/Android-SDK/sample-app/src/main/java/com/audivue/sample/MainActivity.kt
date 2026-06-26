package com.audivue.sample

import android.os.Bundle
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.audivue.sdk.VideoSDK
import com.audivue.sdk.api.VideoRemoteDataSource
import com.audivue.sdk.models.ChannelInfo
import com.audivue.sdk.models.Comment
import com.audivue.sdk.models.EngagementResult
import com.audivue.sdk.models.PlaybackFormat
import com.audivue.sdk.models.PlaybackInfo
import com.audivue.sdk.models.VideoDetails
import com.audivue.sdk.models.VideoFeedPage
import com.audivue.sdk.ui.EngagementBarView
import com.audivue.sdk.ui.VideoView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        VideoSDK.initialize(
            context = applicationContext,
            baseUrl = "https://api.example.com",
            remoteDataSource = SampleVideoRemoteDataSource()
        )

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(0xFF050505.toInt())
        }

        val videoView = VideoView(this).apply {
            bindLifecycle(this@MainActivity)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                (220 * resources.displayMetrics.density).toInt()
            )
        }

        layout.addView(videoView)
        layout.addView(EngagementBarView(this))
        setContentView(layout)
        videoView.loadVideo("sample-video")
    }
}

private class SampleVideoRemoteDataSource : VideoRemoteDataSource {
    override suspend fun getVideoList(cursor: String?, limit: Int): VideoFeedPage = VideoFeedPage(emptyList())
    override suspend fun getVideoDetails(videoId: String): VideoDetails = VideoDetails(id = videoId, title = "AudiVue Sample Video", channel = ChannelInfo(name = "AudiVue"))
    override suspend fun getPlaybackInfo(videoId: String): PlaybackInfo = PlaybackInfo(
        videoId = videoId,
        primaryUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
        format = PlaybackFormat.MP4,
        mimeType = "video/mp4"
    )
    override suspend fun like(videoId: String): EngagementResult = EngagementResult(ok = true, liked = true)
    override suspend fun unlike(videoId: String): EngagementResult = EngagementResult(ok = true, liked = false)
    override suspend fun addComment(videoId: String, text: String): Comment = Comment("sample", "AudiVue", text)
    override suspend fun share(videoId: String, target: String): EngagementResult = EngagementResult(ok = true)
    override suspend fun trackProgress(videoId: String, progressSeconds: Long, completed: Boolean) = Unit
}
