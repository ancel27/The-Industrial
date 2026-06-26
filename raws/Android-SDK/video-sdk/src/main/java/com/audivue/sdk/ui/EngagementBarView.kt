package com.audivue.sdk.ui

import android.content.Context
import android.util.AttributeSet
import android.widget.Button
import android.widget.LinearLayout

class EngagementBarView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : LinearLayout(context, attrs) {
    val likeButton = Button(context).apply { text = "Like" }
    val shareButton = Button(context).apply { text = "Share" }
    val subscribeButton = Button(context).apply { text = "Subscribe" }

    init {
        orientation = HORIZONTAL
        addView(likeButton)
        addView(shareButton)
        addView(subscribeButton)
    }
}
