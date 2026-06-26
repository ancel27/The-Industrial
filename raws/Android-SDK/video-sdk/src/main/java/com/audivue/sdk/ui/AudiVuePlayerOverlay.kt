package com.audivue.sdk.ui

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.view.isVisible

internal class AudiVuePlayerOverlay(context: Context) : FrameLayout(context) {
    private val loading = ProgressBar(context)
    private val message = TextView(context)
    private val logo = TextView(context)
    private val liveBadge = TextView(context)
    val previousButton = TextView(context)
    val playPauseButton = TextView(context)
    val nextButton = TextView(context)

    init {
        setBackgroundColor(Color.TRANSPARENT)
        addView(loading, LayoutParams(52.dp, 52.dp, Gravity.CENTER))

        message.setTextColor(Color.WHITE)
        message.textSize = 14f
        message.gravity = Gravity.CENTER
        message.isVisible = false
        addView(message, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, Gravity.CENTER))

        logo.text = "audivue"
        logo.setTextColor(Color.WHITE)
        logo.textSize = 14f
        logo.typeface = Typeface.DEFAULT_BOLD
        logo.setShadowLayer(12f, 0f, 0f, Color.BLACK)
        val logoParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, Gravity.BOTTOM or Gravity.END)
        logoParams.setMargins(0, 0, 14.dp, 12.dp)
        addView(logo, logoParams)

        liveBadge.text = "LIVE"
        liveBadge.setTextColor(Color.WHITE)
        liveBadge.textSize = 11f
        liveBadge.typeface = Typeface.DEFAULT_BOLD
        liveBadge.setBackgroundColor(Color.rgb(255, 45, 45))
        liveBadge.setPadding(8.dp, 4.dp, 8.dp, 4.dp)
        liveBadge.isVisible = false
        val liveParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, Gravity.TOP or Gravity.START)
        liveParams.setMargins(14.dp, 14.dp, 0, 0)
        addView(liveBadge, liveParams)

        val centralControls = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }
        previousButton.applyControl("‹")
        playPauseButton.applyControl("▶")
        nextButton.applyControl("›")
        centralControls.addView(previousButton, LinearLayout.LayoutParams(46.dp, 46.dp))
        centralControls.addView(playPauseButton, LinearLayout.LayoutParams(60.dp, 60.dp).apply { setMargins(10.dp, 0, 10.dp, 0) })
        centralControls.addView(nextButton, LinearLayout.LayoutParams(46.dp, 46.dp))
        addView(centralControls, LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, Gravity.CENTER))
    }

    fun setLoading(show: Boolean) {
        loading.isVisible = show
        if (show) message.isVisible = false
    }

    fun setError(text: String) {
        loading.isVisible = false
        message.text = text
        message.isVisible = true
    }

    fun setLive(show: Boolean) {
        liveBadge.isVisible = show
    }

    fun setPlaying(playing: Boolean) {
        playPauseButton.text = if (playing) "Ⅱ" else "▶"
    }

    private fun TextView.applyControl(textValue: String) {
        text = textValue
        gravity = Gravity.CENTER
        setTextColor(Color.WHITE)
        textSize = 25f
        typeface = Typeface.DEFAULT_BOLD
        setBackgroundColor(Color.argb(115, 0, 0, 0))
        isClickable = true
        isFocusable = true
    }

    private val Int.dp: Int get() = (this * resources.displayMetrics.density).toInt()
}
