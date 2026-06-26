package com.audivue.sdk.ui

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.ui.PlayerView
import com.audivue.sdk.VideoSDK
import com.audivue.sdk.models.VideoType
import com.audivue.sdk.player.AudiVuePlayerController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class VideoView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
    FrameLayout(context, attrs, defStyleAttr), DefaultLifecycleObserver {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val playerView = PlayerView(context)
    private val overlay = AudiVuePlayerOverlay(context)
    private var controller: AudiVuePlayerController? = null

    init {
        setBackgroundColor(0xFF050505.toInt())
        addView(playerView, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
        addView(overlay, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
        playerView.useController = false
        playerView.setShowBuffering(PlayerView.SHOW_BUFFERING_WHEN_PLAYING)
        overlay.playPauseButton.setOnClickListener {
            val player = controller?.player ?: return@setOnClickListener
            if (player.isPlaying) player.pause() else player.play()
            overlay.setPlaying(player.isPlaying)
        }
        overlay.previousButton.setOnClickListener {
            controller?.player?.seekBack()
        }
        overlay.nextButton.setOnClickListener {
            controller?.player?.seekForward()
        }
    }

    fun bindLifecycle(owner: LifecycleOwner) {
        owner.lifecycle.addObserver(this)
    }

    fun loadVideo(videoId: String) {
        overlay.setLoading(true)
        scope.launch {
            runCatching {
                val repository = VideoSDK.repository()
                val details = withContext(Dispatchers.IO) { repository.getVideoDetails(videoId) }
                val playback = withContext(Dispatchers.IO) { repository.getPlaybackInfo(videoId) }
                overlay.setLive(details.type == VideoType.Live)
                val playerController = controller ?: AudiVuePlayerController(context).also { setupController(it) }
                playerController.play(playback)
            }.onFailure { error ->
                overlay.setError(error.message ?: "Playback failed")
            }
        }
    }

    private fun setupController(playerController: AudiVuePlayerController) {
        controller = playerController
        playerView.player = playerController.player
        playerController.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                overlay.setLoading(playbackState == Player.STATE_BUFFERING)
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                overlay.setPlaying(isPlaying)
            }

            override fun onPlayerError(error: PlaybackException) {
                overlay.setError(error.message ?: "Playback error")
            }
        })
    }

    override fun onPause(owner: LifecycleOwner) {
        controller?.pause()
    }

    override fun onDestroy(owner: LifecycleOwner) {
        release()
    }

    fun release() {
        controller?.release()
        controller = null
        scope.cancel()
    }
}
