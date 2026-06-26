package com.audivue.sdk.player

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import com.audivue.sdk.models.PlaybackFormat
import com.audivue.sdk.models.PlaybackInfo

@OptIn(UnstableApi::class)
internal class AudiVuePlayerController(context: Context) {
    val player: ExoPlayer = ExoPlayer.Builder(context.applicationContext).build()

    fun play(playbackInfo: PlaybackInfo) {
        val mediaItem = MediaItem.Builder()
            .setUri(playbackInfo.primaryUrl)
            .setMimeType(playbackInfo.format.toMimeType())
            .build()
        player.setMediaItem(mediaItem)
        player.prepare()
        player.playWhenReady = true
    }

    fun pause() = player.pause()
    fun release() = player.release()
    fun addListener(listener: Player.Listener) = player.addListener(listener)

    private fun PlaybackFormat.toMimeType(): String = when (this) {
        PlaybackFormat.HLS -> MimeTypes.APPLICATION_M3U8
        PlaybackFormat.DASH -> MimeTypes.APPLICATION_MPD
        PlaybackFormat.MP4 -> MimeTypes.VIDEO_MP4
    }
}
