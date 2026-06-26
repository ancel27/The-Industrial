package com.audivue.sdk.ui

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import android.widget.TextView
import com.audivue.sdk.models.Comment

class CommentsView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : LinearLayout(context, attrs) {
    init {
        orientation = VERTICAL
    }

    fun submit(items: List<Comment>) {
        removeAllViews()
        items.forEach { comment ->
            addView(TextView(context).apply {
                text = "${comment.userName}: ${comment.text}"
                setTextColor(0xFFFFFFFF.toInt())
                textSize = 14f
            })
        }
    }
}
